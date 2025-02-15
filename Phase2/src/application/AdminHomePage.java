package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminHomePage {
    private final DatabaseHelper databaseHelper;
    private final Stage primaryStage;
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final String loggedInUser; // ✅ Store logged-in user's username
    public AdminHomePage(Stage primaryStage, DatabaseHelper databaseHelper, String loggedInUser) {
        this.primaryStage = primaryStage;
        this.databaseHelper = databaseHelper;
        this.loggedInUser = loggedInUser; // ✅ Initialize username
    }
    public void show() {
        TableView<User> tableView = new TableView<>();

        // Adding a column for displaying user names
        TableColumn<User, String> userColumn = new TableColumn<>("User");
        userColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserName()));
        userColumn.setPrefWidth(200);

        // Adding a column for displaying user roles
        TableColumn<User, String> roleColumn = new TableColumn<>("Roles");
        roleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRole()));
        roleColumn.setPrefWidth(250);

        // Adding a column with buttons to assign multiple roles
        TableColumn<User, Void> actionColumn = new TableColumn<>("Assign Roles");
        actionColumn.setPrefWidth(250);
        actionColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final List<String> roles = Arrays.asList("staff", "instructor", "admin", "user");
            private final HBox buttonBox = new HBox(5);

            {
                for (String role : roles) {
                    Button button = new Button(role);
                    button.setStyle("-fx-background-color: lightblue;");
                    button.setOnAction(event -> toggleRole(getTableView().getItems().get(getIndex()), role));
                    buttonBox.getChildren().add(button);
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });

        // Adding a delete button to remove a user
        TableColumn<User, Void> deleteColumn = new TableColumn<>("Delete");
        deleteColumn.setPrefWidth(100);
        deleteColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setStyle("-fx-background-color: lightcoral;");
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    long adminCount = users.stream().filter(u -> u.getRole().contains("admin")).count();

                    if (adminCount <= 1 && user.getRole().contains("admin")) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot delete the last admin.", ButtonType.OK);
                        alert.showAndWait();
                        return;
                    }

                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this user?", ButtonType.YES, ButtonType.NO);
                    confirmDialog.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            deleteUser(user);
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        // Adding all columns to the table
        tableView.getColumns().addAll(userColumn, roleColumn, actionColumn, deleteColumn);
        tableView.setItems(users);
        loadUsers();

     // ✅ Ensure loggedInUser is passed to QuestionPageController
        Button questionPageButton = new Button("Go to Questions Page");
        questionPageButton.setOnAction(event -> {
            new QuestionPageController(RoleManager.Role.ADMIN, loggedInUser, databaseHelper).showQuestionPage(primaryStage);
        });


        // Back to Login Button
        Button backButton = new Button("Back to Login");
        backButton.setOnAction(event -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        // Logout Button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        VBox layout = new VBox(10, tableView, questionPageButton, backButton, logoutButton);
        layout.setSpacing(10);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Page");
        primaryStage.show();
    }

    // Method to load users from the database
    private void loadUsers() {
        users.clear();
        try (ResultSet rs = databaseHelper.getAllUsers()) {
            while (rs.next()) {
                users.add(new User(rs.getString("userName"), "", rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to toggle user roles when clicking a button
    private void toggleRole(User user, String role) {
        List<String> roles = Arrays.asList(user.getRole().split(","));
        roles = roles.stream().map(String::trim).filter(r -> !r.isEmpty()).collect(Collectors.toList());

        if (roles.contains(role)) {
            roles.remove(role);
        } else {
            roles.add(role);
        }

        if (roles.isEmpty()) {
            databaseHelper.deleteUser(user.getUserName());
            users.remove(user);
            return;
        }

        if (roles.contains("admin")) {
            roles.remove("admin");
            roles.add(0, "admin"); // Ensure at least one admin exists
        }

        String newRole = String.join(",", roles);
        databaseHelper.updateUserRole(user.getUserName(), newRole);
        user.setRole(newRole);
        users.set(users.indexOf(user), user);
    }

    // Method to delete a user from the database
    private void deleteUser(User user) {
        databaseHelper.deleteUser(user.getUserName());
        users.remove(user);
    }
}
