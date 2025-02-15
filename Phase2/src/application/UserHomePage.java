package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class UserHomePage {

    private final DatabaseHelper databaseHelper;
    private final Stage primaryStage;
    private final String loggedInUser; // ✅ Store the logged-in user's username

    // Constructor accepting Stage, DatabaseHelper, and logged-in username
    public UserHomePage(Stage primaryStage, DatabaseHelper databaseHelper, String loggedInUser) {
        this.primaryStage = primaryStage;
        this.databaseHelper = databaseHelper;
        this.loggedInUser = loggedInUser; // ✅ Assign the logged-in username
    }

    public void show() {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Label to display "Hello, User!"
        Label userLabel = new Label("Hello, " + loggedInUser + "!"); // ✅ Display username
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Button to go to Questions Page
        Button questionPageButton = new Button("Go to Questions Page");
        questionPageButton.setOnAction(event -> {
            new QuestionPageController(RoleManager.Role.USER, loggedInUser, databaseHelper).showQuestionPage(primaryStage);
        });

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage); // Redirect to login page
        });

        // Add components to the layout
        layout.getChildren().addAll(userLabel, questionPageButton, logoutButton);

        // Create scene and set it to primary stage
        Scene userScene = new Scene(layout, 800, 400);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("User Page");
        primaryStage.show();
    }
}
