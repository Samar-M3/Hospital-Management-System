package model;

import java.time.LocalDateTime;

/**
 * model/PatientRecord.java
 * Represents a medical record written by a doctor after a consultation.
 * Maps to the existing Report table to preserve current persistence logic.
 */
public class PatientRecord {

    private int           reportId;
    private String        problem;
    private String        solution;
    private String        medications;
    private String        notes;
    private int           patientId;
    private int           doctorId;
    private Integer       tokenId;
    private LocalDateTime createdAt;

    // Display fields — joined from Patient/Doctor tables
    private String patientName;
    private String doctorName;

    // UML associations
    private Patient patient;
    private Doctor doctor;

    public PatientRecord() {}

    // — Getters —
    public int           getReportId()    { return reportId; }
    /** Alias to emphasize UML naming. */
    public int           getRecordId()    { return reportId; }
    public String        getProblem()     { return problem; }
    public String        getSolution()    { return solution; }
    public String        getMedications() { return medications; }
    public String        getNotes()       { return notes; }
    public int           getPatientId()   { return patientId; }
    public int           getDoctorId()    { return doctorId; }
    public Integer       getTokenId()     { return tokenId; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public String        getPatientName() { return patientName; }
    public String        getDoctorName()  { return doctorName; }
    public Patient       getPatient()     { return patient; }
    public Doctor        getDoctor()      { return doctor; }

    // — Setters —
    public void setReportId(int id)             { this.reportId    = id; }
    public void setRecordId(int id)             { this.reportId    = id; }
    public void setProblem(String problem)       { this.problem     = problem; }
    public void setSolution(String solution)     { this.solution    = solution; }
    public void setMedications(String meds)      { this.medications = meds; }
    public void setNotes(String notes)           { this.notes       = notes; }
    public void setPatientId(int pid)            { this.patientId   = pid; }
    public void setDoctorId(int did)             { this.doctorId    = did; }
    public void setTokenId(Integer tid)          { this.tokenId     = tid; }
    public void setCreatedAt(LocalDateTime dt)   { this.createdAt   = dt; }
    public void setPatientName(String name)      { this.patientName = name; }
    public void setDoctorName(String name)       { this.doctorName  = name; }
    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.patientId = patient.getPatientId();
            patient.addPatientRecord(this);
        }
    }
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        if (doctor != null) {
            this.doctorId = doctor.getDoctorId();
            doctor.addPatientRecord(this);
        }
    }

    @Override
    public String toString() {
        return "PatientRecord{id=" + reportId + ", token=" + tokenId + ", problem='" + problem + "', by=Dr." + doctorName + "}";
    }
}
