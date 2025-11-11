package com.example.recetario;

import java.io.Serializable;

// Implementamos Serializable para poder pasar el objeto completo entre Activities
public class Receta implements Serializable {

    private long id;
    private String codigo;
    private String nombre;
    private String ingredientes;
    private String proceso;
    private String imagen; // Nombre del drawable, ej: "receta1"

    // Constructor
    public Receta(long id, String codigo, String nombre, String ingredientes, String proceso, String imagen) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.ingredientes = ingredientes;
        this.proceso = proceso;
        this.imagen = imagen;
    }

    // Getters
    public long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getIngredientes() { return ingredientes; }
    public String getProceso() { return proceso; }
    public String getImagen() { return imagen; }
}