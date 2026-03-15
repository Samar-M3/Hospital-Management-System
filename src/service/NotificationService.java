package service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Lightweight notification feed for the dashboard.
 */
public class NotificationService {

    private final ObservableList<String> notifications = FXCollections.observableArrayList(
            "Lab result ready for Sarah Chen",
            "Ravi Patel checked in for 9:30 AM",
            "Inventory: Amoxicillin stock low",
            "New message from Admin: Department meeting at 4 PM"
    );

    public ObservableList<String> observableNotifications() {
        return notifications;
    }

    public void push(String notification) {
        notifications.add(0, notification);
    }
}
