Smart Hospital Management System
================================

Project Overview
----------------
A Java + JavaFX application that streamlines core hospital operations. Patients can book tokens (appointments), doctors manage their queues and records, and admins oversee users, shifts, and analytics. The system keeps schedules organized, routes tokens to the right doctors, and stores visit records for future reference.

Features
--------
- Patient token booking with specialization and shift selection
- Doctor dashboard for queue management and visit completion
- Appointment/token lifecycle management (pending → approved → completed/cancelled)
- Shift management for doctor availability
- Patient medical record tracking (visit history, notes, medications)
- Role-based access: Admin, Doctor, Patient

System Classes
--------------
- **User**: Abstract base for shared identity fields (id, name, email, contact).
- **Patient**: Extends User; books tokens and owns medical records.
- **Doctor**: Extends User; manages tokens, creates patient records, has assigned shifts.
- **Admin**: Extends User; manages doctors, shifts, and system data.
- **Token**: Represents a booked slot linking a patient, doctor, shift, and appointment timing.
- **Shift**: Captures a named work period; doctors can be assigned to multiple shifts.
- **PatientRecord**: Doctor-authored visit record (diagnosis, medications, notes) tied to a patient/token.

Class Relationships
-------------------
- Patient, Doctor, Admin inherit from User.
- Patient ↔ Token: a patient can have many tokens; each token belongs to one patient.
- Doctor ↔ Token: a doctor can manage many tokens; each token is assigned to one doctor.
- Patient ↔ PatientRecord: a patient can have many records; each record belongs to one patient.
- Doctor ↔ PatientRecord: a doctor can create/manage many records.
- Doctor ↔ Shift: a doctor can be assigned to one or more shifts; each shift can include multiple doctors.
- Admin manages doctors and their shifts.

Technology Stack
----------------
- Java 17+ (or compatible JDK)
- JavaFX (controls, FXML)
- MySQL (via `mysql-connector-j` in `lib/`) for persistence

Project Structure
-----------------
```
SHMS/
├─ src/
│  ├─ controller/   # JavaFX controllers
│  ├─ model/        # Domain models and DAOs
│  ├─ service/      # In-memory/services utilities
│  ├─ ui/           # FXML views and styles
│  └─ util/         # Helpers (DB, sessions, scene management)
├─ lib/             # JavaFX + MySQL connector jars
├─ bin/             # Compiled classes (optional)
└─ README.md
```

How to Run
----------
1. Ensure Java 17+ and JavaFX SDK are available (or use the bundled jars in `lib/`).
2. Compile:
   ```bash
   javac --module-path lib --add-modules javafx.controls,javafx.fxml \
         -d bin src/App.java $(find src -name "*.java")
   ```
3. Run:
   ```bash
   java --module-path lib --add-modules javafx.controls,javafx.fxml \
        -cp bin App
   ```
   Adjust paths if running on Windows (use `;` instead of `:` for classpath).
4. Database: start MySQL and update credentials in `src/util/DBConnection.java` if needed.

Future Improvements
-------------------
- Online/self-service booking with email/SMS confirmations
- Push notifications to patients and doctors for status changes
- Rich analytics dashboard with trends and resource utilization
- Role-based audit logs and fine-grained permissions
- Integration with external EHR/LIS systems



