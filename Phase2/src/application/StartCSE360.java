package application;

import javafx.application.Application;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class StartCSE360 extends Application {

    private static final DatabaseHelper databaseHelper = new DatabaseHelper();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            databaseHelper.connectToDatabase();  // Connect to the database

            // Always start with SetupLoginSelectionPage
            SetupLoginSelectionPage setupLoginSelectionPage = new SetupLoginSelectionPage(databaseHelper);
            setupLoginSelectionPage.show(primaryStage);
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
