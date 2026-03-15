package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin user who manages doctors and shifts.
 */
public class Admin extends User {

    private String password;   // BCrypt hash

    // Associations
    private final List<Doctor> managedDoctors = new ArrayList<>();
    private final List<Shift> managedShifts = new ArrayList<>();

    public Admin() {
        super();
    }

    public Admin(int adminId, String name, String email) {
        super(adminId, name, email, null);
    }

    public int    getAdminId()  { return getId(); }
    public String getPassword() { return password; }
    public List<Doctor> getManagedDoctors() { return managedDoctors; }
    public List<Shift> getManagedShifts() { return managedShifts; }

    public void setAdminId(int id)      { setId(id); }
    public void setPassword(String pass){ this.password = pass; }
    public void manageDoctor(Doctor doctor) { if (doctor != null && !managedDoctors.contains(doctor)) managedDoctors.add(doctor); }
    public void manageShift(Shift shift) { if (shift != null && !managedShifts.contains(shift)) managedShifts.add(shift); }

    @Override
    public String toString() {
        return "Admin: " + getName() + " (" + getEmail() + ")";
    }
}
