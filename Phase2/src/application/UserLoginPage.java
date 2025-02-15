package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import databasePart1.DatabaseHelper;

public class UserLoginPage {

    private final DatabaseHelper databaseHelper;
    private final BiConsumer<RoleManager.Role, String> loginSuccessCallback; // ✅ Accept Role & Username

    public UserLoginPage(DatabaseHelper databaseHelper, BiConsumer<RoleManager.Role, String> callback) {
        this.databaseHelper = databaseHelper;
        this.loginSuccessCallback = callback;
    }

    public void show(Stage primaryStage) {
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button loginButton = new Button("Login");

        loginButton.setOnAction(a -> {
            String userName = userNameField.getText();
            String password = passwordField.getText();

            try {
                User user = new User(userName, password, "");
                String roleString = databaseHelper.getUserRole(userName); // ✅ Fetch assigned roles

                if (roleString != null) {
                    user.setRole(roleString);
                    if (databaseHelper.login(user)) {
                        // ✅ Convert roleString to a list of roles
                        List<String> roles = Arrays.stream(roleString.split(","))
                                .map(String::trim)
                                .filter(r -> !r.isEmpty())
                                .collect(Collectors.toList());

                        System.out.println("User roles: " + roles); // Debugging output

                        if (roles.size() == 1) {
                            // ✅ If only one role, navigate directly
                            RoleManager.Role userRole = RoleManager.Role.valueOf(roles.get(0).toUpperCase());
                            loginSuccessCallback.accept(userRole, userName);
                        } else {
                            // ✅ If multiple roles, prompt user to select one
                            selectRoleScreen(roles, userName, primaryStage);
                        }
                    } else {
                        errorLabel.setText("Error logging in");
                    }
                } else {
                    errorLabel.setText("User account doesn't exist");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
                errorLabel.setText("Database error occurred.");
            }
        });

        VBox layout = new VBox(10, userNameField, passwordField, loginButton, errorLabel);
        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }

    // ✅ Show a selection dialog for users with multiple roles
    private void selectRoleScreen(List<String> roles, String userName, Stage primaryStage) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(roles.get(0), roles);
        dialog.setTitle("Select Role");
        dialog.setHeaderText("Multiple Roles Found");
        dialog.setContentText("Select the role you want to use:");

        dialog.showAndWait().ifPresent(selectedRole -> {
            RoleManager.Role userRole = RoleManager.Role.valueOf(selectedRole.toUpperCase());
            loginSuccessCallback.accept(userRole, userName); // ✅ Pass Role & Username
        });
    }
}
