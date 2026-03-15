package model;

import java.time.LocalDateTime;

/**
 * Appointment token connecting a patient, doctor, and schedule slot.
 */
public class Token {

    private int           tokenId;
    private int           tokenNumber;
    private String        healthProblem;
    private String        specialization;
    private String        status;        // Pending / Approved / Completed / Cancelled
    private LocalDateTime createdAt;
    private int           patientId;
    private Integer       doctorId;      // nullable until a doctor accepts
    private String        shift;
    private java.time.LocalDate appointmentDate;
    private java.time.LocalTime appointmentTime;
    private String        roomNumber;

    // Display-only fields joined from other tables
    private String patientName;
    private String doctorName;

    // Object references for UML relationships
    private Patient patient;
    private Doctor doctor;

    public int           getTokenId()        { return tokenId; }
    public int           getTokenNumber()    { return tokenNumber; }
    public String        getHealthProblem()  { return healthProblem; }
    public String        getSpecialization() { return specialization; }
    /** Alias when the UI labels the column as Department. */
    public String        getDepartment()     { return specialization; }
    public String        getStatus()         { return status; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public int           getPatientId()      { return patientId; }
    public Integer       getDoctorId()       { return doctorId; }
    public String        getShift()          { return shift; }
    public java.time.LocalDate getAppointmentDate() { return appointmentDate; }
    public java.time.LocalTime getAppointmentTime() { return appointmentTime; }
    public String        getRoomNumber()     { return roomNumber; }
    public String        getPatientName()    { return patientName; }
    public String        getDoctorName()     { return doctorName; }
    public Patient       getPatient()        { return patient; }
    public Doctor        getDoctor()         { return doctor; }

    public void setTokenId(int id)                 { this.tokenId        = id; }
    public void setTokenNumber(int num)            { this.tokenNumber    = num; }
    public void setHealthProblem(String problem)   { this.healthProblem  = problem; }
    public void setSpecialization(String spec)     { this.specialization = spec; }
    public void setDepartment(String dept)         { this.specialization = dept; }
    public void setStatus(String status)           { this.status         = status; }
    public void setCreatedAt(LocalDateTime dt)     { this.createdAt      = dt; }
    public void setPatientId(int pid)              { this.patientId      = pid; }
    public void setDoctorId(Integer did)           { this.doctorId       = did; }
    public void setShift(String shift)             { this.shift          = shift; }
    public void setAppointmentDate(java.time.LocalDate date) { this.appointmentDate = date; }
    public void setAppointmentTime(java.time.LocalTime time) { this.appointmentTime = time; }
    public void setRoomNumber(String room)         { this.roomNumber     = room; }
    public void setPatientName(String name)        { this.patientName    = name; }
    public void setDoctorName(String name)         { this.doctorName     = name; }
    public void setPatient(Patient patient) {
        this.patient = patient;
        if (patient != null) {
            this.patientId = patient.getPatientId();
            patient.addToken(this);
        }
    }
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        if (doctor != null) {
            this.doctorId = doctor.getDoctorId();
            doctor.addManagedToken(this);
        }
    }

    @Override
    public String toString() {
        return "Token#" + tokenNumber + " [" + status + "] - " + healthProblem;
    }
}
