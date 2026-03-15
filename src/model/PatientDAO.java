package model;

import util.DBConnection;
import util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access operations for patients.
 */
public class PatientDAO {

    private final Connection conn = DBConnection.getInstance().getConnection();

    /**
     * Saves a new patient. Password is BCrypt-hashed before storing.
     */
    public boolean register(Patient p) {
        String sql = "INSERT INTO Patient (name, age, gender, contact, email, password) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt   (2, p.getAge());
            ps.setString(3, p.getGender());
            ps.setString(4, p.getContact());
            ps.setString(5, p.getEmail());
            ps.setString(6, PasswordUtil.hash(p.getPassword()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PatientDAO] register error: " + e.getMessage());
            return false;
        }
    }

    /** Returns true if email is already in the database. */
    public boolean emailExists(String email) {
        String sql = "SELECT patient_id FROM Patient WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[PatientDAO] emailExists error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Finds patient by email, then BCrypt-verifies the password.
     * Returns Patient on success, null on failure.
     */
    public Patient login(String email, String password) {
        String sql = "SELECT * FROM Patient WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && PasswordUtil.verify(password, rs.getString("password"))) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] login error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns every patient in the system.
     * Used by Admin to view the patient list.
     */
    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM Patient ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[PatientDAO] getAllPatients error: " + e.getMessage());
        }
        return list;
    }

    /** Maps a ResultSet row to a Patient object. */
    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt   ("patient_id"));
        p.setName     (rs.getString("name"));
        p.setAge      (rs.getInt   ("age"));
        p.setGender   (rs.getString("gender"));
        p.setContact  (rs.getString("contact"));
        p.setEmail    (rs.getString("email"));
        return p;
    }
}
