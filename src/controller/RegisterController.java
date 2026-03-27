package controller;

import model.Patient;
import model.PatientDAO;
import util.SceneManager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.ResourceBundle;

/**
 * Handles self-registration for new patients.
 */
public class RegisterController implements Initializable {

    @FXML private TextField   tfName;
    @FXML private ComboBox<String> cbGender;
    @FXML private DatePicker  dpDob;
    @FXML private TextField   tfPhone;
    @FXML private TextField   tfEmail;
    @FXML private PasswordField pfPassword;
    @FXML private PasswordField pfConfirm;
    @FXML private Label       lblMessage;

    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbGender.getItems().addAll("Male", "Female", "Other");
        lblMessage.setText("");
    }

    /**
     * Validate inputs and register a new patient record.
     */
    @FXML
    private void handleRegister() {
        String name     = tfName.getText().trim();
        String gender   = cbGender.getValue();
        LocalDate dob   = dpDob.getValue();
        String phone    = tfPhone.getText().trim();
        String email    = tfEmail.getText().trim();
        String password = pfPassword.getText();
        String confirm  = pfConfirm.getText();

        if (name.isEmpty() || gender == null || dob == null ||
            phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if (name.matches(".*\\d.*")) {
            showError("Name cannot contain numbers.");
            return;
        }

        if (!phone.matches("\\d+")) {
            showError("Phone number must contain digits only.");
            return;
        }
        if (phone.length() != 10) {
            showError("Phone number must be exactly 10 digits.");
            return;
        }

        if (!email.contains("@")) {
            showError("Email must contain @.");
            return;
        }
        if (!email.contains(".")) {
            showError("Please enter a valid email address.");
            return;
        }

        if (password.length() < 8) {
            showError("Password must be at least 8 characters.");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        if (dob.isAfter(LocalDate.now())) {
            showError("Date of birth cannot be in the future.");
            return;
        }

        if (patientDAO.emailExists(email)) {
            showError("An account with this email already exists.");
            return;
        }

        int age = Period.between(dob, LocalDate.now()).getYears();

        Patient p = new Patient();
        p.setName(name);
        p.setAge(age);
        p.setGender(gender);
        p.setContact(phone);
        p.setEmail(email);
        p.setPassword(password);   // hashed in PatientDAO.register()

        if (patientDAO.register(p)) {
            showSuccess("Account created! Redirecting to login…");
            new Thread(() -> {
                try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() ->
                    SceneManager.switchScene("Login.fxml", "Login")
                );
            }).start();
        } else {
            showError("Registration failed. Please try again.");
        }
    }

    @FXML
    private void goToLogin() {
        SceneManager.switchScene("Login.fxml", "Login");
    }

    private void showError(String msg) {
        lblMessage.setText(msg);
        lblMessage.getStyleClass().removeAll("success-label");
        if (!lblMessage.getStyleClass().contains("error-label"))
            lblMessage.getStyleClass().add("error-label");
    }

    private void showSuccess(String msg) {
        lblMessage.setText(msg);
        lblMessage.getStyleClass().removeAll("error-label");
        if (!lblMessage.getStyleClass().contains("success-label"))
            lblMessage.getStyleClass().add("success-label");
    }
}
