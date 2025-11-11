package com.example.recetario;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2; // Incrementado por la nueva columna
    private static final String DATABASE_NAME = "recetas.db";
    public static final String TABLE_NAME = "catalogo";

    // Columnas
    public static final String COL_ID = "_id";
    public static final String COL_CODIGO = "codigo";
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_INGREDIENTES = "ingredientes";
    public static final String COL_PROCESO = "proceso";
    public static final String COL_IMAGEN = "imagen";
    public static final String COL_FAVORITO = "favorito"; // Nueva columna

    private static final String CREATE_TABLE_CATALOGO =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_CODIGO + " TEXT UNIQUE NOT NULL," +
                    COL_NOMBRE + " TEXT NOT NULL," +
                    COL_INGREDIENTES + " TEXT," +
                    COL_PROCESO + " TEXT," +
                    COL_IMAGEN + " TEXT," +
                    COL_FAVORITO + " INTEGER DEFAULT 0)"; // 0 = false, 1 = true

    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CATALOGO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Agrega la columna favorito si la BD es de versión anterior
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_FAVORITO + " INTEGER DEFAULT 0");
        }
    }

    // CREATE
    public boolean addReceta(String codigo, String nombre, String ingredientes, String proceso, String imagen) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CODIGO, codigo);
        values.put(COL_NOMBRE, nombre);
        values.put(COL_INGREDIENTES, ingredientes);
        values.put(COL_PROCESO, proceso);
        values.put(COL_IMAGEN, imagen);
        values.put(COL_FAVORITO, 0);

        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result != -1;
    }

    // READ - Todas las recetas
    public Cursor getAllRecetas() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, COL_NOMBRE + " ASC");
    }

    // READ - Recetas favoritas
    public Cursor getFavoritasRecetas() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null,
                COL_FAVORITO + " = ?", new String[]{"1"},
                null, null, COL_NOMBRE + " ASC");
    }

    // READ - Por código
    public Cursor getRecetaByCodigo(String codigo) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null,
                COL_CODIGO + " = ?", new String[]{codigo},
                null, null, null);
    }

    // READ - Por nombre
    public Cursor getRecetaByNombre(String nombre) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null,
                COL_NOMBRE + " LIKE ?", new String[]{"%" + nombre + "%"},
                null, null, null);
    }

    // READ - Por ID
    public Cursor getRecetaById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null,
                COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
    }

    // UPDATE
    public boolean updateReceta(long id, String codigo, String nombre, String ingredientes, String proceso, String imagen) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CODIGO, codigo);
        values.put(COL_NOMBRE, nombre);
        values.put(COL_INGREDIENTES, ingredientes);
        values.put(COL_PROCESO, proceso);
        values.put(COL_IMAGEN, imagen);

        int rowsAffected = db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    // UPDATE - Cambiar estado de favorito
    public boolean toggleFavorito(long id, boolean esFavorito) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FAVORITO, esFavorito ? 1 : 0);

        int rowsAffected = db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    // DELETE
    public boolean deleteReceta(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    // BUSCAR por ingredientes (para sugerencias de IA)
    public Cursor buscarPorIngredientes(String[] ingredientes) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Construye la consulta WHERE con LIKE para cada ingrediente
        StringBuilder whereClause = new StringBuilder();
        String[] whereArgs = new String[ingredientes.length];

        for (int i = 0; i < ingredientes.length; i++) {
            if (i > 0) {
                whereClause.append(" OR ");
            }
            whereClause.append(COL_INGREDIENTES + " LIKE ?");
            whereArgs[i] = "%" + ingredientes[i] + "%";
        }

        return db.query(TABLE_NAME, null,
                whereClause.toString(), whereArgs,
                null, null, COL_NOMBRE + " ASC");
    }
}