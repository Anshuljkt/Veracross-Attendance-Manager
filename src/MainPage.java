import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

public class MainPage extends Application {

    //Variables for different FXML elements to be controlled.
    @FXML private TextField searchBar, hh, mm;
    @FXML private CheckBox checkBox12, checkBox13;
    @FXML private ListView searchList, selectedList;
    @FXML private Button findEnrollmentsButton, selectButton, deSelectButton;
    @FXML private CheckBox emailOption;

    //Variables that will be shared across classes.
    public static ArrayList selectedStudents;
    public static String givenHH, givenMM, givenSS; //To be concatenated and used by Sign In page.
    public static String classNames = "";
    public static boolean sendEmail;
    public Parent parent;
    public Stage mainStage;
    public static int numOfClasses;

    public static void main(String[] args) {
        //Just set the working directory for every file.
        String currentPath = System.getProperty("user.dir"); //Get current directory
        Path joinedPath = FileSystems.getDefault().getPath(currentPath, "NISTAttendanceData"); //Join it to NISTAttendanceData
        Functions.programDataDir = joinedPath.toString(); //Set it into Functions class's programDataDir.

        //Make the directory if it doesn't exist.
        File directory = new File(Functions.programDataDir);
        directory.mkdir();

        //Then launch the program.
        launch(args);


    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        parent = FXMLLoader.load(getClass().getResource("MainPage.fxml"));
        mainStage = primaryStage;
        mainStage.getIcons().add(new Image("Icon.png"));
        mainStage.setTitle("NIST Attendance");
        mainStage.setScene(new Scene(parent));
        mainStage.setOnCloseRequest(event -> System.exit(0)); //Make sure user can always exit when they want to.

        //Check if the user wants to update the database.
        Alert databaseUpdate = new Alert(Alert.AlertType.CONFIRMATION);
        databaseUpdate.setTitle("Database Update");
        databaseUpdate.setContentText("The database was last updated on: " + Functions.readTime() + "\nIt is recommended to update at least once every day.");
        databaseUpdate.setHeaderText("Would you like to update the student and class database? \nThis may take up to 1 minute.");
        ButtonType buttonYes = new ButtonType("Yes");
        ButtonType buttonNo = new ButtonType("No");
        databaseUpdate.getButtonTypes().setAll(buttonYes, buttonNo);
        Optional<ButtonType> result = databaseUpdate.showAndWait();
        databaseUpdate.setOnCloseRequest(event -> System.exit(0));

        String initializeResponse;
        if (result.get() == buttonYes) {
            //This saves the return from the initialize method to decide whether or not to continue.
            initializeResponse = Functions.initialize(true);
        } else {
            initializeResponse = Functions.initialize(false);
        }

        //Now check initializeResponse, and alert if there were any errors.
        if (initializeResponse.equalsIgnoreCase("downloadError")) {
            //Show this alert, and exit after.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText("The program was unable to connect to the Veracross servers.");
            alert.setContentText("Please check your Internet Connection, and restart the program.");
            alert.showAndWait();
            System.exit(0);
        } else {
            //All okay, show the mainStage.
            mainStage.show();
        }
    }

    public void processEnrollments() throws IOException { //This will take in user input and give us the students in those classes.
        if (validateFields()) {
            //Close the stage first to prevent multiple sign-in pages:
            Stage toClose = (Stage) findEnrollmentsButton.getScene().getWindow();
            toClose.close();

            //Just set the class time to be shown. These will be used in the Sign In Page.
            givenSS = "00";
            givenMM = String.format("%02d", (Integer.parseInt(mm.getText())));
            if (Integer.parseInt((hh.getText())) >= 12) {
                givenHH = String.format("%02d", (Integer.parseInt(hh.getText())));
            } else {
                givenHH = String.format("%02d", (Integer.parseInt(hh.getText())));
            }

            ObservableList<Class> selectedClasses = selectedList.getItems(); //This is the user input.

            ArrayList<Integer> classIDs = new ArrayList<Integer>(); //This is what to use for processing enrollments.
            selectedStudents = new ArrayList<Student>(); //This is where the resulting students will be stored.

            //First download all enrollments from selected classes, and add the students into the selectedStudents ArrayList.
            for (Class i : selectedClasses) {
                classIDs.add(i.getId());
                classNames = classNames + i.getName() + " - " + i.getTeacherName() +" | "; //Also concatenate all class names for the email.
            }

            selectedStudents.addAll(Functions.processEnrollments(classIDs, false, selectedList.getItems().size()));

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

            //Take in the emailOption
            sendEmail = emailOption.isSelected();

            //Now go to SignInPage in a new window, using the other FXML file.
            FXMLLoader fxmlLoader = new FXMLLoader((getClass().getResource("SignInPage.fxml")));
            parent = (Parent) fxmlLoader.load();
            fxmlLoader.setController("SignInPage");
            mainStage = new Stage();
            mainStage.setScene(new Scene(parent));
            mainStage.setMaximized(true);
            mainStage.setTitle("Student Sign-In");
            mainStage.show();
            mainStage.setOnCloseRequest(event -> System.exit(0));
        }
    }


    public void searchClasses() {
        //Clear the search pane first, then fill it up with all the results of the search.
        searchList.getItems().clear();
        searchList.getItems().addAll(Functions.searchClasses(searchBar.getText()));
        searchList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        searchList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void selectClasses() {
        //Just put all the classes selected in the searchList into the selectedList.
        selectedList.getItems().addAll(searchList.getSelectionModel().getSelectedItems());
        searchList.getItems().removeAll(searchList.getSelectionModel().getSelectedItems());
    }

    public void deSelectClasses() {
        //Just put all the classes selected in the selectedList into the searchList.
        searchList.getItems().addAll(selectedList.getSelectionModel().getSelectedItems());
        selectedList.getItems().removeAll(selectedList.getSelectionModel().getSelectedItems());
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
        ObservableList<Class> selectedClasses = selectedList.getItems();
        if (!checkBox12.isSelected() && !checkBox13.isSelected() && selectedClasses.isEmpty()) {
            alert.setContentText("Please select at least one class.");
            alert.showAndWait();
            result = false;

        }
        return result;
    }
    }

