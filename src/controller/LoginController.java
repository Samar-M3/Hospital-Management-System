package controller;

import service.AuthenticationService;
import service.DatabaseAuthenticationService;
import service.dto.AuthResult;
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

    // Depend on abstraction for authentication (better testability & swapping)
    private final AuthenticationService authService = new DatabaseAuthenticationService();

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

        AuthResult result = authService.authenticate(email, password);
        if (!result.isSuccess()) {
            showError("Invalid credentials. Please try again.");
            return;
        }

        SessionManager.getInstance().login(result.getUser(), result.getRole());
        switch (result.getRole()) {
            case PATIENT -> SceneManager.switchScene("PatientDashboard.fxml", "Patient Dashboard");
            case DOCTOR  -> SceneManager.switchScene("DoctorDashboard.fxml", "Doctor Dashboard");
            case ADMIN   -> SceneManager.switchScene("AdminDashboard.fxml", "Admin Dashboard");
            default      -> showError("Unknown role. Please contact support.");
        }
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
