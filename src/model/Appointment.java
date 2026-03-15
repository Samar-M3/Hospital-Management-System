package model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Appointment domain object used by the dashboard.
 * Encapsulates scheduling data and offers derived helpers.
 */
public class Appointment {

    public enum AppointmentType { IN_PERSON, VIRTUAL }
    public enum Status { SCHEDULED, CHECKED_IN, COMPLETED, CANCELLED }

    private int              appointmentId;
    private int              patientId;
    private int              doctorId;
    private LocalDate        date;
    private LocalTime        time;
    private String           reason;
    private AppointmentType  type;
    private Status           status;

    public Appointment() {
    }

    public Appointment(int appointmentId, int patientId, int doctorId,
                       LocalDate date, LocalTime time, String reason,
                       AppointmentType type, Status status) {
        this.appointmentId = appointmentId;
        this.patientId     = patientId;
        this.doctorId      = doctorId;
        this.date          = date;
        this.time          = time;
        this.reason        = reason;
        this.type          = type;
        this.status        = status;
    }

    // Getters
    public int getAppointmentId()     { return appointmentId; }
    public int getPatientId()         { return patientId; }
    public int getDoctorId()          { return doctorId; }
    public LocalDate getDate()        { return date; }
    public LocalTime getTime()        { return time; }
    public String getReason()         { return reason; }
    public AppointmentType getType()  { return type; }
    public Status getStatus()         { return status; }

    // Setters
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public void setPatientId(int patientId)         { this.patientId     = patientId; }
    public void setDoctorId(int doctorId)           { this.doctorId      = doctorId; }
    public void setDate(LocalDate date)             { this.date          = date; }
    public void setTime(LocalTime time)             { this.time          = time; }
    public void setReason(String reason)            { this.reason        = reason; }
    public void setType(AppointmentType type)       { this.type          = type; }
    public void setStatus(Status status)            { this.status        = status; }

    public String getDisplaySlot() {
        return time + " — " + reason;
    }
}
