module org.example.lab4 {
    requires javafx.controls;
    requires javafx.fxml;


    opens lab4.prog to javafx.fxml;
    exports lab4.prog;
}