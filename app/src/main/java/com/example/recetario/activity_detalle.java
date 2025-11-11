package com.example.recetario;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class activity_detalle extends AppCompatActivity {

    // ============ DECLARACIÓN DE VISTAS ============
    ImageView imgDetalle;
    TextView tvTitulo, tvDescripcion;
    LinearLayout layoutIngredientesDetalle, layoutProcedimientoDetalle; // Contenedores dinámicos
    Button btnRegresar, btnEditar, btnEliminar;
    ImageButton btnBack;

    // ============ VARIABLES DE LÓGICA ============
    DbHelper dbHelper;
    private Receta recetaActual; // Objeto con los datos de la receta actual

    // ============ MÉTODO onCreate ============
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Aplica el tema personalizado
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_detalle);

        // Inicializa el helper de base de datos
        dbHelper = new DbHelper(this);

        // ============ ENLAZAR VISTAS CON EL LAYOUT ============
        imgDetalle = findViewById(R.id.imgDetalle);
        tvTitulo = findViewById(R.id.tvTituloDetalle);
        tvDescripcion = findViewById(R.id.tvDescripcionDetalle);
        layoutIngredientesDetalle = findViewById(R.id.layoutIngredientesDetalle);
        layoutProcedimientoDetalle = findViewById(R.id.layoutProcedimientoDetalle);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnBack = findViewById(R.id.btnBack);

        // ============ CARGAR DATOS DE LA RECETA ============
        // Obtiene los extras del Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("RECETA_OBJ")) {
            // Obtiene el objeto Receta serializado
            recetaActual = (Receta) extras.getSerializable("RECETA_OBJ");

            if (recetaActual != null) {
                // Establece los datos básicos
                tvTitulo.setText(recetaActual.getNombre());
                tvDescripcion.setText("Código: " + recetaActual.getCodigo());

                // ============ CARGAR IMAGEN ============
                String imgPath = recetaActual.getImagen();
                try {
                    if (imgPath != null && !imgPath.isEmpty()) {
                        Uri imgUri = Uri.parse(imgPath);
                        imgDetalle.setImageURI(imgUri);
                    } else {
                        // Imagen por defecto si no hay ninguna
                        imgDetalle.setImageResource(R.mipmap.ic_launcher);
                    }
                } catch (Exception e) {
                    imgDetalle.setImageResource(R.mipmap.ic_launcher);
                }

                // ============ CARGAR INGREDIENTES CON CHECKBOXES ============
                String ingredientes = recetaActual.getIngredientes();
                if (ingredientes != null && !ingredientes.isEmpty()) {
                    // Divide los ingredientes por saltos de línea
                    String[] ingredientesArray = ingredientes.split("\n");
                    for (String ingrediente : ingredientesArray) {
                        if (!ingrediente.trim().isEmpty()) {
                            agregarIngredienteConCheck(ingrediente.trim());
                        }
                    }
                }

                // ============ CARGAR PROCEDIMIENTO CON CHECKBOXES ============
                String proceso = recetaActual.getProceso();
                if (proceso != null && !proceso.isEmpty()) {
                    // Divide los pasos por saltos de línea
                    String[] pasosArray = proceso.split("\n");
                    int numPaso = 1;
                    for (String paso : pasosArray) {
                        if (!paso.trim().isEmpty()) {
                            agregarPasoConCheck(numPaso, paso.trim());
                            numPaso++;
                        }
                    }
                }
            }
        }

        // ============ CONFIGURAR LISTENERS DE BOTONES ============

        // Botones de regresar (ambos hacen lo mismo)
        btnRegresar.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());

        // Botón editar: abre la actividad de edición con el ID de la receta
        btnEditar.setOnClickListener(v -> {
            if (recetaActual != null) {
                Intent i = new Intent(activity_detalle.this, AddEditActivity.class);
                i.putExtra("RECETA_ID", recetaActual.getId());
                startActivity(i);
                finish(); // Cierra esta actividad
            }
        });

        // Botón eliminar: muestra un diálogo de confirmación
        btnEliminar.setOnClickListener(v -> {
            if (recetaActual != null) {
                mostrarDialogoConfirmacion();
            }
        });
    }

    // ============ MÉTODO PARA AGREGAR INGREDIENTE CON CHECKBOX ============
    private void agregarIngredienteConCheck(String ingrediente) {
        // Crea un CheckBox para cada ingrediente
        CheckBox checkBox = new CheckBox(this);
        checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        // Agrega un ícono de check (✓) al inicio del texto
        checkBox.setText("✓ " + ingrediente);
        checkBox.setTextSize(15);
        checkBox.setPadding(8, 12, 8, 12);

        // ============ LISTENER PARA MARCAR/DESMARCAR ============
        // Cuando se marca el checkbox, se tacha el texto
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Agrega el efecto de tachado (strikethrough)
                checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                // Remueve el efecto de tachado
                checkBox.setPaintFlags(checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        });

        // Agrega el CheckBox al contenedor
        layoutIngredientesDetalle.addView(checkBox);
    }

    // ============ MÉTODO PARA AGREGAR PASO CON CHECKBOX ============
    private void agregarPasoConCheck(int numPaso, String paso) {
        // Crea un CheckBox para cada paso del procedimiento
        CheckBox checkBox = new CheckBox(this);
        checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        // Formatea el texto con número de paso
        checkBox.setText(numPaso + ". " + paso);
        checkBox.setTextSize(15);
        checkBox.setPadding(8, 12, 8, 12);

        // ============ LISTENER PARA MARCAR/DESMARCAR ============
        // Cuando se marca el checkbox, se tacha el texto
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Agrega el efecto de tachado (strikethrough)
                checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                // Remueve el efecto de tachado
                checkBox.setPaintFlags(checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        });

        // Agrega el CheckBox al contenedor
        layoutProcedimientoDetalle.addView(checkBox);
    }

    // ============ MÉTODO PARA MOSTRAR DIÁLOGO DE CONFIRMACIÓN ============
    private void mostrarDialogoConfirmacion() {
        // Crea un AlertDialog para confirmar la eliminación
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Receta")
                .setMessage("¿Está seguro de querer eliminar el registro '" + recetaActual.getNombre() + "'?")
                .setPositiveButton("Sí, Eliminar", (dialog, which) -> {
                    // Si confirma, ejecuta la eliminación
                    eliminarReceta();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Si cancela, solo cierra el diálogo
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // ============ MÉTODO PARA ELIMINAR LA RECETA ============
    private void eliminarReceta() {
        // Llama al método de eliminación del DbHelper
        boolean result = dbHelper.deleteReceta(recetaActual.getId());
        if (result) {
            // Si se eliminó correctamente
            Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show();
            finish(); // Cierra la actividad y regresa
        } else {
            // Si hubo un error
            Toast.makeText(this, "Error al eliminar la receta", Toast.LENGTH_SHORT).show();
        }
    }
}