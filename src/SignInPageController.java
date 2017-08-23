import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.Calendar;

public class SignInPageController {

    private Label classTime = new Label();
    private Label time = new Label();

    @FXML
    public void initialize() { //https://stackoverflow.com/questions/42383857/javafx-live-time-and-date
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            Calendar cal = Calendar.getInstance();
            //properly format the time with leading zeros
            String second = String.format("%02d", (cal.get(Calendar.SECOND)));
            String minute = String.format("%02d", (cal.get(Calendar.MINUTE)));
            String hour = String.format("%02d", (cal.get(Calendar.HOUR)));
            time.setText(hour + ":" + minute + ":" + second);
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    public void signIn() {
        System.out.println("S");
    }
}
