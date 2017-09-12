import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SignInPage {
    @FXML
    private Label classTime;
    @FXML
    private Label time;
    @FXML
    private Label classNamesDisplay;
    @FXML
    private ListView studentList;
    @FXML
    private TextField signInText;
    @FXML
    private Label message;

    public static ArrayList<Student> signedInStudents = new ArrayList<Student>(); //To check for double sign-ins.
    public static ArrayList<Student> lateStudents = new ArrayList<Student>();
    public static boolean isStudentLate = false;
    public static String formattedCurrentTime = "";

    public void initialize() {
        studentList.getItems().addAll(MainPage.selectedStudents); //Bring everything into this studentList.
        classNamesDisplay.setText("Class Names: " + MainPage.classNames);

        //With help from https://stackoverflow.com/questions/42383857/javafx-live-time-and-date
        //Make a live-updating time view on the top of the screen:
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            Date currentTime = new Date();
            formattedCurrentTime = timeFormat.format(currentTime).toString();
            time.setText(formattedCurrentTime);
        }),
                new KeyFrame(Duration.seconds(1))
        );

        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        //Now schedule an email to be sent at class time, and update the Label in the Stage.
        String hours = MainPage.givenHH;
        String minutes = MainPage.givenMM;
        String seconds = MainPage.givenSS;

        //Convert that classTime to a Date.
        Date firstEmailTime = new Date();
        firstEmailTime.setHours(Integer.parseInt(hours));
        firstEmailTime.setMinutes(Integer.parseInt(minutes));
        firstEmailTime.setSeconds(Integer.parseInt(seconds));
        String formattedClassTime = timeFormat.format(firstEmailTime);
        classTime.setText(formattedClassTime);
        ArrayList absentStudents = new ArrayList();

        //Now schedule a Timer task to send an email out.
        Timer email = new Timer();

        //If list is non-empty:
        email.schedule(new TimerTask() {
            public void run() {
                absentStudents.addAll(studentList.getItems()); //Add students that will be marked
                if (!absentStudents.isEmpty()) {
                    isStudentLate = true;
                    time.setTextFill(Color.RED);
                    if (MainPage.sendEmail) {
                        Email.sendEmail(Functions.secOfficeEmail, "Absent Students | " + formattedClassTime, absentStudents);
                    } else {

                    }

                    //Set up the loop to send out emails of late students every 10 minutes. Only send if there is at least 1 late student.
                    Timer lateEmail = new Timer();

                    lateEmail.scheduleAtFixedRate(new TimerTask() {
                        public void run() {
                            ArrayList<Student> lateEmailBatch = new ArrayList<Student>(); //This is to insulate against any issues where late students are lost while email is being sent.
                            //Make a copy of lateStudents to be emailed, and then clear it for next time.
                            lateEmailBatch.addAll(lateStudents);
                            lateStudents.clear();
                            if (!lateEmailBatch.isEmpty()) {
                                Email.sendEmail(Functions.secOfficeEmail, "Late Students | " + formattedClassTime, lateEmailBatch);
                                lateEmailBatch.clear();
                            }
                        }
                    }, 0, 600000); //10 min delay between emails.
                }
            }
        }, firstEmailTime);

        signInText.requestFocus(); //Bring attention to the textfield.
    }

    public void signIn() { //Using the ID number entered, delete a student from the list.

        boolean ready = true;
        int studentID = 0;
        ArrayList<Student> studentArrayList = new ArrayList<Student>();
        studentArrayList.addAll(studentList.getItems()); //Add all students into the search area.

        try {
            studentID = Integer.parseInt(signInText.getText());
        } catch (NumberFormatException e) {
            //Alert: incorrect input.
            ready = false;
            message.setTextFill(Color.RED);
            message.setText("Please enter a valid student ID number.");
            message.setVisible(true);
            signInText.requestFocus();
            signInText.clear();
        }
        if (ready) {
            boolean found = false;
            Student currentStudent = null;
            //Search within enrollments.
            for (Student student : studentArrayList) {
                if (student.getId() == studentID) {
                    currentStudent = student;
                    found = true;
                }
            }
            if (found) { //If an expected student is properly found in the arrayList, remove them and add them to the signed in student list. Also handle latecomers.
                message.setTextFill(Color.GREEN);
                message.setText(currentStudent.getfName() + " " + currentStudent.getlName() + " has signed in.");
                message.setVisible(true);

                if (isStudentLate) {
                    currentStudent.setLateTime(formattedCurrentTime);
                    lateStudents.add(currentStudent);
                }

                signedInStudents.add(currentStudent);
                studentList.getItems().remove(currentStudent);

            } else {
                //Alert: student not found. If they have already signed in, say so!
                boolean isSignedIn = false;

                for (Student searching : signedInStudents) {
                    if (studentID == searching.getId()) {
                        isSignedIn = true;
                    }
                }
                if (isSignedIn) { //When they have previously signed in.
                    message.setTextFill(Color.BLUE);
                    message.setText("You have already signed in.");
                    message.setVisible(true);
                } else { //When they are not expected/input is wrong.
                    message.setTextFill(Color.RED);
                    message.setText("Student not found in list. Please try again.");
                    message.setVisible(true);
                }
            }
        }
        //Just make sure to bring focus back to the textField.
        signInText.requestFocus();
        signInText.clear();
    }
}
