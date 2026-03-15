package controller;

import model.Token;
import model.TokenDAO;
import util.SceneManager;
import util.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Displays the current patient's tokens and lets them cancel pending ones.
 */
public class MyTokensController implements Initializable {

    @FXML private TableView<Token>           tableTokens;
    @FXML private TableColumn<Token,Integer> colNumber;
    @FXML private TableColumn<Token,String>  colSpecialization;
    @FXML private TableColumn<Token,String>  colProblem;
    @FXML private TableColumn<Token,String>  colDoctor;
    @FXML private TableColumn<Token,String>  colStatus;
    @FXML private TableColumn<Token,java.time.LocalDateTime>  colCreatedAt;
    @FXML private Label                      lblMessage;

    private final TokenDAO tokenDAO = new TokenDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblMessage.setText("");
        setupTable();
        loadTokens();
    }

    private void setupTable() {
        colNumber       .setCellValueFactory(new PropertyValueFactory<>("tokenNumber"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colProblem      .setCellValueFactory(new PropertyValueFactory<>("healthProblem"));
        colDoctor       .setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colStatus       .setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty) { setText(null); setStyle(""); return; }
                setText(status);
                switch (status) {
                    case "Pending"   -> setStyle("-fx-text-fill: #c2410c; -fx-font-weight:700;");
                    case "Approved"  -> setStyle("-fx-text-fill: #1d4ed8; -fx-font-weight:700;");
                    case "Completed" -> setStyle("-fx-text-fill: #15803d; -fx-font-weight:700;");
                    case "Cancelled" -> setStyle("-fx-text-fill: #b91c1c; -fx-font-weight:700;");
                    default          -> setStyle("-fx-text-fill: #0f172a;");
                }
            }
        });
        colCreatedAt    .setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colCreatedAt    .setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(java.time.LocalDateTime dt, boolean empty) {
                super.updateItem(dt, empty);
                setText(empty || dt == null ? "" : dt.toLocalDate().toString() + " " + dt.toLocalTime());
            }
        });
    }

    /** Fetch and display this patient's tokens. */
    private void loadTokens() {
        int patientId = SessionManager.getInstance().getCurrentPatient().getPatientId();
        List<Token> tokens = tokenDAO.getTokensByPatient(patientId);
        tableTokens.setItems(FXCollections.observableArrayList(tokens));
        tableTokens.refresh();
    }

    @FXML
    private void handleCancel() {
        Token selected = tableTokens.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select a token to cancel.", false);
            return;
        }
        if (!"Pending".equals(selected.getStatus())) {
            showMessage("Only pending tokens can be cancelled.", false);
            return;
        }
        boolean ok = tokenDAO.cancelToken(selected.getTokenId());
        showMessage(ok ? "Token cancelled." : "Could not cancel token.", ok);
        if (ok) loadTokens();
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("PatientDashboard.fxml", "Patient Dashboard");
    }

    private void showMessage(String msg, boolean ok) {
        lblMessage.setText(msg);
        lblMessage.getStyleClass().removeAll("error-label", "success-label");
        lblMessage.getStyleClass().add(ok ? "success-label" : "error-label");
    }
}
