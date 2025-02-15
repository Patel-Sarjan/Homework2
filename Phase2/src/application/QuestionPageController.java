package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuestionPageController {
	private final String currentUsername; // ✅ Store the logged-in username
    private final RoleManager.Role userRole;
    private final DatabaseHelper databaseHelper;
    private final ListView<String> questionListView = new ListView<>();
    private final TextField searchField = new TextField();
    private final Button addQuestionButton = new Button("Add Question");
    private final Button editQuestionButton = new Button("Edit Question");
    private final Button addAnswerButton = new Button("Add Answer");
    private final Button editAnswerButton = new Button("Edit Answer");
    private final Label questionLabel = new Label("Question: ");
    private final Label answerLabel = new Label("Answer: ");
    
 // 🗑️ "Delete Question" Button (Admins Only)
    private final Button deleteQuestionButton = new Button("Delete Question");

    private VBox rightPane;

    private final Map<String, Integer> questionIdMap = new HashMap<>(); // Store question ID mapping

    public QuestionPageController(RoleManager.Role userRole, String currentUsername, DatabaseHelper databaseHelper) {
        this.userRole = userRole;
        this.currentUsername = currentUsername; // ✅ Assign username
        this.databaseHelper = databaseHelper;
    }

    public void showQuestionPage(Stage primaryStage) {
        // 🔍 Search Bar
        searchField.setPromptText("Search questions...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterQuestions(newValue));

        // 📜 List of Questions
        questionListView.setPrefWidth(250);
        questionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedQuestion) -> {
            displayQuestionDetails(selectedQuestion);
        });

        // ➕ "Add Question" Button
        addQuestionButton.setOnAction(event -> showAddQuestionDialog());
        addQuestionButton.setStyle("-fx-background-color: lightgreen;");
        addQuestionButton.setVisible(userCanAddQuestion());

        // ✏️ "Edit Question" Button
        editQuestionButton.setOnAction(event -> showEditQuestionDialog());
        editQuestionButton.setStyle("-fx-background-color: lightblue;");
        editQuestionButton.setVisible(userRole == RoleManager.Role.ADMIN);

        // ✍️ "Add Answer" Button (Initially Hidden)
        addAnswerButton.setOnAction(event -> showAddAnswerDialog());
        addAnswerButton.setStyle("-fx-background-color: lightcoral;");
        addAnswerButton.setVisible(false);

        // ✍️ "Edit Answer" Button (Initially Hidden)
        editAnswerButton.setOnAction(event -> showEditAnswerDialog());
        editAnswerButton.setStyle("-fx-background-color: lightcoral;");
        editAnswerButton.setVisible(false);

        // 🗑️ "Delete Question" Button (Admins Only)
        deleteQuestionButton.setOnAction(event -> showDeleteQuestionDialog());
        deleteQuestionButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        deleteQuestionButton.setVisible(userRole == RoleManager.Role.ADMIN);

        // 📄 Right Pane Layout (Including Delete Button)
        rightPane = new VBox(10, questionLabel, answerLabel, editQuestionButton, addAnswerButton, editAnswerButton, deleteQuestionButton);
        rightPane.setPrefWidth(700);
        rightPane.setStyle("-fx-padding: 10;");
        rightPane.setVisible(false); // Initially hidden

        // 🏗 Split Layout
        VBox leftPane = new VBox(10, addQuestionButton, searchField, questionListView);
        leftPane.setPrefWidth(300);
        leftPane.setStyle("-fx-padding: 10;");

        SplitPane splitPane = new SplitPane(leftPane, rightPane);
        splitPane.setDividerPositions(0.3);

        Scene scene = new Scene(splitPane, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Page");
        primaryStage.show();

        // ✅ Load questions initially
        loadQuestions();
    }

    // ✅ Load questions and store their IDs
    private void loadQuestions() {
        List<Map.Entry<Integer, String>> questions = databaseHelper.getAllQuestionsWithIds();
        questionIdMap.clear();
        questionListView.getItems().clear();

        for (Map.Entry<Integer, String> entry : questions) {
            questionIdMap.put(entry.getValue(), entry.getKey());
            questionListView.getItems().add(entry.getValue());
        }

        // ✅ Ensure right pane is cleared if no question is selected
        if (questionListView.getSelectionModel().getSelectedItem() == null) {
            displayQuestionDetails(null);
        }
    }

    // ✅ Display selected question details and check for an existing answer
    private void displayQuestionDetails(String question) {
        if (question == null || question.isEmpty()) {
            // ✅ Clear and hide right pane
            questionLabel.setText("");
            answerLabel.setText("");
            addAnswerButton.setVisible(false);
            editAnswerButton.setVisible(false);
            rightPane.setVisible(false);
            return;
        }

        if (!questionIdMap.containsKey(question)) {
            showAlert("Error", "Selected question ID not found.");
            return;
        }

        rightPane.setVisible(true); // ✅ Show right pane when a question is selected
        int questionId = questionIdMap.get(question);
        String answer = databaseHelper.getAnswerForQuestion(questionId);

        // ✅ Update Labels
        questionLabel.setText("📌 Question: " + question);
        if (answer == null || answer.isEmpty()) {
            answerLabel.setText("📝 No answer available.");
            addAnswerButton.setVisible(true);
            editAnswerButton.setVisible(false);
        } else {
            answerLabel.setText("💬 Answer: " + answer);
            addAnswerButton.setVisible(false);
            editAnswerButton.setVisible(true);
        }
    }
    private void showAddAnswerDialog() {
        String selectedQuestion = questionListView.getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) {
            showAlert("No Question Selected", "Please select a question to answer.");
            return;
        }

        // ✅ Ensure question exists in the map
        if (!questionIdMap.containsKey(selectedQuestion)) {
            showAlert("Error", "Question ID not found.");
            return;
        }

        int questionId = questionIdMap.get(selectedQuestion); // ✅ Get Question ID

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Answer");
        dialog.setHeaderText("Enter your answer below:");
        dialog.setContentText("Answer:");

        dialog.showAndWait().ifPresent(answerText -> {
            if (!answerText.trim().isEmpty()) {
                databaseHelper.addAnswerToQuestion(questionId, answerText); // ✅ Ensure this method exists in `DatabaseHelper`
                displayQuestionDetails(selectedQuestion); // ✅ Refresh UI
            }
        });
    }

    
    private void showEditAnswerDialog() {
        String selectedQuestion = questionListView.getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) {
            showAlert("No Question Selected", "Please select a question to edit the answer.");
            return;
        }

        if (!questionIdMap.containsKey(selectedQuestion)) {
            showAlert("Error", "Question ID not found.");
            return;
        }

        int questionId = questionIdMap.get(selectedQuestion); // ✅ Get Question ID
        String currentAnswer = databaseHelper.getAnswerForQuestion(questionId);

        if (currentAnswer == null) {
            showAlert("No Answer Found", "There is no existing answer to edit.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(currentAnswer);
        dialog.setTitle("Edit Answer");
        dialog.setHeaderText("Update your answer:");
        dialog.setContentText("New Answer:");

        dialog.showAndWait().ifPresent(newAnswerText -> {
            if (!newAnswerText.trim().isEmpty()) {
                databaseHelper.updateAnswer(questionId, newAnswerText); // ✅ Ensure this method exists in `DatabaseHelper`
                displayQuestionDetails(selectedQuestion); // ✅ Refresh UI
            }
        });
    }

    
    

    private void showEditQuestionDialog() {
        String selectedQuestion = questionListView.getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) {
            showAlert("No Question Selected", "Please select a question to edit.");
            return;
        }

        if (!questionIdMap.containsKey(selectedQuestion)) {
            showAlert("Error", "Question ID not found.");
            return;
        }

        int questionId = questionIdMap.get(selectedQuestion);

        // ✅ Ensure only the owner can edit their question
        if (!databaseHelper.isUserQuestion(currentUsername, questionId) && userRole != RoleManager.Role.ADMIN) {
            showAlert("Permission Denied", "You can only edit your own questions.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedQuestion);
        dialog.setTitle("Edit Question");
        dialog.setHeaderText("Update your question:");
        dialog.setContentText("New Question:");

        dialog.showAndWait().ifPresent(newQuestionText -> {
            if (!newQuestionText.trim().isEmpty()) {
                databaseHelper.updateQuestion(questionId, newQuestionText);
                questionIdMap.remove(selectedQuestion);
                questionIdMap.put(newQuestionText, questionId);
                loadQuestions(); 
                displayQuestionDetails(newQuestionText);
            }
        });
    }


    
    private void showDeleteQuestionDialog() {
        String selectedQuestion = questionListView.getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) {
            showAlert("No Question Selected", "Please select a question to delete.");
            return;
        }

        if (!questionIdMap.containsKey(selectedQuestion)) {
            showAlert("Error", "Question ID not found.");
            return;
        }

        int questionId = questionIdMap.get(selectedQuestion);

        // ✅ Ensure only the owner can delete their question
        if (!databaseHelper.isUserQuestion(currentUsername, questionId) && userRole != RoleManager.Role.ADMIN) {
            showAlert("Permission Denied", "You can only delete your own questions.");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Are you sure you want to delete this question?");
        confirmDialog.setContentText("This action cannot be undone.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                databaseHelper.deleteQuestion(questionId);
                loadQuestions();
                displayQuestionDetails(null); // Clear the right pane
            }
        });
    }




    // ✅ Search Filter
    private void filterQuestions(String query) {
        List<String> filteredList = databaseHelper.getAllQuestionsWithIds().stream()
                .map(Map.Entry::getValue)
                .filter(q -> q.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        questionListView.getItems().setAll(filteredList);
    }

    private void showAddQuestionDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Question");
        dialog.setHeaderText("Enter your question below:");
        dialog.setContentText("Question:");

        dialog.showAndWait().ifPresent(questionText -> {
            if (questionText.trim().isEmpty()) {
                // ❌ Show error if the question is empty
                showAlert("Error", "Question cannot be empty.");
                return;
            }

            databaseHelper.addQuestion(questionText); // ✅ Reverting to the old method without created_by
            loadQuestions(); // ✅ Refresh the question list
        });
    }





    // ✅ Show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean userCanAddQuestion() {
        return userRole == RoleManager.Role.ADMIN 
            || userRole == RoleManager.Role.INSTRUCTOR 
            || userRole == RoleManager.Role.STAFF 
            || userRole == RoleManager.Role.USER; // ✅ Now users can add questions
    }

}
