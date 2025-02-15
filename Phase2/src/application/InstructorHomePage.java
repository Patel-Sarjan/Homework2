package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class InstructorHomePage {
    private final Stage primaryStage;
    private final DatabaseHelper databaseHelper;
    private final String loggedInUser; // ✅ Store logged-in user's username

    public InstructorHomePage(Stage primaryStage, DatabaseHelper databaseHelper, String loggedInUser) {
        this.primaryStage = primaryStage;
        this.databaseHelper = databaseHelper;
        this.loggedInUser = loggedInUser; // ✅ Initialize username
    }

    public void show() {
        Button questionPageButton = new Button("Go to Questions Page");

        // ✅ Pass loggedInUser to QuestionPageController
        questionPageButton.setOnAction(e -> {
            new QuestionPageController(RoleManager.Role.INSTRUCTOR, loggedInUser, databaseHelper).showQuestionPage(primaryStage);
        });

        // ✅ Logout Button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage); // Redirect to login page
        });

        VBox layout = new VBox(15);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(questionPageButton, logoutButton);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Instructor Home Page");
        primaryStage.show();
    }
}
