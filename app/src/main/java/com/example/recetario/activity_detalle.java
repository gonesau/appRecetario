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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class activity_detalle extends AppCompatActivity {

    ImageView imgDetalle;
    TextView tvTitulo, tvDescripcion;
    LinearLayout layoutIngredientesDetalle, layoutProcedimientoDetalle;
    Button btnRegresar, btnEditar, btnEliminar;
    ImageButton btnBack;
    FloatingActionButton fabFavorito;

    DbHelper dbHelper;
    private Receta recetaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_detalle);

        dbHelper = new DbHelper(this);

        // Vincular vistas
        imgDetalle = findViewById(R.id.imgDetalle);
        tvTitulo = findViewById(R.id.tvTituloDetalle);
        tvDescripcion = findViewById(R.id.tvDescripcionDetalle);
        layoutIngredientesDetalle = findViewById(R.id.layoutIngredientesDetalle);
        layoutProcedimientoDetalle = findViewById(R.id.layoutProcedimientoDetalle);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnBack = findViewById(R.id.btnBack);
        fabFavorito = findViewById(R.id.fabFavorito);

        // Cargar datos
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("RECETA_OBJ")) {
            recetaActual = (Receta) extras.getSerializable("RECETA_OBJ");

            if (recetaActual != null) {
                tvTitulo.setText(recetaActual.getNombre());
                tvDescripcion.setText("Código: " + recetaActual.getCodigo());

                // Cargar imagen
                String imgPath = recetaActual.getImagen();
                try {
                    if (imgPath != null && !imgPath.isEmpty()) {
                        Uri imgUri = Uri.parse(imgPath);
                        imgDetalle.setImageURI(imgUri);
                    } else {
                        imgDetalle.setImageResource(R.drawable.receta1);
                    }
                } catch (Exception e) {
                    imgDetalle.setImageResource(R.drawable.receta1);
                }

                // Configurar FAB de favorito
                updateFabFavorito();

                // Cargar ingredientes
                String ingredientes = recetaActual.getIngredientes();
                if (ingredientes != null && !ingredientes.isEmpty()) {
                    String[] ingredientesArray = ingredientes.split("\n");
                    for (String ingrediente : ingredientesArray) {
                        if (!ingrediente.trim().isEmpty()) {
                            agregarIngredienteConCheck(ingrediente.trim());
                        }
                    }
                }

                // Cargar procedimiento
                String proceso = recetaActual.getProceso();
                if (proceso != null && !proceso.isEmpty()) {
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

        // Listeners
        btnRegresar.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());

        btnEditar.setOnClickListener(v -> {
            if (recetaActual != null) {
                Intent i = new Intent(activity_detalle.this, AddEditActivity.class);
                i.putExtra("RECETA_ID", recetaActual.getId());
                startActivity(i);
                finish();
            }
        });

        btnEliminar.setOnClickListener(v -> {
            if (recetaActual != null) {
                mostrarDialogoConfirmacion();
            }
        });

        fabFavorito.setOnClickListener(v -> toggleFavorito());
    }

    private void updateFabFavorito() {
        if (recetaActual.esFavorito()) {
            fabFavorito.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            fabFavorito.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void toggleFavorito() {
        boolean nuevoEstado = !recetaActual.esFavorito();
        boolean result = dbHelper.toggleFavorito(recetaActual.getId(), nuevoEstado);

        if (result) {
            recetaActual.setEsFavorito(nuevoEstado);
            updateFabFavorito();

            String mensaje = nuevoEstado ? "Agregado a favoritos ❤️" : "Removido de favoritos";
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    private void agregarIngredienteConCheck(String ingrediente) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        checkBox.setText("✓ " + ingrediente);
        checkBox.setTextSize(15);
        checkBox.setPadding(8, 12, 8, 12);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                checkBox.setPaintFlags(checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        });

        layoutIngredientesDetalle.addView(checkBox);
    }

    private void agregarPasoConCheck(int numPaso, String paso) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        checkBox.setText(numPaso + ". " + paso);
        checkBox.setTextSize(15);
        checkBox.setPadding(8, 12, 8, 12);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                checkBox.setPaintFlags(checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        });

        layoutProcedimientoDetalle.addView(checkBox);
    }

    private void mostrarDialogoConfirmacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Receta")
                .setMessage("¿Está seguro de querer eliminar '" + recetaActual.getNombre() + "'?")
                .setPositiveButton("Sí, Eliminar", (dialog, which) -> eliminarReceta())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void eliminarReceta() {
        boolean result = dbHelper.deleteReceta(recetaActual.getId());
        if (result) {
            Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
        }
    }
}