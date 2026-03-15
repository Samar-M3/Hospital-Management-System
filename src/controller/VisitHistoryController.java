package controller;

import model.PatientRecord;
import model.PatientRecordDAO;
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
 * Displays the logged-in patient's visit history.
 */
public class VisitHistoryController implements Initializable {

    @FXML private TableView<PatientRecord>           tableHistory;
    @FXML private TableColumn<PatientRecord,String>  colDate;
    @FXML private TableColumn<PatientRecord,String>  colDoctor;
    @FXML private TableColumn<PatientRecord,String>  colProblem;
    @FXML private TableColumn<PatientRecord,String>  colSolution;
    @FXML private TableColumn<PatientRecord,String>  colMedications;
    @FXML private TableColumn<PatientRecord,String>  colNotes;
    @FXML private Label                        lblMessage;

    // Detail panel
    @FXML private Label lblDetailDoctor;
    @FXML private Label lblDetailDate;
    @FXML private Label lblDetailProblem;
    @FXML private Label lblDetailSolution;
    @FXML private Label lblDetailMeds;
    @FXML private Label lblDetailNotes;

    private final PatientRecordDAO patientRecordDAO = new PatientRecordDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadHistory();

        tableHistory.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, selected) -> showDetail(selected)
        );
    }

    private void setupTableColumns() {
        colDate       .setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colDoctor     .setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colProblem    .setCellValueFactory(new PropertyValueFactory<>("problem"));
        colSolution   .setCellValueFactory(new PropertyValueFactory<>("solution"));
        colMedications.setCellValueFactory(new PropertyValueFactory<>("medications"));
        if (colNotes != null) {
            colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        }
    }

    private void loadHistory() {
        int patientId = SessionManager.getInstance().getCurrentPatient().getPatientId();
        List<PatientRecord> reports = patientRecordDAO.getPatientRecordsByPatient(patientId);
        tableHistory.setItems(FXCollections.observableArrayList(reports));
        applySort(tableHistory, colDate);

        if (reports.isEmpty()) {
            lblMessage.setText("No visit history found. Your reports will appear here after consultations.");
            lblMessage.getStyleClass().add("error-label");
        } else {
            lblMessage.setText(reports.size() + " visit(s) found.");
            lblMessage.getStyleClass().add("success-label");
        }
    }

    private void showDetail(PatientRecord r) {
        if (r == null) return;
        lblDetailDoctor  .setText("Dr. " + (r.getDoctorName() != null ? r.getDoctorName() : "Unknown"));
        lblDetailDate    .setText(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "-");
        lblDetailProblem .setText(r.getProblem()     != null ? r.getProblem()     : "-");
        lblDetailSolution.setText(r.getSolution()    != null ? r.getSolution()    : "-");
        lblDetailMeds    .setText(r.getMedications() != null ? r.getMedications() : "-");
        if (lblDetailNotes != null) {
            lblDetailNotes.setText(r.getNotes() != null && !r.getNotes().isBlank() ? r.getNotes() : "-");
        }
    }

    @FXML private void goBack() { SceneManager.switchScene("PatientDashboard.fxml", "Patient Dashboard"); }

    private <T> void applySort(TableView<T> table, TableColumn<T, ?> col) {
        if (table == null || col == null) return;
        col.setSortType(TableColumn.SortType.ASCENDING);
        table.getSortOrder().setAll(col);
        table.sort();
    }
}
