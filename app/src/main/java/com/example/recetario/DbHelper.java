package com.example.recetario;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    // Constantes de la Base de Datos
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "recetas.db";
    public static final String TABLE_NAME = "catalogo";

    // Constantes de la Tabla "catalogo"
    public static final String COL_ID = "_id"; // ID interno (PK)
    public static final String COL_CODIGO = "codigo"; // Código de receta (solicitado por usuario)
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_INGREDIENTES = "ingredientes"; // Se guardará como texto simple
    public static final String COL_PROCESO = "proceso";
    public static final String COL_IMAGEN = "imagen"; // Nombre del drawable (ej: "receta1")

    // Sentencia SQL para crear la tabla
    private static final String CREATE_TABLE_CATALOGO =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_CODIGO + " TEXT UNIQUE NOT NULL," +
                    COL_NOMBRE + " TEXT NOT NULL," +
                    COL_INGREDIENTES + " TEXT," +
                    COL_PROCESO + " TEXT," +
                    COL_IMAGEN + " TEXT)";

    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Ejecuta la sentencia de creación
        db.execSQL(CREATE_TABLE_CATALOGO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // En una actualización, borra la tabla anterior y la vuelve a crear
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // --- Métodos CRUD ---

    /**
     * CREATE: Agrega una nueva receta a la base de datos.
     */
    public boolean addReceta(String codigo, String nombre, String ingredientes, String proceso, String imagen) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CODIGO, codigo);
        values.put(COL_NOMBRE, nombre);
        values.put(COL_INGREDIENTES, ingredientes);
        values.put(COL_PROCESO, proceso);
        values.put(COL_IMAGEN, imagen);

        // Inserta la fila. Retorna -1 si hay un error (ej: código duplicado)
        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result != -1;
    }

    /**
     * READ: Obtiene todas las recetas.
     */
    public Cursor getAllRecetas() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, COL_NOMBRE + " ASC");
    }

    /**
     * READ: Busca recetas por CÓDIGO.
     */
    public Cursor getRecetaByCodigo(String codigo) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null,
                COL_CODIGO + " = ?", new String[]{codigo},
                null, null, null);
    }

    /**
     * READ: Busca recetas por NOMBRE.
     */
    public Cursor getRecetaByNombre(String nombre) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Usamos LIKE para buscar coincidencias parciales
        return db.query(TABLE_NAME, null,
                COL_NOMBRE + " LIKE ?", new String[]{"%" + nombre + "%"},
                null, null, null);
    }

    /**
     * READ: Obtiene una receta por su ID interno.
     */
    public Cursor getRecetaById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null,
                COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
    }

    /**
     * UPDATE: Modifica una receta existente usando su ID interno.
     */
    public boolean updateReceta(long id, String codigo, String nombre, String ingredientes, String proceso, String imagen) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CODIGO, codigo);
        values.put(COL_NOMBRE, nombre);
        values.put(COL_INGREDIENTES, ingredientes);
        values.put(COL_PROCESO, proceso);
        values.put(COL_IMAGEN, imagen);

        // Actualiza la fila donde _id coincida
        int rowsAffected = db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    /**
     * DELETE: Elimina una receta usando su ID interno.
     */
    public boolean deleteReceta(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Elimina la fila donde _id coincida
        int rowsAffected = db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }
}