package Clases;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Estudiante {
    private final IntegerProperty id;
    private final StringProperty nombre;
    private final StringProperty apellidos;
    private final IntegerProperty fkSexo;

    // Constructor con todos los parámetros
    public Estudiante(int id, String nombre, String apellidos, int fkSexo) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.apellidos = new SimpleStringProperty(apellidos);
        this.fkSexo = new SimpleIntegerProperty(fkSexo);
    }

    // Constructor sin ID (para inserciones)
    public Estudiante(String nombre, String apellidos, int fkSexo) {
        this(0, nombre, apellidos, fkSexo);
    }

    // Getters y Setters para ID
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Getters y Setters para Nombre
    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    // Getters y Setters para Apellidos
    public String getApellidos() {
        return apellidos.get();
    }

    public void setApellidos(String apellidos) {
        this.apellidos.set(apellidos);
    }

    public StringProperty apellidosProperty() {
        return apellidos;
    }

    // Getters y Setters para fkSexo
    public int getFkSexo() {
        return fkSexo.get();
    }

    public void setFkSexo(int fkSexo) {
        this.fkSexo.set(fkSexo);
    }

    public IntegerProperty fkSexoProperty() {
        return fkSexo;
    }

    // Método para obtener el sexo como String (requerido por la tabla)
    public String getFkSexoString() {
        return getFkSexo() == 1 ? "Masculino" : "Femenino";
    }

    public StringProperty fkSexoStringProperty() {
        return new SimpleStringProperty(getFkSexoString());
    }

    @Override
    public String toString() {
        return "ID: " + getId() + " - " + getNombre() + " " + getApellidos() + " (" + getFkSexoString() + ")";
    }
}