package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class SetupLoginSelectionPage {

    private final DatabaseHelper databaseHelper;

    public SetupLoginSelectionPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        Button setupButton = new Button("SetUp");
        Button loginButton = new Button("Login");

        setupButton.setOnAction(a -> {
            new SetupAccountPage(databaseHelper).show(primaryStage);
        });
        loginButton.setOnAction(a -> {
            new UserLoginPage(databaseHelper, (role, username) -> { 
                openHomePage(primaryStage, databaseHelper, role, username);
            }).show(primaryStage);
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(setupButton, loginButton);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }

    // ✅ Updated to include `loggedInUser`
    private void openHomePage(Stage primaryStage, DatabaseHelper databaseHelper, RoleManager.Role role, String loggedInUser) {
        switch (role) {
            case ADMIN:
                new AdminHomePage(primaryStage, databaseHelper, loggedInUser).show();
                break;
            case INSTRUCTOR:
                new InstructorHomePage(primaryStage, databaseHelper, loggedInUser).show();
                break;
            case STAFF:
                new StaffHomePage(primaryStage, databaseHelper, loggedInUser).show();
                break;
            default:
                new UserHomePage(primaryStage, databaseHelper, loggedInUser).show();
                break;
        }
    }
}
