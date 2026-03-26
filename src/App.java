import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import util.SceneManager;
import util.DBConnection;
/**
 * JavaFX entry point for the Smart Hospital Management System.
 */
public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Register the window so controllers can change scenes
        SceneManager.setStage(primaryStage);

        primaryStage.setTitle("SHMS - Smart Hospital Management System");
        primaryStage.setResizable(true);
        primaryStage.setWidth(1100);
        primaryStage.setHeight(700);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((screenBounds.getWidth() - 1100) / 2);
        primaryStage.setY((screenBounds.getHeight() - 700) / 2);

        // Show the login screen first
        SceneManager.switchScene("Login.fxml", "Login");
    }

    @Override
    public void stop() {
        DBConnection.getInstance().close();
        System.out.println("[App] Application closed cleanly.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
