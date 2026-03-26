package service;

import model.Admin;
import model.AdminDAO;
import model.Doctor;
import model.DoctorDAO;
import model.Patient;
import model.PatientDAO;
import service.dto.AuthResult;
import util.SessionManager;

/**
 * Database-backed authentication that tries each role in a deterministic order.
 */
public class DatabaseAuthenticationService implements AuthenticationService {

    private final PatientDAO patientDAO;
    private final DoctorDAO  doctorDAO;
    private final AdminDAO   adminDAO;

    public DatabaseAuthenticationService() {
        this(new PatientDAO(), new DoctorDAO(), new AdminDAO());
    }

    public DatabaseAuthenticationService(PatientDAO patientDAO, DoctorDAO doctorDAO, AdminDAO adminDAO) {
        this.patientDAO = patientDAO;
        this.doctorDAO  = doctorDAO;
        this.adminDAO   = adminDAO;
    }

    @Override
    public AuthResult authenticate(String email, String password) {
        Patient patient = patientDAO.login(email, password);
        if (patient != null) {
            return new AuthResult(SessionManager.Role.PATIENT, patient);
        }

        Doctor doctor = doctorDAO.login(email, password);
        if (doctor != null) {
            return new AuthResult(SessionManager.Role.DOCTOR, doctor);
        }

        Admin admin = adminDAO.login(email, password);
        if (admin != null) {
            return new AuthResult(SessionManager.Role.ADMIN, admin);
        }

        return new AuthResult(null, null);
    }
}
