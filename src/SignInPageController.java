import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;

public class SignInPageController {
    @FXML private Label classTime;
    @FXML private Label time;
    @FXML private ListView studentList;

    @FXML
    public void initialize() {
        //From https://stackoverflow.com/questions/42383857/javafx-live-time-and-date
        //Make a live-updating time view on the top of the screen:
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            Calendar cal = Calendar.getInstance();
            //properly format the time with leading zeros
            String second = String.format("%02d", (cal.get(Calendar.SECOND)));
            String minute = String.format("%02d", (cal.get(Calendar.MINUTE)));
            String hour = String.format("%02d", (cal.get(Calendar.HOUR)));
            String am_pm = (cal.get(Calendar.AM_PM)==0) ? "AM" : "PM";
            time.setText(hour + ":" + (minute) + ":" + second + " " + am_pm);
        }),
                new KeyFrame(Duration.seconds(1))
        );

        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        //Now set classTime display, and schedule an email to be sent then.
        String hours = MainPageController.givenHH;
        String minutes = MainPageController.givenMM;
        String seconds = MainPageController.givenSS;
        String combinedTime = hours + ":" + minutes + ":" + seconds;
        classTime.setText(combinedTime);

        //Convert that classTime to a Date.
        Date emailTime = new Date();
        emailTime.setHours(Integer.parseInt(hours));
        emailTime.setMinutes(Integer.parseInt(minutes));
        emailTime.setSeconds(Integer.parseInt(seconds));

        //Now schedule a Timer task to send an email out.
        Timer email = new Timer();
        email.schedule(new TimerTask() {
            public void run() {
                time.setTextFill(Color.RED);
                ArrayList selectedStudents = new ArrayList();
                selectedStudents.addAll(studentList.getItems());
                Email.sendEmail(Functions.secOfficeEmail, "Absent Students | " + combinedTime, selectedStudents);

            }
        }, emailTime);

        studentList.getItems().addAll(MainPageController.selectedStudents);
    }

    public void signIn() {
        Object toRemove = studentList.getSelectionModel().getSelectedItem();
        studentList.getItems().remove(toRemove);
    }
}
