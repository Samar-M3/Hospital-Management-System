package service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Appointment;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Simple appointment service backing the dashboard calendar and charts.
 */
public class AppointmentService implements CrudService<Appointment> {

    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private final AtomicInteger idSequence = new AtomicInteger(5000);

    public AppointmentService() {
        seed();
    }

    @Override
    public Appointment add(Appointment appointment) {
        if (appointment.getAppointmentId() == 0) {
            appointment.setAppointmentId(idSequence.incrementAndGet());
        }
        appointments.add(appointment);
        return appointment;
    }

    @Override
    public boolean update(Appointment appointment) {
        int idx = indexOf(appointment.getAppointmentId());
        if (idx == -1) return false;
        appointments.set(idx, appointment);
        return true;
    }

    @Override
    public boolean delete(int id) {
        int idx = indexOf(id);
        if (idx == -1) return false;
        appointments.remove(idx);
        return true;
    }

    @Override
    public List<Appointment> list() {
        return appointments;
    }

    @Override
    public Appointment findById(int id) {
        return appointments.stream()
                .filter(a -> a.getAppointmentId() == id)
                .findFirst()
                .orElse(null);
    }

    public ObservableList<Appointment> observableAppointments() {
        return appointments;
    }

    public List<Appointment> forDate(LocalDate date) {
        return appointments.stream()
                .filter(a -> a.getDate().equals(date))
                .collect(Collectors.toList());
    }

    private int indexOf(int id) {
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getAppointmentId() == id) return i;
        }
        return -1;
    }

    private void seed() {
        LocalDate today = LocalDate.now();
        appointments.add(new Appointment(idSequence.incrementAndGet(), 1001, 3001, today,             java.time.LocalTime.of(9, 30),  "Follow-up: BP check", Appointment.AppointmentType.IN_PERSON, Appointment.Status.SCHEDULED));
        appointments.add(new Appointment(idSequence.incrementAndGet(), 1003, 3001, today,             java.time.LocalTime.of(11, 15), "Review lab reports",   Appointment.AppointmentType.VIRTUAL,   Appointment.Status.SCHEDULED));
        appointments.add(new Appointment(idSequence.incrementAndGet(), 1002, 3001, today.plusDays(1), java.time.LocalTime.of(10, 0),  "New consultation",     Appointment.AppointmentType.IN_PERSON, Appointment.Status.SCHEDULED));
        appointments.add(new Appointment(idSequence.incrementAndGet(), 1002, 3001, today.minusDays(1), java.time.LocalTime.of(14, 0), "Post-op check",        Appointment.AppointmentType.IN_PERSON, Appointment.Status.COMPLETED));
    }
}
