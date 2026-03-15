package util;

import model.Admin;
import model.Doctor;
import model.Patient;

/**
 * Tracks the current logged-in user (patient, doctor, or admin).
 * Implemented as a singleton shared across controllers.
 */
public class SessionManager {

    public enum Role { PATIENT, DOCTOR, ADMIN }

    private static SessionManager instance;

    private Patient currentPatient;
    private Doctor  currentDoctor;
    private Admin   currentAdmin;
    private Role    currentRole;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void loginAsPatient(Patient p) {
        clearSession();
        currentPatient = p;
        currentRole    = Role.PATIENT;
        System.out.println("[Session] Patient logged in: " + p.getName());
    }

    public void loginAsDoctor(Doctor d) {
        clearSession();
        currentDoctor = d;
        currentRole   = Role.DOCTOR;
        System.out.println("[Session] Doctor logged in: " + d.getName());
    }

    public void loginAsAdmin(Admin a) {
        clearSession();
        currentAdmin = a;
        currentRole  = Role.ADMIN;
        System.out.println("[Session] Admin logged in: " + a.getName());
    }

    public void logout() {
        clearSession();
        System.out.println("[Session] Logged out.");
    }

    private void clearSession() {
        currentPatient = null;
        currentDoctor  = null;
        currentAdmin   = null;
        currentRole    = null;
    }

    public Patient getCurrentPatient() { return currentPatient; }
    public Doctor  getCurrentDoctor()  { return currentDoctor; }
    public Admin   getCurrentAdmin()   { return currentAdmin; }
    public Role    getCurrentRole()    { return currentRole; }
    public boolean isLoggedIn()        { return currentRole != null; }
}
