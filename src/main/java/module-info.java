module com.mycompany.proyecto {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;
    
    // Abrir paquetes para JavaFX
    opens com.mycompany.proyecto to javafx.fxml;
    opens Clases to javafx.base, javafx.fxml;
    
    exports com.mycompany.proyecto;
}