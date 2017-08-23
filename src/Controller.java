import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.*;

public class Controller extends Application {
    //Variables for different

    @FXML
    private TextField searchBar, hh, mm;
    @FXML
    private CheckBox checkBox12;
    @FXML
    private CheckBox checkBox13;
    @FXML
    private ListView searchList;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainPage.fxml"));
        primaryStage.setTitle("NIST Attendance");
        primaryStage.setScene(new Scene(root, 500, 800));
        primaryStage.show();


        //Check if the user wants to update the database.
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Database Update");
        alert.setContentText("The database was last updated on: " + Functions.readTime() + "\nIt is recommended to update at least once every day.");
        alert.setHeaderText("Would you like to update the student and class database? \nThis may take up to 1 minute.");
        ButtonType buttonYes = new ButtonType("Yes");
        ButtonType buttonNo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonYes, buttonNo);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == buttonYes) {
            Functions.initialize("both");
        } else {
            Functions.initialize("none");
        }
    }

    public void processEnrollments() { //This will take in user input and give us the
        ObservableList<Class> selectedClasses = searchList.getSelectionModel().getSelectedItems(); //This is the user input.
        ArrayList<Integer> classIDs = new ArrayList<Integer>(); //This is what to use for processing enrollments.
        ArrayList<Student> selectedStudents = new ArrayList<Student>(); //This is where the resulting students will be stored.

        //First download all enrollments from selected classes, and add the students into the selectedStudents ArrayList.
        for (Class i : selectedClasses) {
            classIDs.add(i.getId());
        }
        selectedStudents.addAll(Functions.processEnrollments(classIDs, false));

        //If entire year levels have been selected, then add them to the selectedStudents ArrayList too.
        if (checkBox12.isSelected()) {
            selectedStudents.addAll(Functions.searchStudents("Year 12"));
        }
        if (checkBox13.isSelected()) {
            selectedStudents.addAll(Functions.searchStudents("Year 13"));
        }
        //Remove any duplicates
        Set noDuplicates = new LinkedHashSet(selectedStudents);
        selectedStudents.clear();
        selectedStudents.addAll(noDuplicates);
        Collections.sort(selectedStudents);
        Functions.printArrayList(selectedStudents);
    }

    public void searchClasses() {
        searchList.getItems().clear();
        searchList.getItems().addAll(Functions.searchClasses(searchBar.getText()));
        searchList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    }

