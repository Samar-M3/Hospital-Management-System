package service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Patient;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory patient service implementing CrudService.
 * Encapsulates data and offers observable collections for UI binding.
 */
public class PatientService implements CrudService<Patient> {

    private final ObservableList<Patient> patients = FXCollections.observableArrayList();
    private final AtomicInteger idSequence = new AtomicInteger(2000);

    public PatientService() {
        seed();
    }

    @Override
    public Patient add(Patient patient) {
        if (patient.getPatientId() == 0) {
            patient.setPatientId(idSequence.incrementAndGet());
        }
        patients.add(patient);
        return patient;
    }

    @Override
    public boolean update(Patient patient) {
        int idx = indexOf(patient.getPatientId());
        if (idx == -1) return false;
        patients.set(idx, patient);
        return true;
    }

    @Override
    public boolean delete(int id) {
        int idx = indexOf(id);
        if (idx == -1) return false;
        patients.remove(idx);
        return true;
    }

    @Override
    public List<Patient> list() {
        return patients;
    }

    @Override
    public Patient findById(int id) {
        return patients.stream().filter(p -> p.getPatientId() == id).findFirst().orElse(null);
    }

    public ObservableList<Patient> observablePatients() {
        return patients;
    }

    private int indexOf(int id) {
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getPatientId() == id) return i;
        }
        return -1;
    }

    private void seed() {
        Patient a = new Patient(1001, "Sarah Chen", "sarah.chen@example.com");
        a.setAge(32); a.setGender("Female"); a.setContact("+977-9812345678");

        Patient b = new Patient(1002, "Ravi Patel", "ravi.patel@example.com");
        b.setAge(45); b.setGender("Male"); b.setContact("+977-9800123123");

        Patient c = new Patient(1003, "Emma Wilson", "emma.wilson@example.com");
        c.setAge(27); c.setGender("Female"); c.setContact("+977-9845678900");

        patients.addAll(a, b, c);
    }
}
