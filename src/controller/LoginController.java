package controller;

import model.Admin;
import model.AdminDAO;
import model.Doctor;
import model.DoctorDAO;
import model.Patient;
import model.PatientDAO;
import util.SceneManager;
import util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * controller/LoginController.java
 * Single-form login. Role is detected automatically based on the credentials.
 */
public class LoginController implements Initializable {

    @FXML private TextField     tfEmail;
    @FXML private PasswordField pfPassword;
    @FXML private Label         lblError;

    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO  doctorDAO  = new DoctorDAO();
    private final AdminDAO   adminDAO   = new AdminDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.setText("");
        pfPassword.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String email    = tfEmail.getText().trim();
        String password = pfPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your email and password.");
            return;
        }

        // Patient
        Patient patient = patientDAO.login(email, password);
        if (patient != null) {
            SessionManager.getInstance().loginAsPatient(patient);
            SceneManager.switchScene("PatientDashboard.fxml", "Patient Dashboard");
            return;
        }

        // Doctor
        Doctor doctor = doctorDAO.login(email, password);
        if (doctor != null) {
            SessionManager.getInstance().loginAsDoctor(doctor);
            SceneManager.switchScene("DoctorDashboard.fxml", "Doctor Dashboard");
            return;
        }

        // Admin
        Admin admin = adminDAO.login(email, password);
        if (admin != null) {
            SessionManager.getInstance().loginAsAdmin(admin);
            SceneManager.switchScene("AdminDashboard.fxml", "Admin Dashboard");
            return;
        }

        showError("Invalid credentials. Please try again.");
    }

    @FXML
    private void goToRegister() {
        SceneManager.switchScene("Register.fxml", "Create Account");
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.getStyleClass().removeAll("success-label");
        if (!lblError.getStyleClass().contains("error-label"))
            lblError.getStyleClass().add("error-label");
    }
}
