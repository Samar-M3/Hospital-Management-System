package model;

import util.DBConnection;
import util.PasswordUtil;

import java.sql.*;

/**
 * Data access operations for admins.
 */
public class AdminDAO {

    private final Connection conn = DBConnection.getInstance().getConnection();

    /**
     * Verify admin credentials by email and password.
     */
    public Admin login(String email, String password) {
        String sql = "SELECT * FROM Admin WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password");
                boolean ok = hash != null && PasswordUtil.verify(password, hash);
                if (!ok && hash != null && hash.equals(password)) ok = true; // tolerate legacy plain-text entries
                if (ok) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[AdminDAO] Login error: " + e.getMessage());
        }
        return null;
    }

    /** True if another admin already uses this email. */
    public boolean emailExists(String email) {
        String sql = "SELECT admin_id FROM Admin WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[AdminDAO] emailExists error: " + e.getMessage());
            return false;
        }
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setAdminId (rs.getInt   ("admin_id"));
        a.setName    (rs.getString("name"));
        a.setEmail   (rs.getString("email"));
        return a;
    }
}
