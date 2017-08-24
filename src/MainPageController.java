import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class MainPageController extends Application {
    //Variables for different

    @FXML private TextField searchBar, hh, mm;
    @FXML private CheckBox checkBox12, checkBox13;
    @FXML private ListView searchList;

    public static ArrayList selectedStudents;
    public static String givenHH, givenMM, givenSS; //To be concatenated and used by Sign In page.
    public static String classNames = "";
    public Parent parent;
    public Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        parent = FXMLLoader.load(getClass().getResource("MainPage.fxml"));
        mainStage = primaryStage;
        mainStage.setTitle("NIST Attendance");
        mainStage.setScene(new Scene(parent));
        mainStage.show();
        mainStage.setOnCloseRequest(event -> System.exit(0)); //Make sure user can always exit when they want to.

        //Check if the user wants to update the database.
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Database Update");
        alert.setContentText("The database was last updated on: " + Functions.readTime() + "\nIt is recommended to update at least once every day.");
        alert.setHeaderText("Would you like to update the student and class database? \nThis may take up to 1 minute.");
        ButtonType buttonYes = new ButtonType("Yes");
        ButtonType buttonNo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonYes, buttonNo);
        Optional<ButtonType> result = alert.showAndWait();
        alert.setOnCloseRequest(event -> System.exit(0));

        if (result.get() == buttonYes) {
            Functions.initialize("both");
        } else {
            Functions.initialize("none");
        }
    }

    public void processEnrollments() throws IOException { //This will take in user input and give us the students in those classes.
        if (validateFields()) {
            //Just set the class time to be shown. These will be used in the Sign In Page.
            givenSS = "00";
            givenMM = String.format("%02d", (Integer.parseInt(mm.getText())));
            givenHH = String.format("%02d", (Integer.parseInt(hh.getText())));


            ObservableList<Class> selectedClasses = searchList.getSelectionModel().getSelectedItems(); //This is the user input.
            ArrayList<Integer> classIDs = new ArrayList<Integer>(); //This is what to use for processing enrollments.
            selectedStudents = new ArrayList<Student>(); //This is where the resulting students will be stored.

            //First download all enrollments from selected classes, and add the students into the selectedStudents ArrayList.
            for (Class i : selectedClasses) {
                classIDs.add(i.getId());
                classNames = classNames + i.getName() +" | "; //Also concatenate all class names for the email.
            }

            selectedStudents.addAll(Functions.processEnrollments(classIDs, false));

            //If entire year levels have been selected, then add them to the selectedStudents ArrayList too.
            if (checkBox12.isSelected()) {
                selectedStudents.addAll(Functions.searchStudents("Year 12"));
                classNames = classNames + "All Y12 | ";
            }
            if (checkBox13.isSelected()) {
                selectedStudents.addAll(Functions.searchStudents("Year 13"));
                classNames = classNames + "All Y13 | ";
            }
            //Remove any duplicates
            Set noDuplicates = new LinkedHashSet(selectedStudents);
            selectedStudents.clear();
            selectedStudents.addAll(noDuplicates);
            Collections.sort(selectedStudents);

            //Now go to SignInPage in a new window, using the other FXML file.
            FXMLLoader fxmlLoader = new FXMLLoader((getClass().getResource("SignInPage.fxml")));
            parent = (Parent) fxmlLoader.load();
            fxmlLoader.setController("SignInPageController");
            mainStage = new Stage();
            mainStage.setScene(new Scene(parent));
            mainStage.setMaximized(true);
            mainStage.show();
//            mainStage.setOnCloseRequest(event -> System.exit(0));
        }
    }


    public void searchClasses() {
        searchList.getItems().clear();
        searchList.getItems().addAll(Functions.searchClasses(searchBar.getText()));
        searchList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public boolean validateFields() { //Make sure you're not searching anything empty, and that the numbers make sense.
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        boolean result = true;
        try {
            int hours = Integer.parseInt(hh.getText());
            int mins = Integer.parseInt(mm.getText());
            if (!(hours>=0 && hours<=23 && mins>=0 && mins<=59)) {
                alert.setContentText("Please enter a valid 24-hour clock time (HH:MM) between 00:00 and 23:59.");
                alert.showAndWait();
                result = false;
            }

        } catch (Exception e) {
            alert.setContentText("Please enter a valid 24-hour clock time (HH:MM) between 00:00 and 23:59.");
            alert.showAndWait();
            result = false;
        }
        ObservableList<Class> selectedClasses = searchList.getSelectionModel().getSelectedItems();
        if (!checkBox12.isSelected() && !checkBox13.isSelected() && selectedClasses.isEmpty()) {
            alert.setContentText("Please select at least one class.");
            alert.showAndWait();
            result = false;

        }
        return result;
    }
    }

