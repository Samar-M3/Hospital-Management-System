package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Central place to swap JavaFX scenes and access the primary stage.
 */
public class SceneManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /** Alias for setPrimaryStage used by App. */
    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/ui/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            var css = SceneManager.class.getResource("/ui/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            primaryStage.setScene(scene);
            primaryStage.setFullScreen(false);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("[SceneManager] Failed to load FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }

    public static void switchScene(String fxmlFile, String title) {
        switchScene(fxmlFile);
        primaryStage.setTitle(title);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
