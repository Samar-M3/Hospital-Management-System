package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a work shift (Morning/Day/Evening/Night) that one or more doctors can be assigned to.
 * Keeps associations in-memory so the existing string-based scheduling logic remains untouched.
 */
public class Shift {

    private int shiftId;
    private String name;
    private final List<Doctor> doctors = new ArrayList<>();

    public Shift() {}

    public Shift(String name) {
        this.name = name;
    }

    public Shift(int shiftId, String name) {
        this.shiftId = shiftId;
        this.name = name;
    }

    public int getShiftId() { return shiftId; }
    public void setShiftId(int shiftId) { this.shiftId = shiftId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Doctor> getDoctors() { return doctors; }

    /** Assigns a doctor to this shift (no-op for nulls). */
    public void addDoctor(Doctor doctor) {
        if (doctor != null && !doctors.contains(doctor)) {
            doctors.add(doctor);
        }
    }

    public void removeDoctor(Doctor doctor) {
        doctors.remove(doctor);
    }

    @Override
    public String toString() {
        return name != null ? name : "Shift";
    }
}
