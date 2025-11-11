package com.example.recetario;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class AddEditActivity extends AppCompatActivity {

    // ============ DECLARACIÓN DE VISTAS ============
    TextInputLayout tilCodigo, tilNombre;
    EditText etCodigo, etNombre;
    EditText etIngredienteInput, etProcesoInput; // Campos para ingresar ingredientes y pasos
    Button btnAgregarIngrediente, btnAgregarPaso; // Botones para agregar a las listas
    LinearLayout layoutIngredientes, layoutPasos; // Contenedores dinámicos
    ImageView imgPreview;
    Button btnSeleccionarImagen, btnGuardar;
    ImageButton btnBack;
    TextView tvTituloBarra;

    // ============ VARIABLES DE LÓGICA ============
    DbHelper dbHelper;
    private boolean isUpdateMode = false; // Bandera para saber si estamos editando o creando
    private long recetaIdToUpdate = -1; // ID de la receta en modo edición
    private Uri imagenUri = null; // URI de la imagen seleccionada

    // Listas dinámicas para almacenar ingredientes y pasos
    private List<String> listaIngredientes = new ArrayList<>();
    private List<String> listaPasos = new ArrayList<>();

    // ============ LAUNCHER PARA SELECCIONAR IMAGEN ============
    // Registra un contrato de resultado para obtener contenido (imagen) de la galería
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Intenta obtener permisos persistentes para la URI
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    // Guarda la URI y muestra la imagen en el preview
                    imagenUri = uri;
                    imgPreview.setImageURI(imagenUri);
                }
            }
    );

    // ============ MÉTODO onCreate ============
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Aplica el tema personalizado
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_add_edit);

        // Inicializa el helper de base de datos
        dbHelper = new DbHelper(this);

        // ============ ENLAZAR VISTAS CON EL LAYOUT ============
        tilCodigo = findViewById(R.id.tilCodigo);
        tilNombre = findViewById(R.id.tilNombre);
        etCodigo = findViewById(R.id.etCodigo);
        etNombre = findViewById(R.id.etNombre);
        etIngredienteInput = findViewById(R.id.etIngredienteInput);
        etProcesoInput = findViewById(R.id.etProcesoInput);
        btnAgregarIngrediente = findViewById(R.id.btnAgregarIngrediente);
        btnAgregarPaso = findViewById(R.id.btnAgregarPaso);
        layoutIngredientes = findViewById(R.id.layoutIngredientes);
        layoutPasos = findViewById(R.id.layoutPasos);
        imgPreview = findViewById(R.id.imgPreview);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnBack = findViewById(R.id.btnBackAddEdit);
        tvTituloBarra = findViewById(R.id.tvTituloAddEdit);

        // ============ CONFIGURAR LISTENERS ============

        // Botón de retroceso en la barra superior
        btnBack.setOnClickListener(v -> finish());

        // Botón para seleccionar imagen de la galería
        btnSeleccionarImagen.setOnClickListener(v -> {
            mGetContent.launch("image/*");
        });

        // Botón para agregar un ingrediente a la lista
        btnAgregarIngrediente.setOnClickListener(v -> {
            String ingrediente = etIngredienteInput.getText().toString().trim();
            if (!ingrediente.isEmpty()) {
                listaIngredientes.add(ingrediente); // Agrega a la lista
                agregarIngredienteALista(ingrediente); // Muestra en UI
                etIngredienteInput.setText(""); // Limpia el campo
                etIngredienteInput.requestFocus(); // Mantiene el foco
            } else {
                Toast.makeText(this, "Escribe un ingrediente", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón para agregar un paso al procedimiento
        btnAgregarPaso.setOnClickListener(v -> {
            String paso = etProcesoInput.getText().toString().trim();
            if (!paso.isEmpty()) {
                listaPasos.add(paso); // Agrega a la lista
                agregarPasoALista(paso); // Muestra en UI
                etProcesoInput.setText(""); // Limpia el campo
                etProcesoInput.requestFocus(); // Mantiene el foco
            } else {
                Toast.makeText(this, "Escribe un paso", Toast.LENGTH_SHORT).show();
            }
        });

        // ============ DETERMINAR MODO (CREAR O EDITAR) ============
        // Verifica si se está editando una receta existente
        if (getIntent().hasExtra("RECETA_ID")) {
            isUpdateMode = true;
            recetaIdToUpdate = getIntent().getLongExtra("RECETA_ID", -1);
            btnGuardar.setText("Actualizar Receta");
            setTitle("Editar Receta");
            tvTituloBarra.setText("Editar Receta");
            loadRecetaData(); // Carga los datos existentes
        } else {
            isUpdateMode = false;
            btnGuardar.setText("Guardar Receta");
            setTitle("Agregar Receta");
            tvTituloBarra.setText("Agregar Receta");
        }

        // Listener del botón guardar
        btnGuardar.setOnClickListener(v -> saveReceta());
    }

    // ============ MÉTODO PARA CARGAR DATOS EN MODO EDICIÓN ============
    private void loadRecetaData() {
        if (recetaIdToUpdate == -1) return;

        // Consulta la receta por ID
        Cursor cursor = dbHelper.getRecetaById(recetaIdToUpdate);
        if (cursor != null && cursor.moveToFirst()) {
            // Carga campos básicos
            etCodigo.setText(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_CODIGO)));
            etNombre.setText(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_NOMBRE)));

            // Carga ingredientes (separados por saltos de línea)
            String ingredientes = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_INGREDIENTES));
            if (ingredientes != null && !ingredientes.isEmpty()) {
                String[] ingredientesArray = ingredientes.split("\n");
                for (String ing : ingredientesArray) {
                    if (!ing.trim().isEmpty()) {
                        listaIngredientes.add(ing.trim());
                        agregarIngredienteALista(ing.trim());
                    }
                }
            }

            // Carga pasos del procedimiento (separados por saltos de línea)
            String proceso = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_PROCESO));
            if (proceso != null && !proceso.isEmpty()) {
                String[] pasosArray = proceso.split("\n");
                for (String paso : pasosArray) {
                    if (!paso.trim().isEmpty()) {
                        listaPasos.add(paso.trim());
                        agregarPasoALista(paso.trim());
                    }
                }
            }

            // Carga la imagen
            String uriString = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_IMAGEN));
            if (uriString != null && !uriString.isEmpty()) {
                imagenUri = Uri.parse(uriString);
                imgPreview.setImageURI(imagenUri);
            }

            cursor.close();
        }
    }

    // ============ MÉTODO PARA AGREGAR INGREDIENTE A LA UI ============
    private void agregarIngredienteALista(String ingrediente) {
        // Crea un LinearLayout horizontal para cada ingrediente
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(8, 8, 8, 8);

        // TextView para mostrar el ingrediente
        TextView tvIngrediente = new TextView(this);
        tvIngrediente.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));
        tvIngrediente.setText("• " + ingrediente);
        tvIngrediente.setTextSize(15);
        tvIngrediente.setPadding(8, 4, 8, 4);

        // Botón para eliminar el ingrediente
        Button btnEliminar = new Button(this);
        btnEliminar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        btnEliminar.setText("×");
        btnEliminar.setTextSize(20);
        btnEliminar.setPadding(16, 0, 16, 0);

        // Listener para eliminar el ingrediente
        btnEliminar.setOnClickListener(v -> {
            listaIngredientes.remove(ingrediente);
            layoutIngredientes.removeView(itemLayout);
        });

        // Agrega las vistas al layout del item
        itemLayout.addView(tvIngrediente);
        itemLayout.addView(btnEliminar);

        // Agrega el item al contenedor principal
        layoutIngredientes.addView(itemLayout);
    }

    // ============ MÉTODO PARA AGREGAR PASO A LA UI ============
    private void agregarPasoALista(String paso) {
        // Crea un LinearLayout horizontal para cada paso
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(8, 8, 8, 8);

        // TextView para mostrar el número de paso y su descripción
        TextView tvPaso = new TextView(this);
        tvPaso.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));
        int numeroPaso = listaPasos.indexOf(paso) + 1;
        tvPaso.setText(numeroPaso + ". " + paso);
        tvPaso.setTextSize(15);
        tvPaso.setPadding(8, 4, 8, 4);

        // Botón para eliminar el paso
        Button btnEliminar = new Button(this);
        btnEliminar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        btnEliminar.setText("×");
        btnEliminar.setTextSize(20);
        btnEliminar.setPadding(16, 0, 16, 0);

        // Listener para eliminar el paso
        btnEliminar.setOnClickListener(v -> {
            listaPasos.remove(paso);
            layoutPasos.removeView(itemLayout);
            renumerarPasos(); // Actualiza la numeración
        });

        // Agrega las vistas al layout del item
        itemLayout.addView(tvPaso);
        itemLayout.addView(btnEliminar);

        // Agrega el item al contenedor principal
        layoutPasos.addView(itemLayout);
    }

    // ============ MÉTODO PARA RENUMERAR PASOS ============
    // Actualiza la numeración de los pasos después de eliminar uno
    private void renumerarPasos() {
        for (int i = 0; i < layoutPasos.getChildCount(); i++) {
            LinearLayout itemLayout = (LinearLayout) layoutPasos.getChildAt(i);
            TextView tvPaso = (TextView) itemLayout.getChildAt(0);
            String textoActual = tvPaso.getText().toString();
            // Elimina el número anterior y agrega el nuevo
            String textoSinNumero = textoActual.substring(textoActual.indexOf(".") + 2);
            tvPaso.setText((i + 1) + ". " + textoSinNumero);
        }
    }

    // ============ MÉTODO PARA GUARDAR O ACTUALIZAR RECETA ============
    private void saveReceta() {
        // Obtiene los valores de los campos básicos
        String codigo = etCodigo.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();

        // Convierte las listas a strings con saltos de línea
        String ingredientes = TextUtils.join("\n", listaIngredientes);
        String proceso = TextUtils.join("\n", listaPasos);

        // Obtiene la URI de la imagen (si existe)
        String imagenUriString = (imagenUri != null) ? imagenUri.toString() : null;

        // ============ VALIDACIONES ============
        boolean esValido = true;

        // Valida código
        if (codigo.isEmpty()) {
            tilCodigo.setError("El código es obligatorio");
            esValido = false;
        } else {
            tilCodigo.setError(null);
        }

        // Valida nombre
        if (nombre.isEmpty()) {
            tilNombre.setError("El nombre es obligatorio");
            esValido = false;
        } else {
            tilNombre.setError(null);
        }

        // Valida que haya al menos un ingrediente
        if (listaIngredientes.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un ingrediente", Toast.LENGTH_SHORT).show();
            esValido = false;
        }

        // Valida que haya al menos un paso
        if (listaPasos.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un paso del procedimiento", Toast.LENGTH_SHORT).show();
            esValido = false;
        }

        // Si hay errores de validación, detiene el proceso
        if (!esValido) {
            Toast.makeText(this, "Por favor, completa todos los campos requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        // ============ GUARDAR O ACTUALIZAR EN BD ============
        boolean result;
        if (isUpdateMode) {
            // Actualiza la receta existente
            result = dbHelper.updateReceta(recetaIdToUpdate, codigo, nombre, ingredientes, proceso, imagenUriString);
            if (result) {
                Toast.makeText(this, "Receta actualizada con éxito", Toast.LENGTH_SHORT).show();
                finish(); // Cierra la actividad
            } else {
                Toast.makeText(this, "Error al actualizar la receta", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Crea una nueva receta
            result = dbHelper.addReceta(codigo, nombre, ingredientes, proceso, imagenUriString);
            if (result) {
                Toast.makeText(this, "Receta guardada con éxito", Toast.LENGTH_SHORT).show();
                finish(); // Cierra la actividad
            } else {
                Toast.makeText(this, "Error al guardar. ¿Código duplicado?", Toast.LENGTH_SHORT).show();
            }
        }
    }
}