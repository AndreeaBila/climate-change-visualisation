package aib;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


/**
 * Starts  the application
 */
public class Main extends Application {

    /** The application menu */
    public static Menu mainMenu;

    /**
     * The start method that sets and displays the application window
     * @param primaryStage The application stage
     */
    @Override
    public void start(Stage primaryStage) {
        // Create new menu
        mainMenu = new Menu();
        HBox root = mainMenu.buildWindow();

        // Application bar title
        primaryStage.setTitle("Climate Change Visualisation");

        // Create the window
        Scene scene = new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        primaryStage.setScene(scene);

        // Add css style for menu
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Set the application icon
        Image image = new Image("/bee.png");

        primaryStage.getIcons().add(image);

        // Set window to maximizes, i.e. fullscreen windowed
        primaryStage.setMaximized(true);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(400);
        primaryStage.show();
    }

    /**
     * Main method that launches the application
     * @param args The application arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
