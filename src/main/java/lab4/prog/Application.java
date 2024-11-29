package lab4.prog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/lab4/prog/styles.css")).toExternalForm());
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        WeatherManager controller = fxmlLoader.getController();

        stage.setOnCloseRequest(event -> {
            controller.shutdownExecutors();
            System.out.println("Application closed. Executors have been shut down.");
        });
    }

    public static void main(String[] args) {
        launch();
    }
}