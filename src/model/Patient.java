package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Patient in the system.
 * Inherits common identity fields from User (inheritance).
 */
public class Patient extends User {

    private int    age;
    private String gender;
    private String password;   // BCrypt hash

    // UML associations
    private final List<Token> tokens = new ArrayList<>();
    private final List<PatientRecord> patientRecords = new ArrayList<>();

    public Patient() {
        super();
    }

    public Patient(int patientId, String name, String email) {
        super(patientId, name, email, null);
    }

    // Getters
    public int    getPatientId() { return getId(); }
    public int    getAge()       { return age; }
    public String getGender()    { return gender; }
    public String getPassword()  { return password; }
    public List<Token> getTokens() { return tokens; }
    public List<PatientRecord> getPatientRecords() { return patientRecords; }

    // Setters
    public void setPatientId(int id)       { setId(id); }
    public void setAge(int age)            { this.age    = age; }
    public void setGender(String gender)   { this.gender = gender; }
    public void setPassword(String password) { this.password = password; }
    public void addToken(Token token) { if (token != null && !tokens.contains(token)) tokens.add(token); }
    public void addPatientRecord(PatientRecord record) { if (record != null && !patientRecords.contains(record)) patientRecords.add(record); }

    @Override
    public String toString() {
        return getName() + " (Patient #" + getId() + ")";
    }
}
