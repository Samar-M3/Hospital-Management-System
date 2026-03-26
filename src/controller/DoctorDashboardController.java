package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Doctor;
import model.Patient;
import model.PatientRecord;
import model.PatientRecordDAO;
import model.Token;
import model.TokenDAO;
import service.CrudService;
import service.NotificationService;
import service.PatientService;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Modern, full-screen Doctor Dashboard controller.
 * Demonstrates OOP pillars:
 *  - Encapsulation: services wrap data
 *  - Inheritance: Doctor/Patient extend User
 *  - Abstraction & Polymorphism: CrudService<T> implemented by concrete services
 */
public class DoctorDashboardController implements Initializable {

    // Header & hero labels
    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label nextAppointmentLabel;
    @FXML private Label nextAppointmentLabelOverview;

    // Stats cards
    @FXML private Label totalPatientsValue;
    @FXML private Label todayAppointmentsValue;
    @FXML private Label waitingTokensValue;
    @FXML private Label completionRateValue;

    // Patients table
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, String> patientNameCol;
    @FXML private TableColumn<Patient, String> patientAgeCol;
    @FXML private TableColumn<Patient, String> patientGenderCol;
    @FXML private TableColumn<Patient, String> patientContactCol;

    // Token queue
    @FXML private TableView<Token> tokenTable;
    @FXML private TableColumn<Token, String> tokenNumberCol;
    @FXML private TableColumn<Token, String> tokenPatientCol;
    @FXML private TableColumn<Token, String> tokenDeptCol;
    @FXML private TableColumn<Token, String> tokenProblemCol;
    @FXML private TableColumn<Token, String> tokenRoomCol;
    @FXML private TableColumn<Token, String> tokenShiftCol;
    @FXML private TableColumn<Token, String> tokenDateCol;
    @FXML private TableColumn<Token, String> tokenTimeCol;
    @FXML private TableColumn<Token, String> tokenStatusCol;
    @FXML private Label tokenActionMessage;
    @FXML private TextArea taDiagnosis;
    @FXML private TextArea taMedicines;
    @FXML private TextArea taNotes;

    // Notifications & charts
    @FXML private ListView<String> notificationsList;
    @FXML private BarChart<String, Number> appointmentBarChart;
    @FXML private PieChart patientPieChart;

    @FXML private Button btnAddPatient;
    @FXML private Button btnEditPatient;
    @FXML private Button btnDeletePatient;
    @FXML private TextField searchField;
    @FXML private Button btnToggleSidebar;
    @FXML private Button btnNavOverview;
    @FXML private Button btnNavPatients;
    @FXML private Button btnNavAppointments;
    @FXML private Button btnNavSettings;
    @FXML private VBox sidebar;
    @FXML private VBox overviewSection;
    @FXML private VBox patientsSection;
    @FXML private VBox appointmentsSection;

    // Services (polymorphic via CrudService)
    private final PatientService patientService = new PatientService();
    private final TokenDAO tokenDAO = new TokenDAO();
    private final PatientRecordDAO patientRecordDAO = new PatientRecordDAO();
    private final NotificationService notificationService = new NotificationService();
    private final CrudService<Patient> polymorphicPatientService = patientService;
    private final ObservableList<Token> tokenList = FXCollections.observableArrayList();

    private FilteredList<Patient> patientFilter;
    private boolean sidebarCollapsed = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Stage stage = SceneManager.getPrimaryStage();
        if (stage != null) {
            stage.setResizable(true);
            // Keep window at current size, don't force maximize
        }

        Doctor doctor = SessionManager.getInstance().getCurrentDoctor();
        if (doctor != null) {
            String rawName = doctor.getName() != null ? doctor.getName().trim() : "";
            String cleanName = rawName.replaceFirst("(?i)^dr\\.?\\s*", "");
            welcomeLabel.setText("Welcome, Dr. " + (cleanName.isEmpty() ? rawName : cleanName));
        } else {
            welcomeLabel.setText("Welcome, Doctor");
        }
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")));

        setupPatientTable();
        setupTokenTable();
        setupNotifications();
        setupSearchFilter();
        setupTooltips();

        expandSidebar(false); // start expanded
        showSection(overviewSection, btnNavOverview);
        refreshPatients();
        refreshTokens();
        refreshStatsAndCharts();
    }

    private void setupPatientTable() {
        patientNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        patientAgeCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getAge())));
        patientGenderCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGender()));
        patientContactCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getContact()));
    }

    private void setupTokenTable() {
        tokenNumberCol.setCellValueFactory(data -> {
            Token t = data.getValue();
            int num = t.getTokenNumber() > 0 ? t.getTokenNumber() : t.getTokenId();
            return new SimpleStringProperty(String.valueOf(num));
        });
        tokenPatientCol.setCellValueFactory(data -> {
            Token t = data.getValue();
            String name = t.getPatientName();
            if (name == null || name.isBlank()) {
                name = resolvePatientName(t.getPatientId());
            }
            return new SimpleStringProperty(name);
        });
        tokenDeptCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSpecialization()));
        tokenProblemCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHealthProblem()));
        tokenRoomCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRoomNumber() != null ? data.getValue().getRoomNumber() : ""));
        tokenShiftCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getShift() != null ? data.getValue().getShift() : ""));
        tokenDateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getAppointmentDate() != null
                        ? data.getValue().getAppointmentDate().toString()
                        : (data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt().toLocalDate().toString() : "")));
        tokenTimeCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getAppointmentTime() != null
                        ? data.getValue().getAppointmentTime().toString()
                        : ""));
        tokenStatusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        tokenStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
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
    }

    private void setupNotifications() {
        notificationsList.setItems(notificationService.observableNotifications());
        notificationsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText("• " + item);
                    setWrapText(true);
                }
            }
        });
    }

    private void setupSearchFilter() {
        patientFilter = new FilteredList<>(patientService.observablePatients(), p -> true);
        searchField.textProperty().addListener((obs, old, text) -> {
            String term = text == null ? "" : text.toLowerCase();
            patientFilter.setPredicate(p ->
                    term.isBlank()
                    || p.getName().toLowerCase().contains(term)
                    || String.valueOf(p.getPatientId()).contains(term)
                    || (p.getContact() != null && p.getContact().toLowerCase().contains(term))
            );
        });
        SortedList<Patient> sorted = new SortedList<>(patientFilter);
        sorted.comparatorProperty().bind(patientTable.comparatorProperty());
        patientTable.setItems(sorted);
    }

    private void setupTooltips() {
        Tooltip.install(btnAddPatient, new Tooltip("Add a new patient record"));
        Tooltip.install(btnEditPatient, new Tooltip("Edit selected patient"));
        Tooltip.install(btnDeletePatient, new Tooltip("Delete selected patient"));
    }

    private void refreshPatients() {
        patientTable.refresh();
        applySort(patientTable, patientNameCol);
    }

    private void refreshTokens() {
        Doctor doctor = SessionManager.getInstance().getCurrentDoctor();
        if (doctor == null) {
            System.err.println("[DoctorDashboard] No doctor logged in, cannot refresh tokens");
            return;
        }
        
        String specialization = doctor.getSpecialization();
        int doctorId = doctor.getDoctorId();
        
        System.out.println("[DoctorDashboard] Refreshing tokens for specialization: '" + specialization + "', doctorId: " + doctorId);
        
        // Try to get tokens for the doctor's specialization
        List<Token> tokens = tokenDAO.getTokensForDoctor(specialization, doctorId);
        
        // If no tokens found with specialization, try getting all unassigned tokens
        if (tokens.isEmpty() && specialization != null && !specialization.isBlank()) {
            System.out.println("[DoctorDashboard] No tokens found with specialization filter, trying fallback...");
            // Fallback: get all tokens without specialization filter (for unassigned tokens)
            tokens = tokenDAO.getTokensForDoctor(null, doctorId);
        }
        
        tokenList.setAll(tokens);
        tokenTable.setItems(tokenList);
        tokenTable.refresh();
        applySort(tokenTable, tokenNumberCol);
        
        System.out.println("[DoctorDashboard] Loaded " + tokens.size() + " tokens");
        updateNextTokenLabel();
    }

    private void refreshStatsAndCharts() {
        long totalPatients = tokenList.stream().map(Token::getPatientId).distinct().count();
        long pending = tokenList.stream().filter(t -> "Pending".equals(t.getStatus())).count();
        long approved = tokenList.stream().filter(t -> "Approved".equals(t.getStatus())).count();
        long completed = tokenList.stream().filter(t -> "Completed".equals(t.getStatus())).count();
        long effectiveTotal = tokenList.stream().filter(t -> !"Cancelled".equals(t.getStatus())).count();

        totalPatientsValue.setText(String.valueOf(totalPatients));
        todayAppointmentsValue.setText(String.valueOf(pending));
        waitingTokensValue.setText(String.valueOf(approved));
        completionRateValue.setText(effectiveTotal == 0
                ? "0%"
                : Math.round((completed / (double) effectiveTotal) * 100) + "%");

        updateTokenBarChart();
        updatePatientPieChart();
    }

    private void updateTokenBarChart() {
        if (appointmentBarChart == null) return;
        
        appointmentBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        LocalDate start = LocalDate.now().minusDays(6);
        for (int i = 0; i < 7; i++) {
            LocalDate day = start.plusDays(i);
            long count = tokenList.stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().equals(day))
                    .count();
            series.getData().add(new XYChart.Data<>(day.getMonthValue() + "/" + day.getDayOfMonth(), count));
        }
        appointmentBarChart.getData().add(series);
    }

    private void updatePatientPieChart() {
        if (patientPieChart == null) return;
        
        patientPieChart.getData().clear();
        long male = patientService.list().stream().filter(p -> "Male".equalsIgnoreCase(p.getGender())).count();
        long female = patientService.list().stream().filter(p -> "Female".equalsIgnoreCase(p.getGender())).count();
        long other = patientService.list().size() - male - female;

        if (patientService.list().isEmpty()) return;

        patientPieChart.getData().addAll(
                new PieChart.Data("Male", male),
                new PieChart.Data("Female", female),
                new PieChart.Data("Other", other)
        );
    }

    private void updateNextTokenLabel() {
        tokenList.stream()
                .filter(t -> "Pending".equals(t.getStatus()) || "Approved".equals(t.getStatus()))
                .filter(t -> t.getCreatedAt() != null)
                .sorted(Comparator.comparing(Token::getCreatedAt))
                .findFirst()
                .ifPresentOrElse(t -> {
                    String text = "Token " + t.getTokenId() + " • " + resolvePatientName(t.getPatientId());
                    nextAppointmentLabel.setText(text);
                    if (nextAppointmentLabelOverview != null) nextAppointmentLabelOverview.setText(text);
                }, () -> {
                    nextAppointmentLabel.setText("No upcoming tokens");
                    if (nextAppointmentLabelOverview != null) nextAppointmentLabelOverview.setText("No upcoming tokens");
                });
    }

    private String resolvePatientName(int id) {
        Patient p = polymorphicPatientService.findById(id);
        return p != null ? p.getName() : "Patient #" + id;
    }
    private int currentDoctorId() {
        Doctor d = SessionManager.getInstance().getCurrentDoctor();
        return d != null ? d.getDoctorId() : 0;
    }

    private void showTokenMessage(String msg, boolean success) {
        if (tokenActionMessage != null) {
            tokenActionMessage.setText(msg);
            tokenActionMessage.getStyleClass().removeAll("error-label", "success-label");
            tokenActionMessage.getStyleClass().add(success ? "success-label" : "error-label");
        } else {
            showInfo(msg);
        }
    }

    @FXML
    private void handleAddPatient() {
        Patient p = showPatientDialog(null);
        if (p != null) {
            polymorphicPatientService.add(p); // polymorphism via CrudService reference
            notificationService.push("New patient added: " + p.getName());
            refreshPatients();
            refreshStatsAndCharts();
        }
    }

    @FXML
    private void handleEditPatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a patient to edit.");
            return;
        }
        Patient updated = showPatientDialog(selected);
        if (updated != null) {
            polymorphicPatientService.update(updated);
            refreshPatients();
            refreshStatsAndCharts();
        }
    }

    @FXML
    private void handleDeletePatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a patient to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + selected.getName() + " ?", ButtonType.OK, ButtonType.CANCEL);
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            polymorphicPatientService.delete(selected.getPatientId());
            refreshPatients();
            refreshStatsAndCharts();
        }
    }

    @FXML
    private void handleApproveToken() {
        Token selected = tokenTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showTokenMessage("Select a token to approve.", false); return; }
        if (!"Pending".equals(selected.getStatus())) {
            showTokenMessage("Only Pending tokens can be approved.", false); return;
        }
        int doctorId = currentDoctorId();
        boolean ok = tokenDAO.approveToken(selected.getTokenId(), doctorId);
        showTokenMessage(ok ? "Token approved and assigned." : "Could not approve token.", ok);
        if (ok) { refreshTokens(); refreshStatsAndCharts(); }
    }

    @FXML
    private void handleCompleteToken() {
        Token selected = tokenTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showTokenMessage("Select a token to complete.", false); return; }
        if (!"Approved".equals(selected.getStatus())) {
            showTokenMessage("Only Approved tokens can be completed.", false); return;
        }
        String diagnosis = taDiagnosis != null ? taDiagnosis.getText().trim() : "";
        if (diagnosis.isBlank()) {
            showTokenMessage("Diagnosis is required to complete a token.", false);
            return;
        }
        String meds  = taMedicines != null ? taMedicines.getText().trim() : "";
        String notes = taNotes != null ? taNotes.getText().trim() : "";

        int doctorId = currentDoctorId();
        boolean updated = tokenDAO.completeToken(selected.getTokenId(), doctorId);
        if (!updated) {
            showTokenMessage("Could not mark as completed. Try again.", false);
            return;
        }

        PatientRecord record = new PatientRecord();
        // Store the physician's diagnosis and prescription
        record.setProblem(diagnosis);      // maps to Report.diagnosis in DB
        record.setSolution(diagnosis);     // kept for UI binding
        record.setMedications(meds);       // maps to prescription column
        record.setNotes(notes);
        record.setPatientId(selected.getPatientId());
        record.setDoctorId(doctorId);
        record.setTokenId(selected.getTokenId());
        patientRecordDAO.addPatientRecord(record);

        if (taDiagnosis != null) taDiagnosis.clear();
        if (taMedicines != null) taMedicines.clear();
        if (taNotes != null) taNotes.clear();

        showTokenMessage("Token completed and patient record saved.", true);
        refreshTokens();
        refreshStatsAndCharts();
    }

    @FXML
    private void handleCancelToken() {
        Token selected = tokenTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showTokenMessage("Select a token to cancel.", false); return; }
        if ("Completed".equals(selected.getStatus()) || "Cancelled".equals(selected.getStatus())) {
            showTokenMessage("Completed/Cancelled tokens cannot be changed.", false); return;
        }
        boolean ok = tokenDAO.cancelTokenByDoctor(selected.getTokenId(), currentDoctorId());
        showTokenMessage(ok ? "Token cancelled." : "Could not cancel token.", ok);
        if (ok) { refreshTokens(); refreshStatsAndCharts(); }
    }

    @FXML
    private void handleUndoToken() {
        Token selected = tokenTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showTokenMessage("Select a cancelled token to undo.", false); return; }
        if (!"Cancelled".equals(selected.getStatus())) {
            showTokenMessage("Only cancelled tokens can be undone.", false); return;
        }
        boolean ok = tokenDAO.restoreCancelledToApproved(selected.getTokenId(), currentDoctorId());
        showTokenMessage(ok ? "Token restored to Approved." : "Could not undo cancel.", ok);
        if (ok) { refreshTokens(); refreshStatsAndCharts(); }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        SceneManager.switchScene("Login.fxml", "Login");
    }

    private Patient showPatientDialog(Patient base) {
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle(base == null ? "Add Patient" : "Edit Patient");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField   = new TextField(base != null ? base.getName() : "");
        TextField emailField  = new TextField(base != null ? base.getEmail() : "");
        TextField contactField= new TextField(base != null ? base.getContact() : "");
        TextField ageField    = new TextField(base != null ? String.valueOf(base.getAge()) : "");
        ComboBox<String> genderBox = new ComboBox<>(FXCollections.observableArrayList("Male", "Female", "Other"));
        genderBox.setValue(base != null ? base.getGender() : "Male");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Name"), nameField);
        grid.addRow(1, new Label("Email"), emailField);
        grid.addRow(2, new Label("Contact"), contactField);
        grid.addRow(3, new Label("Age"), ageField);
        grid.addRow(4, new Label("Gender"), genderBox);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Patient p = base == null ? new Patient() : base;
                p.setName(nameField.getText());
                p.setEmail(emailField.getText());
                p.setContact(contactField.getText());
                p.setAge(parseIntSafe(ageField.getText(), p.getAge()));
                p.setGender(genderBox.getValue());
                if (p.getPatientId() == 0) {
                    p.setPatientId(0); // auto id on add
                }
                return p;
            }
            return null;
        });

        Optional<Patient> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private int parseIntSafe(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    @FXML
    private void handleToggleSidebar() {
        // Keep sidebar persistent like patient dashboard
        expandSidebar(true);
    }

    @FXML
    private void handleNavOverview() {
        showSection(overviewSection, btnNavOverview);
    }

    @FXML
    private void handleNavPatients() {
        showSection(patientsSection, btnNavPatients);
    }

    @FXML
    private void handleNavAppointments() {
        showSection(appointmentsSection, btnNavAppointments);
    }

    @FXML
    private void handleNavSettings() {
        showInfo("Settings coming soon.");
    }

    private void collapseSidebar() { sidebarCollapsed = false; sidebar.setVisible(true); sidebar.setManaged(true); btnToggleSidebar.setText("?"); }

    private void expandSidebar(boolean focus) {
        sidebarCollapsed = false;
        sidebar.setVisible(true);
        sidebar.setManaged(true);
        btnToggleSidebar.setText("✕");
        if (focus) {
            btnToggleSidebar.requestFocus();
        }
    }

    private void showSection(VBox targetSection, Button activeButton) {
        overviewSection.setVisible(false);
        overviewSection.setManaged(false);
        patientsSection.setVisible(false);
        patientsSection.setManaged(false);
        appointmentsSection.setVisible(false);
        appointmentsSection.setManaged(false);

        if (targetSection != null) {
            targetSection.setVisible(true);
            targetSection.setManaged(true);
        }

        clearActiveNav();
        if (activeButton != null) {
            activeButton.getStyleClass().add("side-btn-active");
        }
    }

    private void clearActiveNav() {
        if (btnNavOverview != null)     btnNavOverview.getStyleClass().remove("side-btn-active");
        if (btnNavPatients != null)     btnNavPatients.getStyleClass().remove("side-btn-active");
        if (btnNavAppointments != null) btnNavAppointments.getStyleClass().remove("side-btn-active");
        // Settings button is optional in the current FXML, so guard against null.
        if (btnNavSettings != null)     btnNavSettings.getStyleClass().remove("side-btn-active");
    }

    private <T> void applySort(TableView<T> table, TableColumn<T, ?> col) {
        if (table == null || col == null) return;
        col.setSortType(TableColumn.SortType.ASCENDING);
        table.getSortOrder().setAll(col);
        table.sort();
    }
}
