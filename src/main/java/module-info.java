module com.group01.asm2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires cloudinary.core;

    opens com.group01.asm2 to javafx.fxml;
    opens com.group01.asm2.controllers to javafx.fxml;


    exports com.group01.asm2;
    exports com.group01.asm2.controllers;
}