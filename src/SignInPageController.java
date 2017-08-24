import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.util.Duration;

import java.util.Calendar;

public class SignInPageController {
    @FXML private Label classTime;
    @FXML private Label time;
    @FXML private ListView studentList;

    @FXML
    public void initialize() { //https://stackoverflow.com/questions/42383857/javafx-live-time-and-date
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            Calendar cal = Calendar.getInstance();
            //properly format the time with leading zeros
            String second = String.format("%02d", (cal.get(Calendar.SECOND)));
            String minute = String.format("%02d", (cal.get(Calendar.MINUTE)));
            String hour = String.format("%02d", (cal.get(Calendar.HOUR)));
            time.setText(hour + ":" + (minute) + ":" + second);
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        classTime.setText(MainPageController.classTime);

        studentList.getItems().addAll(MainPageController.selectedStudents);

    }

    public void signIn() {
        Object toRemove = studentList.getSelectionModel().getSelectedItem();
        studentList.getItems().remove(toRemove);
    }
}
