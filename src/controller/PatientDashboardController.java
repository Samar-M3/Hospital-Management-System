package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Patient;
import model.PatientRecord;
import model.PatientRecordDAO;
import model.Token;
import model.TokenDAO;
import util.SceneManager;
import util.SessionManager;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Patient dashboard with sidebar navigation and stacked sections.
 */
public class PatientDashboardController implements Initializable {

    @FXML private Label lblWelcomeNav;
    @FXML private Label lblWelcomeTitle;
    @FXML private Label lblSubtitle;
    @FXML private Label lblDate;
    @FXML private Label lblUpcomingCount;
    @FXML private Label lblCompletedCount;
    @FXML private Label lblReportCount;
    @FXML private Label lblNextAppointment;

    // sidebar + sections
    @FXML private VBox sidebar;
    @FXML private Button btnToggleSidebar;
    @FXML private Button btnNavOverview;
    @FXML private Button btnNavBook;
    @FXML private Button btnNavTokens;
    @FXML private Button btnNavReports;
    @FXML private VBox sectionOverview;
    @FXML private VBox sectionBook;
    @FXML private VBox sectionTokens;
    @FXML private VBox sectionReports;

    // booking
    @FXML private TextField tfPatientName;
    @FXML private TextField tfPatientAge;
    @FXML private TextField tfPatientGender;
    @FXML private ComboBox<String> cbProblemType;
    @FXML private TextArea taProblem;
    @FXML private ToggleGroup shiftToggle;
    @FXML private RadioButton rbMorning;
    @FXML private RadioButton rbDay;
    @FXML private RadioButton rbEvening;
    @FXML private Label lblCrowdLevel;
    @FXML private Label lblBestTime;
    @FXML private Label lblBookMessage;

    // tokens
    @FXML private VBox tokenCardContainer;
    @FXML private Label lblTokenMessage;

    // reports
    @FXML private VBox reportCardContainer;
    @FXML private Label lblReportCountHeader;
    @FXML private TableView<PatientRecord> tableReports;
    @FXML private TableColumn<PatientRecord,String> colReportProblem;
    @FXML private TableColumn<PatientRecord,String> colReportSolution;
    @FXML private TableColumn<PatientRecord,String> colReportNotes;
    @FXML private TableColumn<PatientRecord,String> colReportMeds;
    @FXML private TableColumn<PatientRecord,String> colReportDoctor;
    @FXML private TableColumn<PatientRecord,java.time.LocalDateTime> colReportDate;
    @FXML private Label lblReportMessage;

    private final TokenDAO tokenDAO   = new TokenDAO();
    private final PatientRecordDAO patientRecordDAO = new PatientRecordDAO();
    private final ObservableList<String> problemOptions = FXCollections.observableArrayList();
    private boolean sidebarCollapsed = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Patient p = SessionManager.getInstance().getCurrentPatient();
        String name = p != null ? p.getName() : "Patient";
        lblWelcomeNav.setText("Welcome, " + name);
        lblWelcomeTitle.setText("Welcome, " + name + " 👋");
        lblSubtitle.setText("Stay updated with your appointments and medical reports.");
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")));

        if (p != null) {
            tfPatientName.setText(p.getName());
            tfPatientAge.setText(String.valueOf(p.getAge()));
            tfPatientGender.setText(p.getGender());
        }
        populateProblemTypes();
        enableProblemSearch();
        setupShiftSelector();
        setupReportTable();

        loadTokens();
        loadReports();
        updateStats();
        showSection(sectionOverview, btnNavOverview);
        expandSidebar(false);
    }

    private void setupReportTable() {
        // Table columns are no longer used - reports are now displayed as cards
        // Adding null check for backward compatibility
        if (colReportProblem == null) return;
        
        colReportProblem.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("problem"));
        colReportSolution.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("solution"));
        if (colReportNotes != null) {
            colReportNotes.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("notes"));
        }
        colReportMeds.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("medications"));
        colReportDoctor.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("doctorName"));
        colReportDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("createdAt"));
        colReportDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(java.time.LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) { setText(null); return; }
                setText(value != null ? value.toLocalDate().toString() : "");
            }
        });
    }

    private void populateProblemTypes() {
        if (cbProblemType == null) return;
        cbProblemType.setEditable(true);
        problemOptions.setAll(
                "Brain and Nerves (Neurology/Neurosurgery)",
                "Eyes (Ophthalmology)",
                "Ears (Otolaryngology/ENT)",
                "Nose and Sinuses (Otolaryngology/ENT)",
                "Mouth, Teeth, and Jaw (Dental/Maxillofacial Surgery)",
                "Throat and Vocal Cords (Otolaryngology/ENT)",
                "Thyroid and Glands (Endocrinology)",
                "Heart (Cardiology/Cardiovascular Surgery)",
                "Arteries and Veins (Vascular Surgery)",
                "Lungs and Airways (Pulmonology/Thoracic Surgery)",
                "Esophagus (Gastroenterology)",
                "Stomach (Gastroenterology)",
                "Liver and Gallbladder (Hepatology/General Surgery)",
                "Pancreas (Gastroenterology/Endocrinology)",
                "Small and Large Intestines/Colon (Gastroenterology/Colorectal Surgery)",
                "Appendix (General Surgery)",
                "Kidneys (Nephrology)",
                "Bladder and Urinary Tract (Urology)",
                "Male Reproductive Organs (Urology)",
                "Female Reproductive Organs (Gynecology/Obstetrics)",
                "Bones and Joints (Orthopedics)",
                "Spine and Spinal Cord (Orthopedics/Neurosurgery)",
                "Muscles, Ligaments, and Tendons (Orthopedics/Sports Medicine)",
                "Skin, Hair, and Nails (Dermatology)",
                "Blood (Hematology)",
                "Immune System (Immunology/Rheumatology)",
                "Breast Tissue (Oncology/General Surgery)"
        );
        cbProblemType.getItems().setAll(problemOptions);
    }

    private void enableProblemSearch() {
        if (cbProblemType == null) return;
        cbProblemType.getEditor().textProperty().addListener((obs, old, val) -> {
            String term = val == null ? "" : val.toLowerCase();
            cbProblemType.getItems().setAll(problemOptions.filtered(opt -> opt.toLowerCase().contains(term)));
            cbProblemType.show();
        });
    }

    private void setupShiftSelector() {
        if (shiftToggle == null) return;
        shiftToggle.selectedToggleProperty().addListener((obs, old, val) -> {
            if (val instanceof RadioButton rb) updateShiftHints(rb.getText());
        });
        if (rbMorning != null) {
            rbMorning.setSelected(true);
            updateShiftHints(rbMorning.getText());
        }
    }

    private void updateShiftHints(String shift) {
        String crowd;
        String window;
        switch (shift) {
            case "Morning" -> { crowd = "Crowd Level: Medium"; window = "Recommended Time: 9:30 AM – 10:30 AM"; }
            case "Evening" -> { crowd = "Crowd Level: Low"; window = "Recommended Time: 6:00 PM – 7:00 PM"; }
            default -> { crowd = "Crowd Level: High"; window = "Recommended Time: 2:00 PM – 3:00 PM"; }
        }
        if (lblCrowdLevel != null) lblCrowdLevel.setText(crowd);
        if (lblBestTime != null) lblBestTime.setText(window);
    }

    private void loadTokens() {
        Patient p = SessionManager.getInstance().getCurrentPatient();
        if (p == null) return;
        List<Token> tokens = tokenDAO.getTokensByPatient(p.getPatientId());
        long pendingOrApproved = tokens.stream()
                .filter(t -> "Pending".equals(t.getStatus()) || "Approved".equals(t.getStatus()))
                .count();
        long completed = tokens.stream().filter(t -> "Completed".equals(t.getStatus())).count();
        lblUpcomingCount.setText(String.valueOf(pendingOrApproved));
        lblCompletedCount.setText(String.valueOf(completed));
        updateNextAppointment(tokens);

        if (tokenCardContainer != null) {
            tokenCardContainer.getChildren().clear();
            tokens.stream()
                    .sorted(Comparator.comparing(Token::getCreatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .forEach(t -> tokenCardContainer.getChildren().add(buildTokenCard(t, p)));
            lblTokenMessage.setText(tokens.isEmpty() ? "No tokens yet." : "");
        }
    }

    private void loadReports() {
        Patient p = SessionManager.getInstance().getCurrentPatient();
        if (p == null) return;
        List<PatientRecord> reports = patientRecordDAO.getPatientRecordsByPatient(p.getPatientId());
        
        // Update the header count
        lblReportCountHeader.setText(reports.size() + " report" + (reports.size() != 1 ? "s" : ""));
        lblReportCount.setText(String.valueOf(reports.size()));
        
        // Clear existing cards
        reportCardContainer.getChildren().clear();
        
        if (reports.isEmpty()) {
            lblReportMessage.setText("Reports appear only after the doctor completes your token.");
            return;
        }
        
        lblReportMessage.setText("");
        
        // Create cards for each report
        for (PatientRecord report : reports) {
            VBox card = createReportCard(report);
            reportCardContainer.getChildren().add(card);
        }
    }
    
    private VBox createReportCard(PatientRecord report) {
        VBox card = new VBox();
        card.getStyleClass().add("report-card");
        
        // Header with doctor info and date
        HBox header = new HBox();
        header.getStyleClass().add("report-header");
        VBox docInfo = new VBox();
        docInfo.getStyleClass().add("report-doc-info");
        Label docName = new Label("👨‍⚕️ Dr. " + (report.getDoctorName() != null ? report.getDoctorName() : "Unknown"));
        docName.getStyleClass().add("report-doc-name");
        String dateStr = report.getCreatedAt() != null ? 
            report.getCreatedAt().toLocalDate().toString() : "N/A";
        Label date = new Label("📅 " + dateStr);
        date.getStyleClass().add("report-date");
        docInfo.getChildren().addAll(docName, date);
        
        // Add icon
        Label icon = new Label("🏥");
        icon.setStyle("-fx-font-size: 24px;");
        
        header.getChildren().addAll(icon, docInfo);
        card.getChildren().add(header);
        
        // Problem section
        if (report.getProblem() != null && !report.getProblem().isEmpty()) {
            VBox problemSection = createReportSection("PROBLEM", report.getProblem(), "report-section-problem");
            card.getChildren().add(problemSection);
        }
        
        // Solution/Diagnosis section
        if (report.getSolution() != null && !report.getSolution().isEmpty()) {
            VBox solutionSection = createReportSection("DIAGNOSIS / SOLUTION", report.getSolution(), "report-section-solution");
            card.getChildren().add(solutionSection);
        }
        
        // Medications section
        if (report.getMedications() != null && !report.getMedications().isEmpty()) {
            VBox medsSection = createReportSection("💊 MEDICATIONS", report.getMedications(), "report-section-meds");
            card.getChildren().add(medsSection);
        }
        
        // Notes section
        if (report.getNotes() != null && !report.getNotes().isEmpty()) {
            VBox notesSection = createReportSection("📝 DOCTOR NOTES", report.getNotes(), "report-section-notes");
            card.getChildren().add(notesSection);
        }
        
        return card;
    }
    
    private VBox createReportSection(String labelText, String valueText, String styleClass) {
        VBox section = new VBox();
        section.getStyleClass().add("report-section");
        if (styleClass != null && !styleClass.isEmpty()) {
            section.getStyleClass().add(styleClass);
        }
        
        Label label = new Label(labelText);
        label.getStyleClass().add("report-label");
        
        Label value = new Label(valueText);
        value.getStyleClass().add("report-value");
        
        section.getChildren().addAll(label, value);
        return section;
    }

    private void updateNextAppointment(List<Token> tokens) {
        Optional<Token> next = tokens.stream()
                .filter(t -> "Pending".equals(t.getStatus()) || "Approved".equals(t.getStatus()))
                .sorted(Comparator.comparing(this::tokenScheduledDateTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .findFirst();
        if (next.isPresent()) {
            Token t = next.get();
            int displayNum = t.getTokenNumber() > 0 ? t.getTokenNumber() : t.getTokenId();
            lblNextAppointment.setText("Token " + displayNum + " • " + t.getSpecialization() + " • " +
                    formatDate(tokenScheduledDateTime(t)));
        } else {
            lblNextAppointment.setText("No upcoming tokens");
        }
    }

    private void updateStats() {
        // stats derived in loadTokens/loadReports
    }

    // ——— Actions ————————————————————————————————————————————————
    @FXML
    private void handleCreateToken() {
        Patient p = SessionManager.getInstance().getCurrentPatient();
        if (p == null) { showBookMsg("Not logged in.", false); return; }
        String problemType = cbProblemType != null && cbProblemType.getEditor() != null
                ? cbProblemType.getEditor().getText().trim()
                : "";
        String notes = taProblem != null ? taProblem.getText().trim() : "";
        String shift = getSelectedShift();
        LocalDate apptDate = LocalDate.now();
        LocalTime apptTime = bestTimeForShift(shift);
        String department = problemType.isEmpty() ? "General Medicine" : problemType;
        String room = mapDepartmentToRoom(department);
        String healthProblem = problemType.isEmpty() ? notes : (notes.isEmpty() ? problemType : problemType + " - " + notes);

        Token created = tokenDAO.createToken(p.getPatientId(), healthProblem, department, shift, apptDate, apptTime, room);
        if (created != null) {
            showBookMsg("Token ID " + created.getTokenId() + " created for " + created.getSpecialization() + ". Status: Pending.", true);
            handleResetForm();
            loadTokens();
        } else {
            showBookMsg("Failed to create token. Try again.", false);
        }
    }

    @FXML
    private void handleCancelToken() {
        showTokenMsg("Tokens cannot be modified after creation. Please contact your doctor to cancel.", false);
    }

    @FXML private void refreshTokens() { loadTokens(); }

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        SceneManager.switchScene("Login.fxml", "Login");
    }

    // ——— Navigation ——————————————————————————————————————————————
    @FXML private void handleToggleSidebar() {
        if (sidebarCollapsed) expandSidebar(true); else collapseSidebar();
    }
    @FXML private void handleNavOverview()   { showSection(sectionOverview, btnNavOverview); }
    @FXML private void handleNavBook()       { showSection(sectionBook, btnNavBook); }
    @FXML private void handleNavTokens()     { showSection(sectionTokens, btnNavTokens); }
    @FXML private void handleNavReports()    { showSection(sectionReports, btnNavReports); }

    private void showSection(VBox target, Button activeBtn) {
        sectionOverview.setVisible(false); sectionOverview.setManaged(false);
        sectionBook.setVisible(false);     sectionBook.setManaged(false);
        sectionTokens.setVisible(false);   sectionTokens.setManaged(false);
        sectionReports.setVisible(false);  sectionReports.setManaged(false);

        target.setVisible(true); target.setManaged(true);
        clearActiveNav();
        activeBtn.getStyleClass().add("side-btn-active");
    }

    private void clearActiveNav() {
        btnNavOverview.getStyleClass().remove("side-btn-active");
        btnNavBook.getStyleClass().remove("side-btn-active");
        btnNavTokens.getStyleClass().remove("side-btn-active");
        btnNavReports.getStyleClass().remove("side-btn-active");
    }

    private void collapseSidebar() {
        sidebarCollapsed = true;
        sidebar.setVisible(false);
        sidebar.setManaged(false);
        btnToggleSidebar.setText("☼");
    }

    private void expandSidebar(boolean focus) {
        sidebarCollapsed = false;
        sidebar.setVisible(true);
        sidebar.setManaged(true);
        btnToggleSidebar.setText("✕");
        if (focus) btnToggleSidebar.requestFocus();
    }

    // ——— Helpers ————————————————————————————————————————————————
    private LocalDateTime tokenScheduledDateTime(Token t) {
        LocalDate date = t.getAppointmentDate() != null
                ? t.getAppointmentDate()
                : (t.getCreatedAt() != null ? t.getCreatedAt().toLocalDate() : LocalDate.now());
        LocalTime time = t.getAppointmentTime() != null ? t.getAppointmentTime() : bestTimeForShift(t.getShift());
        return LocalDateTime.of(date, time != null ? time : LocalTime.NOON);
    }

    private LocalTime bestTimeForShift(String shift) {
        return switch (shift == null ? "" : shift) {
            case "Morning" -> LocalTime.of(9, 30);
            case "Evening" -> LocalTime.of(18, 0);
            default -> LocalTime.of(14, 0);
        };
    }

    private String getSelectedShift() {
        if (shiftToggle != null && shiftToggle.getSelectedToggle() instanceof RadioButton rb) {
            return rb.getText();
        }
        return "Day";
    }

    private String mapDepartmentToRoom(String dept) {
        if (dept == null) return "101";
        if (dept.contains("Ortho")) return "204";
        if (dept.contains("Cardio") || dept.contains("Heart")) return "305";
        if (dept.contains("Neuro") || dept.contains("Brain")) return "310";
        if (dept.contains("ENT") || dept.contains("Ear") || dept.contains("Nose") || dept.contains("Throat")) return "112";
        if (dept.contains("Derm")) return "118";
        return "101";
    }

    private VBox buildTokenCard(Token t, Patient p) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color:white; -fx-border-color:#D8DCE8; -fx-border-radius:12; -fx-background-radius:12; -fx-padding:12 16;");

        String patientName = t.getPatientName() != null ? t.getPatientName() : (p != null ? p.getName() : "Patient");
        String age = p != null ? String.valueOf(p.getAge()) : "";
        String gender = p != null ? p.getGender() : "";
        String dept = t.getSpecialization() != null ? t.getSpecialization() : "General Medicine";
        String problem = t.getHealthProblem() != null ? t.getHealthProblem() : "";
        String shift = t.getShift() != null ? t.getShift() : "Day";
        String room = t.getRoomNumber() != null ? t.getRoomNumber() : mapDepartmentToRoom(dept);
        LocalDateTime scheduled = tokenScheduledDateTime(t);
        int displayNum = t.getTokenNumber() > 0 ? t.getTokenNumber() : t.getTokenId();

        Label title = new Label("TOKEN #" + displayNum);
        title.getStyleClass().add("content-title");
        Label patientLine = new Label("Patient: " + patientName + "   Age: " + age + "   Gender: " + gender);
        Label problemLine = new Label("Problem: " + problem);
        Label deptLine = new Label("Department: " + dept + "   Room: " + room);
        Label shiftLine = new Label("Shift: " + shift + "   Date: " + formatDate(scheduled) + "   Time: " + formatTime(scheduled));
        Label statusLine = new Label("Status: " + t.getStatus());
        statusLine.setStyle("-fx-font-weight:700; -fx-text-fill:" + statusColour(t.getStatus()));

        card.getChildren().addAll(title, patientLine, problemLine, deptLine, shiftLine, statusLine);
        return card;
    }

    private String formatDate(LocalDateTime dt) {
        if (dt == null) return "-";
        return dt.toLocalDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
    }

    private String formatTime(LocalDateTime dt) {
        if (dt == null) return "-";
        return dt.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a"));
    }

    private String statusColour(String status) {
        return switch (status) {
            case "Pending" -> "#F59E0B";
            case "Approved" -> "#2563EB";
            case "Completed" -> "#22C55E";
            case "Cancelled" -> "#EF4444";
            default -> "#334155";
        };
    }

    @FXML
    private void handleResetForm() {
        if (cbProblemType != null) {
            cbProblemType.getSelectionModel().clearSelection();
            cbProblemType.getEditor().clear();
        }
        if (taProblem != null) taProblem.clear();
        if (shiftToggle != null && rbMorning != null) {
            shiftToggle.selectToggle(rbMorning);
            updateShiftHints(rbMorning.getText());
        } else {
            updateShiftHints("Day");
        }
    }

    private void showBookMsg(String msg, boolean ok) {
        lblBookMessage.setText(msg);
        lblBookMessage.getStyleClass().removeAll("error-label","success-label");
        lblBookMessage.getStyleClass().add(ok ? "success-label" : "error-label");
    }

    private void showTokenMsg(String msg, boolean ok) {
        lblTokenMessage.setText(msg);
        lblTokenMessage.getStyleClass().removeAll("error-label","success-label");
        lblTokenMessage.getStyleClass().add(ok ? "success-label" : "error-label");
    }
}
