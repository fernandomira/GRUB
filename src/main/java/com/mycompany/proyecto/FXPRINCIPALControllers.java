package com.mycompany.proyecto;

import Clases.Estudiante;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;

public class FXPRINCIPALControllers {

    @FXML private TextField nombreField;
    @FXML private TextField apellidosField;
    @FXML private ComboBox<String> sexoComboBox;
    @FXML private TableView<Estudiante> tablaEstudiantes;
    @FXML private TableColumn<Estudiante, Integer> colId;
    @FXML private TableColumn<Estudiante, String> colNombre;
    @FXML private TableColumn<Estudiante, String> colApellidos;
    @FXML private TableColumn<Estudiante, String> colSexo;

    private final ObservableList<Estudiante> listaEstudiantes = FXCollections.observableArrayList();
    private Connection conexion;

    // ⚠️ Ajusta estos valores según tu configuración
    private final String DB_URL = "jdbc:mysql://localhost:3306/bdusuarios?useSSL=false&serverTimezone=UTC";
    private final String USER = "root";
    private final String PASS = "1234";

    @FXML
    public void initialize() {
        System.out.println("Iniciando aplicación...");
        
        // Configurar las columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colSexo.setCellValueFactory(new PropertyValueFactory<>("fkSexoString"));

        // Configurar ComboBox
        sexoComboBox.getItems().addAll("Masculino", "Femenino");

        // Asignar la lista a la tabla ANTES de cargar datos
        tablaEstudiantes.setItems(listaEstudiantes);

        // Conectar a la base de datos y cargar estudiantes
        conectarBD();
        if (conexion != null) {
            cargarEstudiantes();
            // Forzar actualización de la tabla
            tablaEstudiantes.refresh();
            System.out.println("Tabla actualizada. Elementos en lista: " + listaEstudiantes.size());
        }

        // Configurar la selección de la tabla
        tablaEstudiantes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarDatosEnFormulario(newSelection);
            }
        });
        
        // Estilo para las filas (doble clic para editar)
        tablaEstudiantes.setRowFactory(tv -> {
            TableRow<Estudiante> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    cargarDatosEnFormulario(row.getItem());
                }
            });
            return row;
        });
    }

    private void conectarBD() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Conexión exitosa a la base de datos");
            
            // Verificar la estructura de la tabla
            verificarEstructuraTabla();
            
        } catch (Exception e) {
            mostrarAlerta("Error de Conexión", "No se pudo conectar a la base de datos: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void verificarEstructuraTabla() {
        try {
            String sql = "DESCRIBE usuarios";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("Estructura de la tabla 'usuarios':");
            while (rs.next()) {
                System.out.println("Campo: " + rs.getString("Field") + 
                                 ", Tipo: " + rs.getString("Type") + 
                                 ", Nulo: " + rs.getString("Null"));
            }
            
            // También verificar si hay datos
            String countSql = "SELECT COUNT(*) as total FROM usuarios";
            ResultSet countRs = stmt.executeQuery(countSql);
            if (countRs.next()) {
                int total = countRs.getInt("total");
                System.out.println("Total de registros en la tabla: " + total);
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error al verificar estructura: " + e.getMessage());
        }
    }

    private void cargarEstudiantes() {
        if (conexion == null) {
            mostrarAlerta("Error", "No hay conexión a la base de datos", Alert.AlertType.ERROR);
            return;
        }
        
        listaEstudiantes.clear();
        String sql = "SELECT u.id, u.nombre, u.apellidos, u.fksexo FROM usuarios u ORDER BY u.id";
        
        try (Statement stmt = conexion.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("Ejecutando consulta: " + sql);
            
            int contador = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String apellidos = rs.getString("apellidos");
                int fksexo = rs.getInt("fksexo");
                
                System.out.println("Cargando estudiante: ID=" + id + ", Nombre=" + nombre + ", Apellidos=" + apellidos + ", Sexo=" + fksexo);
                
                Estudiante estudiante = new Estudiante(id, nombre, apellidos, fksexo);
                listaEstudiantes.add(estudiante);
                contador++;
            }
            
            System.out.println("Total estudiantes cargados: " + contador);
            System.out.println("Elementos en listaEstudiantes: " + listaEstudiantes.size());
            
            // Verificar si la tabla está vinculada
            if (tablaEstudiantes.getItems() != listaEstudiantes) {
                tablaEstudiantes.setItems(listaEstudiantes);
                System.out.println("Tabla revinculada a la lista");
            }
            
            // Forzar actualización
            tablaEstudiantes.refresh();
            
        } catch (SQLException e) {
            System.err.println("Error SQL: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar estudiantes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarDatosEnFormulario(Estudiante estudiante) {
        if (estudiante != null) {
            nombreField.setText(estudiante.getNombre());
            apellidosField.setText(estudiante.getApellidos());
            sexoComboBox.setValue(estudiante.getFkSexo() == 1 ? "Masculino" : "Femenino");
        }
    }

    @FXML
    private void agregarEstudiante() {
        if (!validarCampos()) return;
        
        String nombre = nombreField.getText().trim();
        String apellidos = apellidosField.getText().trim();
        int fksexo = sexoComboBox.getValue().equals("Masculino") ? 1 : 2;

        String sql = "INSERT INTO usuarios (nombre, apellidos, fksexo) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setString(2, apellidos);
            stmt.setInt(3, fksexo);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                mostrarAlerta("Éxito", "Estudiante agregado correctamente", Alert.AlertType.INFORMATION);
                cargarEstudiantes();
                limpiarCampos();
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo agregar el estudiante: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void actualizarEstudiante() {
        Estudiante estudianteSeleccionado = tablaEstudiantes.getSelectionModel().getSelectedItem();
        
        if (estudianteSeleccionado == null) {
            mostrarAlerta("Advertencia", "Seleccione un estudiante para actualizar", Alert.AlertType.WARNING);
            return;
        }
        
        if (!validarCampos()) return;

        String nombre = nombreField.getText().trim();
        String apellidos = apellidosField.getText().trim();
        int fksexo = sexoComboBox.getValue().equals("Masculino") ? 1 : 2;

        String sql = "UPDATE usuarios SET nombre=?, apellidos=?, fksexo=? WHERE id=?";

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setString(2, apellidos);
            stmt.setInt(3, fksexo);
            stmt.setInt(4, estudianteSeleccionado.getId());
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                mostrarAlerta("Éxito", "Estudiante actualizado correctamente", Alert.AlertType.INFORMATION);
                cargarEstudiantes();
                limpiarCampos();
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al actualizar el estudiante: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminarEstudiante() {
        Estudiante estudianteSeleccionado = tablaEstudiantes.getSelectionModel().getSelectedItem();
        
        if (estudianteSeleccionado == null) {
            mostrarAlerta("Advertencia", "Seleccione un estudiante para eliminar", Alert.AlertType.WARNING);
            return;
        }

        // Confirmación antes de eliminar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar este estudiante?");
        confirmacion.setContentText("Estudiante: " + estudianteSeleccionado.getNombre() + " " + estudianteSeleccionado.getApellidos() + "\nEsta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM usuarios WHERE id=?";

                try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
                    stmt.setInt(1, estudianteSeleccionado.getId());
                    
                    int filasAfectadas = stmt.executeUpdate();
                    if (filasAfectadas > 0) {
                        mostrarAlerta("Éxito", "Estudiante eliminado correctamente", Alert.AlertType.INFORMATION);
                        cargarEstudiantes();
                        limpiarCampos();
                    }
                } catch (SQLException e) {
                    mostrarAlerta("Error", "No se pudo eliminar el estudiante: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void limpiarCampos() {
        nombreField.clear();
        apellidosField.clear();
        sexoComboBox.setValue(null);
        tablaEstudiantes.getSelectionModel().clearSelection();
    }

    @FXML
    private void recargarDatos() {
        System.out.println("Recargando datos manualmente...");
        cargarEstudiantes();
        mostrarAlerta("Información", "Datos recargados. Registros encontrados: " + listaEstudiantes.size(), Alert.AlertType.INFORMATION);
    }

    private boolean validarCampos() {
        if (nombreField.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "El campo 'Nombre' es obligatorio", Alert.AlertType.WARNING);
            nombreField.requestFocus();
            return false;
        }
        
        if (apellidosField.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "El campo 'Apellidos' es obligatorio", Alert.AlertType.WARNING);
            apellidosField.requestFocus();
            return false;
        }
        
        if (sexoComboBox.getValue() == null) {
            mostrarAlerta("Validación", "Debe seleccionar un sexo", Alert.AlertType.WARNING);
            sexoComboBox.requestFocus();
            return false;
        }
        
        return true;
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        // Personalizar el icono y estilo según el tipo
        switch (tipo) {
            case ERROR:
                alert.setHeaderText("Error");
                break;
            case WARNING:
                alert.setHeaderText("Advertencia");
                break;
            case INFORMATION:
                alert.setHeaderText("Información");
                break;
        }
        
        alert.showAndWait();
    }
    
    // Método para cerrar la conexión cuando se cierre la aplicación
    public void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}