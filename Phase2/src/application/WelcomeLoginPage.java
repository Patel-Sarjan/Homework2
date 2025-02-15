package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
	
	private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    private void navigateToRoleScreen(String role, User user, Stage primaryStage) {
        System.out.println("User selected role: " + role); // Debugging output
        String loggedInUser = user.getUserName(); // ✅ Get logged-in username

        if (role.equalsIgnoreCase("admin")) {
            new AdminHomePage(primaryStage, databaseHelper, loggedInUser).show(); // ✅ Pass username
        } else if (role.equalsIgnoreCase("user")) {
            new UserHomePage(primaryStage, databaseHelper, loggedInUser).show(); // ✅ Pass username
        } else if (role.equalsIgnoreCase("staff")) {
            new StaffHomePage(primaryStage, databaseHelper, loggedInUser).show(); // ✅ Pass username
        } else if (role.equalsIgnoreCase("instructor")) {
            new InstructorHomePage(primaryStage, databaseHelper, loggedInUser).show(); // ✅ Pass username
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unknown role selected!", ButtonType.OK);
            alert.showAndWait();
        }
    }

    
    private void selectRoleScreen(List<String> roles, User user, Stage primaryStage) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(roles.get(0), roles);
        dialog.setTitle("Select Role");
        dialog.setHeaderText("Multiple Roles Found");
        dialog.setContentText("Select the role you want to use:");

        dialog.showAndWait().ifPresent(selectedRole -> navigateToRoleScreen(selectedRole, user, primaryStage));
    }


    
    
    public void show( Stage primaryStage, User user) {
    	
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label welcomeLabel = new Label("Welcome!!");
	    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    // Button to navigate to the user's respective page based on their role
	    Button continueButton = new Button("Continue to your Page");
	    continueButton.setOnAction(a -> {
	        // Split the role string into a list
	        List<String> roles = Arrays.stream(user.getRole().split(","))
	                                   .map(String::trim)
	                                   .filter(r -> !r.isEmpty())
	                                   .collect(Collectors.toList());

	        // Debugging output to check parsed roles
	        System.out.println("Parsed roles: " + roles);

	        if (roles.size() == 1) {
	            // If only one role exists, go to that page
	            navigateToRoleScreen(roles.get(0), user, primaryStage);
	        } else {
	            // If multiple roles exist, prompt user to select one
	            selectRoleScreen(roles, user, primaryStage);
	        }
	    });

	    
	    // Button to quit the application
	    Button quitButton = new Button("Quit");
	    quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });
	    
	    // "Invite" button for admin to generate invitation codes
	    if ("admin".equals(user.getRole())) {
            Button inviteButton = new Button("Invite");
            inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage);
            });
            layout.getChildren().add(inviteButton);
        }

	    layout.getChildren().addAll(welcomeLabel,continueButton,quitButton);
	    Scene welcomeScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(welcomeScene);
	    primaryStage.setTitle("Welcome Page");
    }
}