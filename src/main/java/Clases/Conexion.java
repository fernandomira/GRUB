/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;

/**
 *
 * @author pasar
 */
import java.sql.Connection;
import java.sql.DriverManager;
import javafx.scene.control.Alert;

public class Conexion {
    Connection conectar = null;

    String usuario = "root";
    String contrasenia = "odnanreF.69   "; 
    String bd = "escuela_db";
    String ip = "localhost";
    String puerto = "3306";

    String cadena = "jdbc:mysql://"+ip+":"+puerto+"/"+bd+"?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    public Connection estableceConexion(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // ← driver actualizado

            conectar = DriverManager.getConnection(cadena, usuario, contrasenia);
            showAlert("Mensaje", "Se conectó a la base de datos");
        } catch (Exception e) {
            showAlert("Error", "No se conectó a la base de datos: " + e.toString());
        }
        return conectar;
    }

    private void showAlert(String title, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void cerrarConexion(){
        try {
            if (conectar != null && !conectar.isClosed()) {
                conectar.close();
                showAlert("Mensaje", "Conexión cerrada");
            }
        } catch (Exception e) {
            showAlert("Error", "Error al cerrar conexión: " + e.toString());
        }
    }
}

