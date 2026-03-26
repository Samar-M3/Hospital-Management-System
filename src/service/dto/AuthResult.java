package service.dto;

import model.User;
import util.SessionManager;

/**
 * Value object for authentication outcomes.
 */
public class AuthResult {
    private final SessionManager.Role role;
    private final User user;

    public AuthResult(SessionManager.Role role, User user) {
        this.role = role;
        this.user = user;
    }

    public boolean isSuccess() {
        return user != null && role != null;
    }

    public SessionManager.Role getRole() {
        return role;
    }

    public User getUser() {
        return user;
    }
}
