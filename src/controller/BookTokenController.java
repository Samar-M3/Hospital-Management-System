package controller;

import model.Token;
import model.TokenDAO;
import util.SceneManager;
import util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Smart token booking controller.
 * Patient enters their health problem → system maps to specialization
 * and creates a token with an auto-generated number.
 */
public class BookTokenController implements Initializable {

    @FXML private ComboBox<String> cbProblem;       // editable list of common problems
    @FXML private ComboBox<String> cbDepartment;    // patient-selected department/specialization
    @FXML private TextArea         taDetails;       // optional extra notes
    @FXML private Label            lblSpecialization;
    @FXML private Label            lblMessage;

    private final TokenDAO tokenDAO = new TokenDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblMessage.setText("");
        lblSpecialization.setText("");

        cbProblem.setEditable(true);
        cbProblem.getItems().addAll(
            "Ear Problem",
            "Heart Problem",
            "Skin Problem",
            "Eye Problem",
            "General Checkup"
        );

        cbDepartment.setPromptText("Select department / specialization");
        cbDepartment.getItems().setAll(tokenDAO.getAvailableDepartments());
    }

    @FXML
    private void handleBookToken() {
        String problem = getProblemText();
        if (problem.isEmpty()) {
            showMessage("Please enter or select your health problem.", false);
            return;
        }
        String department = cbDepartment.getValue();
        if (department == null || department.isBlank()) {
            showMessage("Please pick a department / specialization.", false);
            return;
        }

        int patientId = SessionManager.getInstance().getCurrentPatient().getPatientId();
        Token created = tokenDAO.createToken(patientId, problem, department);

        if (created != null) {
            lblSpecialization.setText("Department: " + created.getSpecialization());
            showMessage("Token ID " + created.getTokenId() + " created (" + created.getSpecialization() + "). Status: Pending.", true);
            cbProblem.setValue("");
            cbProblem.getEditor().clear();
            taDetails.clear();
            cbDepartment.getSelectionModel().clearSelection();
        } else {
            showMessage("Could not create token. Please try again.", false);
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchScene("PatientDashboard.fxml", "Patient Dashboard");
    }

    private String getProblemText() {
        String typed = cbProblem.getEditor() != null ? cbProblem.getEditor().getText().trim() : "";
        String selected = cbProblem.getValue() != null ? cbProblem.getValue().trim() : "";
        String base = !typed.isEmpty() ? typed : selected;
        if (!taDetails.getText().trim().isEmpty()) {
            return base + " - " + taDetails.getText().trim();
        }
        return base;
    }

    private void showMessage(String msg, boolean success) {
        lblMessage.setText(msg);
        lblMessage.getStyleClass().removeAll("error-label", "success-label");
        lblMessage.getStyleClass().add(success ? "success-label" : "error-label");
    }
}
