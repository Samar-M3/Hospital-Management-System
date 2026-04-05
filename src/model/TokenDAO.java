package model;

import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Smart token routing:
 *   - Patient provides a health problem
 *   - Problem is mapped to a doctor specialization
 *   - A token number is generated per-specialization per-day
 *   - Tokens surface automatically to doctors with that specialization
 */
public class TokenDAO {

    private final Connection conn = DBConnection.getInstance().getConnection();

    /**
     * Create a token for a patient with optional scheduling metadata.
     */
    public Token createToken(int patient_id, String healthProblem, String department,
                             String shift, LocalDate appointmentDate, LocalTime appointmentTime,
                             String roomNumber) {
        String specialization = (department == null || department.trim().isEmpty())
                ? mapProblemToSpecialization(healthProblem)
                : department.trim();
        int nextNumber = getNextTokenNumberForToday(specialization);

        String sql = """
            INSERT INTO Token (token_number, health_problem, specialization,
                               status, created_at, patient_id, doctorId,
                               shift, appointment_date, appointment_time, room_number)
            VALUES (?, ?, ?, 'Pending', NOW(), ?, NULL, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, nextNumber);
            ps.setString(2, healthProblem);
            ps.setString(3, specialization);
            ps.setInt   (4, patient_id);
            ps.setString(5, shift);
            if (appointmentDate != null) ps.setDate(6, Date.valueOf(appointmentDate)); else ps.setNull(6, Types.DATE);
            if (appointmentTime != null) ps.setTime(7, Time.valueOf(appointmentTime)); else ps.setNull(7, Types.TIME);
            ps.setString(8, roomNumber);
            int rows = ps.executeUpdate();
            if (rows == 0) return null;

            int tokenId = 0;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) tokenId = keys.getInt(1);

            Token t = new Token();
            t.setTokenId(tokenId);
            t.setTokenNumber(nextNumber);
            t.setHealthProblem(healthProblem);
            t.setSpecialization(specialization);
            t.setStatus("Pending");
            t.setPatientId(patient_id);
            t.setShift(shift);
            t.setAppointmentDate(appointmentDate);
            t.setAppointmentTime(appointmentTime);
            t.setRoomNumber(roomNumber);
            return t;
        } catch (SQLException e) {
            System.err.println("[TokenDAO] createToken error: " + e.getMessage());
            return null;
        }
    }

    /** Backwards-compatible overload without scheduling metadata. */
    public Token createToken(int patient_id, String healthProblem, String department) {
        return createToken(patient_id, healthProblem, department,
                "Day", java.time.LocalDate.now(), java.time.LocalTime.NOON, null);
    }

    /**
     * For patients: list all their tokens (newest first).
     */
    public List<Token> getTokensByPatient(int patient_id) {
        String sql = """
            SELECT t.*, p.name AS patient_name, d.name AS doctor_name
            FROM Token t
            LEFT JOIN Patient p ON t.patient_id = p.patient_id
            LEFT JOIN Doctor  d ON t.doctorId  = d.doctorId
            WHERE t.patient_id = ?
            ORDER BY t.token_number ASC, t.token_id ASC
            """;
        List<Token> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patient_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[TokenDAO] getTokensByPatient error: " + e.getMessage());
        }
        return list;
    }

    /**
     * For doctors: show waiting / in-progress tokens matching their specialization.
     * Includes unclaimed tokens (doctorId IS NULL) and ones already claimed by them.
     */
    public List<Token> getTokensForDoctor(String specialization, int doctorId) {
        // Handle null or empty specialization - return all tokens for this doctor or unassigned
        if (specialization == null || specialization.trim().isEmpty()) {
            String sql = """
                SELECT t.*, p.name AS patient_name, d.name AS doctor_name
                FROM Token t
                LEFT JOIN Patient p ON t.patient_id = p.patient_id
                LEFT JOIN Doctor  d ON t.doctorId  = d.doctorId
                WHERE t.doctorId IS NULL OR t.doctorId = ?
                ORDER BY t.token_number ASC, t.token_id ASC
                """;
            List<Token> list = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, doctorId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) list.add(mapRow(rs));
            } catch (SQLException e) {
                System.err.println("[TokenDAO] getTokensForDoctor error: " + e.getMessage());
            }
            return list;
        }
        
        String sql = """
            SELECT t.*, p.name AS patient_name, d.name AS doctor_name
            FROM Token t
            LEFT JOIN Patient p ON t.patient_id = p.patient_id
            LEFT JOIN Doctor  d ON t.doctorId  = d.doctorId
            WHERE TRIM(UPPER(t.specialization)) = TRIM(UPPER(?))
              AND (t.doctorId IS NULL OR t.doctorId = ?)
            ORDER BY t.token_number ASC, t.token_id ASC
            """;
        List<Token> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, specialization);
            ps.setInt   (2, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[TokenDAO] getTokensForDoctor error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Admin: view every token in the system.
     */
    public List<Token> getAllTokens() {
        String sql = """
            SELECT t.*, p.name AS patient_name, d.name AS doctor_name
            FROM Token t
            LEFT JOIN Patient p ON t.patient_id = p.patient_id
            LEFT JOIN Doctor  d ON t.doctorId  = d.doctorId
            ORDER BY t.token_number ASC, t.token_id ASC
            """;
        List<Token> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[TokenDAO] getAllTokens error: " + e.getMessage());
        }
        return list;
    }

    /** Doctor approves a pending token and claims it. */
    public boolean approveToken(int tokenId, int doctorId) {
        String sql = """
            UPDATE Token
               SET doctorId = ?, status = 'Approved'
             WHERE token_id = ? AND status = 'Pending'
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, tokenId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TokenDAO] approveToken error: " + e.getMessage());
            return false;
        }
    }

    /** Doctor marks an approved token as completed. */
    public boolean completeToken(int tokenId, int doctorId) {
        String sql = """
            UPDATE Token
               SET doctorId = ?, status = 'Completed'
             WHERE token_id = ? AND status = 'Approved' AND (doctorId IS NULL OR doctorId = ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, tokenId);
            ps.setInt(3, doctorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TokenDAO] completeToken error: " + e.getMessage());
            return false;
        }
    }

    /** Doctor cancels a token that hasn't been completed yet. */
    public boolean cancelTokenByDoctor(int tokenId, int doctorId) {
        String sql = """
            UPDATE Token
               SET doctorId = COALESCE(doctorId, ?), status = 'Cancelled'
             WHERE token_id = ?
               AND status IN ('Pending','Approved')
               AND (doctorId IS NULL OR doctorId = ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, tokenId);
            ps.setInt(3, doctorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TokenDAO] cancelTokenByDoctor error: " + e.getMessage());
            return false;
        }
    }

    /** Patient cancels their own pending token. Guards against cancelling other users' tokens. */
    public boolean cancelPendingTokenForPatient(int tokenId, int patientId) {
        String sql = """
            UPDATE Token
               SET status = 'Cancelled'
             WHERE token_id = ? AND patient_id = ? AND status = 'Pending'
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tokenId);
            ps.setInt(2, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TokenDAO] cancelPendingTokenForPatient error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generic status update (doctor completes, patient cancels).
     */
    public boolean updateStatus(int tokenId, String status) {
        String sql = "UPDATE Token SET status = ? WHERE token_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt   (2, tokenId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TokenDAO] updateStatus error: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelToken(int tokenId) { return updateStatus(tokenId, "Cancelled"); }

    /** Returns distinct specializations/departments available from doctors. */
    public List<String> getAvailableDepartments() {
        String sql = "SELECT DISTINCT specialization FROM Doctor WHERE specialization IS NOT NULL ORDER BY specialization";
        List<String> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) items.add(rs.getString(1));
        } catch (SQLException e) {
            System.err.println("[TokenDAO] getAvailableDepartments error: " + e.getMessage());
        }
        if (items.isEmpty()) {
            items.add("General Medicine");
        }
        return items;
    }


    private int getNextTokenNumberForToday(String specialization) {
        String sql = """
            SELECT COALESCE(MAX(token_number),0) + 1
            FROM Token
            WHERE specialization = ?
              AND DATE(created_at) = CURDATE()
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, specialization);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[TokenDAO] getNextTokenNumberForToday error: " + e.getMessage());
        }
        return 1;
    }

    /** Basic keyword mapping; can be replaced with a table later. */
    private String mapProblemToSpecialization(String problem) {
        String p = problem.toLowerCase();
        if (p.contains("ear") || p.contains("nose") || p.contains("throat")) return "ENT";
        if (p.contains("heart") || p.contains("cardio")) return "Cardiologist";
        if (p.contains("eye") || p.contains("vision")) return "Ophthalmologist";
        if (p.contains("skin") || p.contains("derma") || p.contains("rash")) return "Dermatologist";
        return "General Medicine";
    }

    /** Map a ResultSet row to Token. */
    private Token mapRow(ResultSet rs) throws SQLException {
        Token t = new Token();
        t.setTokenId       (rs.getInt   ("token_id"));
        try {
            t.setTokenNumber(rs.getInt("token_number"));
        } catch (SQLException ignored) {
            // Fallback: if the column is still camelCase
            t.setTokenNumber(rs.getInt("tokenNumber"));
        }
        t.setHealthProblem (rs.getString("health_problem"));
        try {
            t.setSpecialization(rs.getString("specialization"));
        } catch (SQLException e) {
            // legacy column name
            t.setSpecialization(rs.getString("department"));
        }
        t.setStatus        (rs.getString("status"));
        int pid = 0;
        try { pid = rs.getInt("patient_id"); } catch (SQLException e) {
            try { pid = rs.getInt("patient_id"); } catch (SQLException ignored) {}
        }
        t.setPatientId(pid);
        int did = 0;
        try { did = rs.getInt("doctorId"); t.setDoctorId(rs.wasNull() ? null : did); }
        catch (SQLException e) {
            try { did = rs.getInt("doctorId"); t.setDoctorId(rs.wasNull() ? null : did); } catch (SQLException ignored) {}
        }

        try { t.setShift(rs.getString("shift")); } catch (SQLException ignored) {}
        try {
            Date apptDate = rs.getDate("appointment_date");
            if (apptDate != null) t.setAppointmentDate(apptDate.toLocalDate());
        } catch (SQLException ignored) {}
        try {
            Time apptTime = rs.getTime("appointment_time");
            if (apptTime != null) t.setAppointmentTime(apptTime.toLocalTime());
        } catch (SQLException ignored) {}
        try { t.setRoomNumber(rs.getString("room_number")); } catch (SQLException ignored) {}

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setCreatedAt(ts.toLocalDateTime());

        try { t.setPatientName(rs.getString("patient_name")); } catch (SQLException ignored) {}
        try { t.setDoctorName (rs.getString("doctor_name")); }  catch (SQLException ignored) {}

        // Fallbacks for readability when joined columns are missing
        if ((t.getPatientName() == null || t.getPatientName().isBlank()) && t.getPatientId() > 0) {
            t.setPatientName(lookupPatientName(t.getPatientId()));
        }
        if ((t.getDoctorName() == null || t.getDoctorName().isBlank()) && t.getDoctorId() != null) {
            t.setDoctorName(lookupDoctorName(t.getDoctorId()));
        }
        if (t.getTokenNumber() <= 0) {
            t.setTokenNumber(t.getTokenId());
        }
        return t;
    }

    /** Allows restoring a cancelled token back to Approved (undo). */
    public boolean restoreCancelledToApproved(int tokenId, int doctorId) {
        String sql = """
            UPDATE Token
               SET status = 'Approved', doctorId = COALESCE(doctorId, ?)
             WHERE token_id = ? AND status = 'Cancelled'
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, tokenId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TokenDAO] restoreCancelledToApproved error: " + e.getMessage());
            return false;
        }
    }

    private String lookupPatientName(int patient_id) {
        String sql = "SELECT name FROM Patient WHERE patient_id = ? OR patient_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patient_id);
            ps.setInt(2, patient_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException ignored) {}
        return "Patient " + patient_id;
    }

    private String lookupDoctorName(int doctorId) {
        String sql = "SELECT name FROM Doctor WHERE doctorId = ? OR doctorId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException ignored) {}
        return "Doctor " + doctorId;
    }
}
