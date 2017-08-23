import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;



public class Controller extends Application {
    //These are all the different scenes and buttons we will have in the program.

    public Button button;
    Stage window;
    Scene scene1, scene2;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Design.fxml"));
        primaryStage.setTitle("NIST Attendance");
        primaryStage.setScene(new Scene(root, 500, 500));
        primaryStage.show();

    }

    public void handleClick() {
        System.out.println("Code");
        button.setText("asdas");
    }

//        window = primaryStage;
//        window.setTitle("NIST Attendance");
//
//        GridPane grid = new GridPane();
//        grid.setPadding(new Insets(10, 10, 10, 10));
//        grid.setVgap(8);
//        grid.setHgap(10);
//
//        //Name Label
//        Label nameLabel = new Label("Username");
//        GridPane.setConstraints(nameLabel,3,0);
//
//        //Name input
//        TextField nameInput = new TextField("Bucky");
//        GridPane.setConstraints(nameInput, 1, 0);
//
//        //Password Label
//        Label passLabel = new Label("Password");
//        GridPane.setConstraints(passLabel,0,1);
//
//        //Password input
//        TextField passInput = new TextField();
//        passInput.setPromptText("Hello");
//        GridPane.setConstraints(passInput, 1, 1);
//
//        Button login = new Button("Log in");
//        GridPane.setConstraints(login, 1, 2);
//
//        grid.getChildren().addAll(nameLabel, nameInput, passLabel, passInput, login);
//
//        Scene scene = new Scene(grid, 300,200);
//        window.setScene(scene);
//        window.show();

//        Label label1 = new Label("Welcome");
//        Button button1 = new Button("Go to scene 2");
//        button1.setOnAction(event -> window.setScene(scene2));
//
//        VBox layout1 = new VBox(20);
//        layout1.getChildren().addAll(label1, button1);
//        scene1 = new Scene(layout1, 300,300);
//
//        Button button2 = new Button("Scene 1");
//        button2.setOnAction(event -> window.setScene(scene1));
//
//        StackPane layout2 = new StackPane();
//        layout2.getChildren().add(button2);
//        scene2 = new Scene(layout2, 600,600);
//
//        window.setScene(scene1);
//        window.setTitle("Title here");
//        window.show();
    }

