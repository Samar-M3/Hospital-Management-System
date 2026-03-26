package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import model.*;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblDate;
    @FXML private Label lblPatientCount;
    @FXML private Label lblDoctorCount;
    @FXML private Label lblTokenCount;

    @FXML private VBox sidebar;
    @FXML private Button btnToggleSidebar;
    @FXML private Button btnNavOverview;
    @FXML private Button btnNavPatients;
    @FXML private Button btnNavDoctors;
    @FXML private Button btnNavAnalytics;

    @FXML private VBox sectionOverview;
    @FXML private VBox sectionPatients;
    @FXML private VBox sectionDoctors;
    @FXML private VBox sectionAnalytics;

    // Overview tokens
    @FXML private TableView<Token> tableTokens;
    @FXML private TableColumn<Token,Integer> colTokenNumber;
    @FXML private TableColumn<Token,String>  colTokenPatient;
    @FXML private TableColumn<Token,String>  colTokenSpec;
    @FXML private TableColumn<Token,String>  colTokenDoctor;
    @FXML private TableColumn<Token,String>  colTokenStatus;
    @FXML private TableColumn<Token,java.time.LocalDateTime> colTokenCreated;

    // Patients
    @FXML private TableView<Patient> tablePatients;
    @FXML private TableColumn<Patient,Integer> colPatientId;
    @FXML private TableColumn<Patient,String>  colPatientName;
    @FXML private TableColumn<Patient,String>  colPatientEmail;
    @FXML private TableColumn<Patient,String>  colPatientPhone;
    @FXML private TableColumn<Patient,String>  colPatientGender;
    @FXML private TextField tfPatientName;
    @FXML private TextField tfPatientEmail;
    @FXML private TextField tfPatientPhone;
    @FXML private TextField tfPatientAge;
    @FXML private ComboBox<String> cbPatientGender;
    @FXML private Label lblPatientMsg;

    // Doctors
    @FXML private TableView<Doctor> tableDoctors;
    @FXML private TableColumn<Doctor,Integer> colDoctorId;
    @FXML private TableColumn<Doctor,String>  colDoctorName;
    @FXML private TableColumn<Doctor,String>  colSpecialization;
    @FXML private TableColumn<Doctor,String>  colShifts;
    @FXML private TableColumn<Doctor,String>  colDepartment;
    @FXML private TableColumn<Doctor,String>  colEmail;
    @FXML private TextField tfDoctorName;
    @FXML private TextField tfSpecialization;
    @FXML private ComboBox<String> cbShift;
    @FXML private TextField tfDepartment;
    @FXML private TextField tfEmail;
    @FXML private PasswordField pfPassword;
    @FXML private Label lblDoctorMsg;

    // Analytics
    @FXML private BarChart<String, Number> barRevenueByDept;

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final TokenDAO tokenDAO = new TokenDAO();
    private final ObservableList<Doctor> doctorList = FXCollections.observableArrayList();
    private Patient selectedPatient;
    private Doctor selectedDoctor;
    private boolean sidebarCollapsed = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Admin admin = SessionManager.getInstance().getCurrentAdmin();
        lblWelcome.setText(admin != null ? "Welcome, " + admin.getName() : "Admin");
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")));

        setupTables();
        cbShift.setItems(FXCollections.observableArrayList("Morning", "Day", "Evening", "Night"));

        loadPatients();
        loadDoctors();
        loadTokens();
        loadAnalytics();
        refreshCounts();

        applySort(tablePatients, colPatientId);
        applySort(tableDoctors, colDoctorId);
        applySort(tableTokens, colTokenNumber);

        showSection(sectionOverview, btnNavOverview);
        expandSidebar(false);
    }

    private void setupTables() {
        colTokenNumber.setCellValueFactory(new PropertyValueFactory<>("tokenNumber"));
        colTokenPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colTokenSpec.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colTokenDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colTokenStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTokenStatus.setCellFactory(col -> new TableCell<>() {
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
        colTokenCreated.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colPatientId.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPatientEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPatientPhone.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colPatientGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        if (cbPatientGender != null) {
            cbPatientGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        }

        tablePatients.getSelectionModel().selectedItemProperty().addListener((obs, o, p) -> {
            selectedPatient = p;
            if (p != null) {
                tfPatientName.setText(p.getName());
                tfPatientEmail.setText(p.getEmail());
                tfPatientPhone.setText(p.getContact());
                tfPatientAge.setText(String.valueOf(p.getAge()));
                cbPatientGender.setValue(p.getGender());
            }
        });

        colDoctorId.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        colDoctorName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colShifts.setCellValueFactory(new PropertyValueFactory<>("shifts"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableDoctors.setItems(doctorList);

        tableDoctors.getSelectionModel().selectedItemProperty().addListener((obs, o, d) -> {
            selectedDoctor = d;
            if (d != null) {
                tfDoctorName.setText(d.getName());
                tfSpecialization.setText(d.getSpecialization());
                cbShift.setValue(d.getShifts());
                tfDepartment.setText(d.getDepartmentName() != null ? d.getDepartmentName() : String.valueOf(d.getDepartmentId()));
                tfEmail.setText(d.getEmail());
            }
        });
    }

    // Data loading
    @FXML private void loadPatients() {
        tablePatients.setItems(FXCollections.observableArrayList(patientDAO.getAllPatients()));
        tablePatients.getSelectionModel().clearSelection();
        applySort(tablePatients, colPatientId);
        clearPatientForm();
        refreshCounts();
    }
    @FXML private void loadDoctors() {
        doctorList.setAll(doctorDAO.getAllDoctors());
        applySort(tableDoctors, colDoctorId);
        refreshCounts();
    }
    @FXML private void loadTokens() {
        tableTokens.setItems(FXCollections.observableArrayList(tokenDAO.getAllTokens()));
        applySort(tableTokens, colTokenNumber);
        refreshCounts();
    }

    @FXML
    private void handleUpdatePatient() {
        Patient p = selectedPatient;
        if (p == null) { showPatientMsg("Select a patient to update.", false); return; }
        Patient updated = buildPatientFromForm(p.getPatientId());
        if (updated == null) return;
        boolean ok = patientDAO.updatePatient(updated);
        if (ok) {
            loadPatients();
            showPatientMsg("Patient updated.", true);
        } else {
            showPatientMsg("Update failed.", false);
        }
    }

    @FXML
    private void handleRemovePatient() {
        Patient p = selectedPatient;
        if (p == null) { showPatientMsg("Select a patient to remove.", false); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Remove " + p.getName() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean ok = patientDAO.removePatient(p.getPatientId());
                if (ok) {
                    loadPatients();
                    showPatientMsg("Patient removed.", true);
                } else {
                    showPatientMsg("Removal failed.", false);
                }
            }
        });
    }

    private Patient buildPatientFromForm(int id) {
        String name  = tfPatientName.getText().trim();
        String email = tfPatientEmail.getText().trim();
        String phone = tfPatientPhone.getText().trim();
        String gender= cbPatientGender.getValue();
        int age      = parseDeptId(tfPatientAge.getText()); // reuse safe int parser

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || gender == null) {
            showPatientMsg("Fill all patient fields.", false);
            return null;
        }
        Patient p = new Patient();
        p.setPatientId(id);
        p.setName(name);
        p.setEmail(email);
        p.setContact(phone);
        p.setGender(gender);
        p.setAge(age);
        return p;
    }

    private void clearPatientForm() {
        selectedPatient = null;
        if (tfPatientName != null) tfPatientName.clear();
        if (tfPatientEmail != null) tfPatientEmail.clear();
        if (tfPatientPhone != null) tfPatientPhone.clear();
        if (tfPatientAge != null) tfPatientAge.clear();
        if (cbPatientGender != null) cbPatientGender.setValue(null);
    }

    private void refreshCounts() {
        lblPatientCount.setText(String.valueOf(tablePatients.getItems().size()));
        lblDoctorCount.setText(String.valueOf(tableDoctors.getItems().size()));
        lblTokenCount.setText(String.valueOf(tableTokens.getItems().size()));
    }

    private void loadAnalytics() {
        List<Token> tokens = tokenDAO.getAllTokens();
        loadRevenue(tokens);
    }

    private void loadRevenue(List<Token> tokens) {
        if (barRevenueByDept == null) return;
        barRevenueByDept.getData().clear();
        Map<String, Integer> rates = defaultRates();
        Map<String, Double> revenue = new HashMap<>();
        tokens.stream()
                .filter(t -> "Completed".equalsIgnoreCase(t.getStatus()))
                .forEach(t -> {
                    String dept = normalizeSpec(t.getSpecialization());
                    double charge = rates.getOrDefault(dept, 500);
                    revenue.merge(dept, charge, Double::sum);
                });
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        revenue.forEach((dept, sum) -> series.getData().add(new XYChart.Data<>(dept, sum)));
        barRevenueByDept.getData().add(series);
    }

    private String normalizeSpec(String spec) {
        if (spec == null || spec.isBlank()) return "General";
        return spec.trim();
    }

    private Map<String, Integer> defaultRates() {
        Map<String, Integer> rates = new HashMap<>();
        rates.put("ENT", 500);
        rates.put("Cardiologist", 1500);
        rates.put("Dermatologist", 600);
        rates.put("Ophthalmologist", 700);
        rates.put("General Medicine", 400);
        rates.put("Neurology", 1800);
        rates.put("Orthopedics", 1200);
        rates.put("Pediatrics", 800);
        rates.put("Gynecology", 900);
        rates.put("Urology", 1000);
        return rates;
    }

    // Doctor CRUD
    @FXML
    private void handleAddDoctor() {
        Doctor d = buildDoctorFromForm();
        if (d == null) return;
        if (doctorDAO.emailExists(d.getEmail())) { showDoctorMsg("Email already exists.", false); return; }
        boolean ok = doctorDAO.addDoctor(d);
        showDoctorMsg(ok ? "Doctor created." : "Create failed.", ok);
        if (ok) {
            clearForm();
            loadDoctors();
            // Highlight the newest entry for immediate feedback
            if (!doctorList.isEmpty()) {
                tableDoctors.getSelectionModel().selectLast();
                tableDoctors.scrollTo(doctorList.size() - 1);
            }
        }
    }

    @FXML
    private void handleUpdateDoctor() {
        if (selectedDoctor == null) { showDoctorMsg("Select a doctor to update.", false); return; }
        Doctor d = buildDoctorFromForm();
        if (d == null) return;
        d.setDoctorId(selectedDoctor.getDoctorId());
        if (d.getDepartmentId() == 0 && selectedDoctor.getDepartmentId() != 0) {
            d.setDepartmentId(selectedDoctor.getDepartmentId());
        }
        if (d.getDepartmentName() == null || d.getDepartmentName().isBlank()) {
            d.setDepartmentName(selectedDoctor.getDepartmentName());
        }
        boolean ok = doctorDAO.updateDoctor(d);
        showDoctorMsg(ok ? "Doctor updated." : "Update failed.", ok);
        if (ok) { clearForm(); loadDoctors(); }
    }

    @FXML
    private void handleRemoveDoctor() {
        if (selectedDoctor == null) { showDoctorMsg("Select a doctor to remove.", false); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Remove Dr. " + selectedDoctor.getName() + "?", ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean ok = doctorDAO.removeDoctor(selectedDoctor.getDoctorId());
                showDoctorMsg(ok ? "Doctor removed." : "Removal failed.", ok);
                if (ok) { clearForm(); loadDoctors(); }
            }
        });
    }

    private Doctor buildDoctorFromForm() {
        String name = tfDoctorName.getText().trim();
        String spec = tfSpecialization.getText().trim();
        String shift = cbShift.getValue();
        String dept = tfDepartment.getText().trim();
        String email = tfEmail.getText().trim();
        String pass = pfPassword.getText();
        if (name.isEmpty() || spec.isEmpty() || shift == null || email.isEmpty()) {
            showDoctorMsg("Fill all fields.", false); return null;
        }
        int deptId = parseDeptId(dept);
        Doctor d = new Doctor();
        d.setName(name); d.setSpecialization(spec); d.setShifts(shift); d.setEmail(email); d.setPassword(pass);
        d.setDepartmentId(deptId);
        d.setDepartmentName(dept.isBlank() ? (deptId == 0 ? "" : String.valueOf(deptId)) : dept);
        return d;
    }

    private void clearForm() {
        tfDoctorName.clear(); tfSpecialization.clear(); cbShift.setValue(null); tfDepartment.clear(); tfEmail.clear(); pfPassword.clear();
        selectedDoctor = null; lblDoctorMsg.setText("");
        tableDoctors.getSelectionModel().clearSelection();
    }

    private int parseDeptId(String value) {
        if (value == null || value.isBlank()) return 0;
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private <T> void applySort(TableView<T> table, TableColumn<T, ?> col) {
        if (table == null || col == null) return;
        col.setSortType(TableColumn.SortType.ASCENDING);
        table.getSortOrder().setAll(col);
        table.sort();
    }

    // Navigation
    @FXML private void handleToggleSidebar() { if (sidebarCollapsed) expandSidebar(true); else collapseSidebar(); }
    @FXML private void handleNavOverview()   { showSection(sectionOverview, btnNavOverview); }
    @FXML private void handleNavPatients()   { showSection(sectionPatients, btnNavPatients); }
    @FXML private void handleNavDoctors()    { showSection(sectionDoctors, btnNavDoctors); }
    @FXML private void handleNavAnalytics()  { showSection(sectionAnalytics, btnNavAnalytics); loadAnalytics(); }

    private void showSection(VBox target, Button activeBtn) {
        sectionOverview.setVisible(false); sectionOverview.setManaged(false);
        sectionPatients.setVisible(false); sectionPatients.setManaged(false);
        sectionDoctors.setVisible(false);  sectionDoctors.setManaged(false);
        sectionAnalytics.setVisible(false);sectionAnalytics.setManaged(false);
        target.setVisible(true); target.setManaged(true);
        clearActiveNav(); activeBtn.getStyleClass().add("side-btn-active");
    }

    private void clearActiveNav() {
        btnNavOverview.getStyleClass().remove("side-btn-active");
        btnNavPatients.getStyleClass().remove("side-btn-active");
        btnNavDoctors.getStyleClass().remove("side-btn-active");
        btnNavAnalytics.getStyleClass().remove("side-btn-active");
    }

    private void collapseSidebar() {
        sidebarCollapsed = true;
        sidebar.setVisible(false); sidebar.setManaged(false);
        btnToggleSidebar.setText("☰");
    }
    private void expandSidebar(boolean focus) {
        sidebarCollapsed = false;
        sidebar.setVisible(true); sidebar.setManaged(true);
        btnToggleSidebar.setText("✕");
        if (focus) btnToggleSidebar.requestFocus();
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        SceneManager.switchScene("Login.fxml", "Login");
    }

    private void showPatientMsg(String msg, boolean ok) {
        if (lblPatientMsg != null) {
            lblPatientMsg.setText(msg);
            lblPatientMsg.getStyleClass().removeAll("error-label","success-label");
            lblPatientMsg.getStyleClass().add(ok ? "success-label" : "error-label");
        }
    }

    private void showDoctorMsg(String msg, boolean ok) {
        lblDoctorMsg.setText(msg);
        lblDoctorMsg.getStyleClass().removeAll("error-label","success-label");
        lblDoctorMsg.getStyleClass().add(ok ? "success-label" : "error-label");
    }
}
