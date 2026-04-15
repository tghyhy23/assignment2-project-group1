module com.example.asm2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.asm2 to javafx.fxml;
    exports com.example.asm2;
}