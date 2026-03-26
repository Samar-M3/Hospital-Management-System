package model;

import util.DBConnection;
import util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access operations for doctors.
 */
public class DoctorDAO {

    private final Connection conn = DBConnection.getInstance().getConnection();

    /**
     * Verifies doctor login credentials.
     * Doctors log in using email + password (same as Patient/Admin).
     */
    public Doctor login(String email, String password) {
        String sql = "SELECT * FROM Doctor WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password");
                boolean ok = hash != null && PasswordUtil.verify(password, hash);
                // Fallback for existing plain-text entries
                if (!ok && hash != null && hash.equals(password)) ok = true;
                if (ok) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] Login error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns ALL doctors, joined with their department name.
     * Used by Admin to display the full doctor list.
     */
    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>();

        // Simple select keeps this working even if the Department table is absent
        String sql = "SELECT * FROM Doctor ORDER BY name";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Doctor doc = mapRow(rs);
                list.add(doc);
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] getAllDoctors error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Adds a new doctor to the database. Called by Admin.
     * Password is hashed before storing.
     *
     * @return true if added successfully
     */
    public boolean addDoctor(Doctor doc) {
        String sql = "INSERT INTO Doctor (name, specialization, shifts, departmentId, email, password) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doc.getName());
            ps.setString(2, doc.getSpecialization());
            ps.setString(3, doc.getShifts());
            if (doc.getDepartmentId() == 0) ps.setNull(4, Types.INTEGER); else ps.setInt(4, doc.getDepartmentId());
            ps.setString(5, doc.getEmail().toLowerCase());
            ps.setString(6, PasswordUtil.hash(doc.getPassword()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] addDoctor error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing doctor's details. Called by Admin.
     * Does NOT update password (separate feature).
     *
     * @return true if updated successfully
     */
    public boolean updateDoctor(Doctor doc) {
        String sql = "UPDATE Doctor SET name=?, specialization=?, shifts=?, departmentId=?, email=? " +
                     "WHERE doctorId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doc.getName());
            ps.setString(2, doc.getSpecialization());
            ps.setString(3, doc.getShifts());
            if (doc.getDepartmentId() == 0) ps.setNull(4, Types.INTEGER); else ps.setInt(4, doc.getDepartmentId());
            ps.setString(5, doc.getEmail().toLowerCase());
            ps.setInt   (6, doc.getDoctorId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] updateDoctor error: " + e.getMessage() + " (retrying snake_case column)");
            String sqlSnake = "UPDATE Doctor SET name=?, specialization=?, shifts=?, departmentId=?, email=? " +
                              "WHERE doctor_id=?";
            try (PreparedStatement ps2 = conn.prepareStatement(sqlSnake)) {
                ps2.setString(1, doc.getName());
                ps2.setString(2, doc.getSpecialization());
                ps2.setString(3, doc.getShifts());
                if (doc.getDepartmentId() == 0) ps2.setNull(4, Types.INTEGER); else ps2.setInt(4, doc.getDepartmentId());
                ps2.setString(5, doc.getEmail().toLowerCase());
                ps2.setInt   (6, doc.getDoctorId());
                return ps2.executeUpdate() > 0;
            } catch (SQLException ex) {
                System.err.println("[DoctorDAO] updateDoctor retry failed: " + ex.getMessage());
                return false;
            }
        }
    }

    /**
     * Deletes a doctor by ID. Called by Admin.
     * Note: Tokens linked to this doctor will have doctor_id set to NULL (ON DELETE SET NULL).
     *
     * @return true if deleted successfully
     */
    public boolean removeDoctor(int doctorId) {
        String sql = "DELETE FROM Doctor WHERE doctorId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] removeDoctor error: " + e.getMessage() + " (retrying snake_case column)");
            String sqlSnake = "DELETE FROM Doctor WHERE doctor_id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(sqlSnake)) {
                ps2.setInt(1, doctorId);
                return ps2.executeUpdate() > 0;
            } catch (SQLException ex) {
                System.err.println("[DoctorDAO] removeDoctor retry failed: " + ex.getMessage());
                return false;
            }
        }
    }

    /** Checks if an email is already used by another doctor. */
    public boolean emailExists(String email) {
        String sql = "SELECT doctorId FROM Doctor WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] emailExists error: " + e.getMessage());
            return false;
        }
    }

    /** Returns a list of doctors available for a specific shift (for token booking). */
    public List<Doctor> getDoctorsByShift(String shift) {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM Doctor WHERE shifts LIKE ? ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + shift + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Doctor doc = mapRow(rs);
                list.add(doc);
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] getDoctorsByShift error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Helper: maps a ResultSet row to a Doctor object. The mapping is tolerant
     * of column-name differences (camelCase vs snake_case) so one bad column
     * no longer causes the entire list to stop rendering in the UI.
     */
    private Doctor mapRow(ResultSet rs) {
        Doctor d = new Doctor();

        d.setDoctorId(safeInt(rs, "doctorId", "doctor_id"));
        d.setName(safeString(rs, "name", "doctor_name"));
        d.setSpecialization(safeString(rs, "specialization", "speciality"));
        d.setShifts(safeString(rs, "shifts", "shift"));

        int deptId = safeInt(rs, "departmentId", "department_id");
        d.setDepartmentId(deptId);
        d.setEmail(safeString(rs, "email"));

        String deptName = safeString(rs,
                "departmentName", "department", "department_name", "department_display");
        if ((deptName == null || deptName.isBlank()) && deptId != 0) {
            deptName = String.valueOf(deptId);
        }
        if (deptName == null || deptName.isBlank()) {
            deptName = d.getSpecialization(); // last-resort fallback to avoid empty cells
        }
        d.setDepartmentName(deptName);

        return d;
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



