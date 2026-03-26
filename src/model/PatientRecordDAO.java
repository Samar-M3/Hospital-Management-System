package model;

import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistence layer for PatientRecord (backed by the existing Report table).
 * Adds UML-friendly naming while keeping legacy aliases to avoid behavior changes.
 */
public class PatientRecordDAO {

    private final Connection conn = DBConnection.getInstance().getConnection();

    /**
     * Saves a new patient record after a consultation.
     * Called by the Doctor when they complete a token.
     *
     * @param record PatientRecord with problem, solution, medications, notes, patientId, doctorId, tokenId
     * @return true if saved successfully
     */
    public boolean addPatientRecord(PatientRecord record) {
        String sql = "INSERT INTO Report (diagnosis, prescription, notes, patient_id, doctorId, token_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getProblem());       // diagnosis
            ps.setString(2, record.getMedications());   // prescription/medicines
            ps.setString(3, record.getNotes());
            ps.setInt   (4, record.getPatientId());
            ps.setInt   (5, record.getDoctorId());
            if (record.getTokenId() != null) ps.setInt(6, record.getTokenId()); else ps.setNull(6, java.sql.Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PatientRecordDAO] addPatientRecord error: " + e.getMessage());
            return false;
        }
    }

    /** Legacy alias to preserve prior call sites if any remain. */
    public boolean addReport(PatientRecord record) { return addPatientRecord(record); }

    /**
     * Returns the full visit/medical history for a patient.
     * Joins the Doctor table to show which doctor wrote each record.
     *
     * @param patientId the logged-in patient's ID
     */
    public List<PatientRecord> getPatientRecordsByPatient(int patientId) {
        List<PatientRecord> list = new ArrayList<>();

        String sql = """
            SELECT r.*, d.name AS doctor_name
            FROM Report r
            LEFT JOIN Doctor d ON r.doctorId = d.doctorId
            LEFT JOIN Token  t ON r.token_id  = t.token_id
            WHERE r.patient_id = ?
              AND (t.status = 'Completed' OR r.token_id IS NULL)
            ORDER BY r.created_at DESC
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PatientRecord rec = mapRow(rs);
                rec.setDoctorName(rs.getString("doctor_name"));
                list.add(rec);
            }
        } catch (SQLException e) {
            System.err.println("[PatientRecordDAO] getPatientRecordsByPatient error: " + e.getMessage());
        }
        return list;
    }

    /** Legacy alias to preserve prior call sites if any remain. */
    public List<PatientRecord> getReportsByPatient(int patientId) { return getPatientRecordsByPatient(patientId); }

    /** Helper: maps a ResultSet row to a PatientRecord object. */
    private PatientRecord mapRow(ResultSet rs) {
        PatientRecord r = new PatientRecord();
        r.setReportId(safeInt(rs, "report_id", "reportId", "record_id"));

        String diagnosis    = safeString(rs, "diagnosis", "problem");
        String prescription = safeString(rs, "prescription", "medications", "medicine");
        r.setProblem(diagnosis);
        r.setSolution(prescription);   // reuse column for UI "solution"
        r.setMedications(prescription);   // show prescribed medicines
        r.setNotes(safeString(rs, "notes", "remarks"));

        int tid = safeInt(rs, "token_id", "tokenId");
        r.setTokenId(tid == 0 ? null : tid);
        r.setPatientId(safeInt(rs, "patient_id", "patientId"));
        r.setDoctorId(safeInt(rs, "doctorId", "doctor_id"));

        Timestamp ts = null;
        try { ts = rs.getTimestamp("created_at"); } catch (SQLException ignored) {}
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());

        return r;
    }

    private int safeInt(ResultSet rs, String... columns) {
        for (String col : columns) {
            try {
                int val = rs.getInt(col);
                if (!rs.wasNull() || val != 0) return val;
            } catch (SQLException ignored) { /* try next */ }
        }
        return 0;
    }

    private String safeString(ResultSet rs, String... columns) {
        for (String col : columns) {
            try {
                String val = rs.getString(col);
                if (val != null) return val;
            } catch (SQLException ignored) { /* try next */ }
        }
        return null;
    }
}
