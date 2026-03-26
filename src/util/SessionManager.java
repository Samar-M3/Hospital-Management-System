package util;

import model.Admin;
import model.Doctor;
import model.Patient;
import model.User;

/**
 * Tracks the current logged-in user (patient, doctor, or admin).
 * Implemented as a singleton shared across controllers.
 */
public class SessionManager {

    public enum Role { PATIENT, DOCTOR, ADMIN }

    private static SessionManager instance;

    private User    currentUser;
    private Patient currentPatient;
    private Doctor  currentDoctor;
    private Admin   currentAdmin;
    private Role    currentRole;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    /** Primary login entry point (polymorphic). */
    public void login(User user, Role role) {
        clearSession();
        this.currentUser = user;
        this.currentRole = role;
        if (user instanceof Patient p) currentPatient = p;
        if (user instanceof Doctor d)  currentDoctor  = d;
        if (user instanceof Admin a)   currentAdmin   = a;
        System.out.println("[Session] Logged in as " + role + ": " + (user != null ? user.getName() : "unknown"));
    }

    // Backward-compatible role-specific helpers
    public void loginAsPatient(Patient p) { login(p, Role.PATIENT); }
    public void loginAsDoctor(Doctor d)   { login(d, Role.DOCTOR); }
    public void loginAsAdmin(Admin a)     { login(a, Role.ADMIN); }

    public void logout() {
        clearSession();
        System.out.println("[Session] Logged out.");
    }

    private void clearSession() {
        currentUser    = null;
        currentPatient = null;
        currentDoctor  = null;
        currentAdmin   = null;
        currentRole    = null;
    }

    public User getCurrentUser()  { return currentUser; }
    public Patient getCurrentPatient() { return currentPatient; }
    public Doctor  getCurrentDoctor()  { return currentDoctor; }
    public Admin   getCurrentAdmin()   { return currentAdmin; }
    public Role    getCurrentRole()    { return currentRole; }
    public boolean isLoggedIn()        { return currentRole != null; }
}
