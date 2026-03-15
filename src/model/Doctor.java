package model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a Doctor in the system.
 * Demonstrates inheritance by extending the abstract User class.
 */
public class Doctor extends User {

    private String specialization;
    /** Legacy string view of assigned shifts (e.g. "Morning" or "Evening"). */
    private String shifts;
    private int    departmentId;
    private String departmentName;  // joined from Department table (for display)
    private String password;        // BCrypt hash

    /** UML-friendly association: a doctor can be assigned to one or more shifts. */
    private List<Shift> assignedShifts = new ArrayList<>();
    /** Tokens this doctor manages (pending/approved/completed). */
    private final List<Token> managedTokens = new ArrayList<>();
    /** Patient records authored by this doctor. */
    private final List<PatientRecord> patientRecords = new ArrayList<>();

    public Doctor() {
        super();
    }

    public Doctor(int doctorId, String name, String specialization, String shifts) {
        super(doctorId, name, null, null);
        this.specialization = specialization;
        setShifts(shifts);
    }

    // Getters
    public int    getDoctorId()       { return getId(); }
    public String getSpecialization() { return specialization; }
    public String getShifts()         { return shifts; }
    public int    getDepartmentId()   { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getPassword()       { return password; }
    public List<Shift> getAssignedShifts() { return assignedShifts; }
    public List<Token> getManagedTokens() { return managedTokens; }
    public List<PatientRecord> getPatientRecords() { return patientRecords; }

    // Setters
    public void setDoctorId(int id)              { setId(id); }
    public void setSpecialization(String spec)   { this.specialization = spec; }
    /**
     * Maintains the legacy string while keeping the list of Shift objects in sync.
     */
    public void setShifts(String shifts) {
        this.shifts = shifts;
        this.assignedShifts.clear();
        if (shifts != null && !shifts.isBlank()) {
            for (String name : shifts.split(",")) {
                String trimmed = name.trim();
                if (!trimmed.isEmpty()) {
                    Shift shift = new Shift(trimmed);
                    shift.addDoctor(this);
                    this.assignedShifts.add(shift);
                }
            }
        }
    }
    public void setDepartmentId(int deptId)      { this.departmentId   = deptId; }
    public void setDepartmentName(String dName)  { this.departmentName = dName; }
    public void setPassword(String password)     { this.password       = password; }
    public void setAssignedShifts(List<Shift> shifts) {
        this.assignedShifts = shifts != null ? new ArrayList<>(shifts) : new ArrayList<>();
        this.assignedShifts.forEach(s -> s.addDoctor(this));
        this.shifts = this.assignedShifts.stream()
                .map(Shift::getName)
                .collect(Collectors.joining(", "));
    }
    public void addManagedToken(Token token) { if (token != null && !managedTokens.contains(token)) managedTokens.add(token); }
    public void addPatientRecord(PatientRecord record) { if (record != null && !patientRecords.contains(record)) patientRecords.add(record); }

    @Override
    public String toString() {
        return "Dr. " + getName() + " (" + specialization + ")";
    }
}
