package com.example.recetario;

import java.io.Serializable;

public class Receta implements Serializable {

    private long id;
    private String codigo;
    private String nombre;
    private String ingredientes;
    private String proceso;
    private String imagen;
    private boolean esFavorito; // Nueva propiedad

    // Constructor completo
    public Receta(long id, String codigo, String nombre, String ingredientes, String proceso, String imagen, boolean esFavorito) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.ingredientes = ingredientes;
        this.proceso = proceso;
        this.imagen = imagen;
        this.esFavorito = esFavorito;
    }

    // Constructor sin favorito (por compatibilidad)
    public Receta(long id, String codigo, String nombre, String ingredientes, String proceso, String imagen) {
        this(id, codigo, nombre, ingredientes, proceso, imagen, false);
    }

    // Getters
    public long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getIngredientes() { return ingredientes; }
    public String getProceso() { return proceso; }
    public String getImagen() { return imagen; }
    public boolean esFavorito() { return esFavorito; }

    // Setters
    public void setEsFavorito(boolean esFavorito) { this.esFavorito = esFavorito; }
    public void setId(long id) { this.id = id; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setIngredientes(String ingredientes) { this.ingredientes = ingredientes; }
    public void setProceso(String proceso) { this.proceso = proceso; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}