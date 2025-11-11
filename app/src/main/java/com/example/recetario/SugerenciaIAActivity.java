package com.example.recetario;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SugerenciaIAActivity extends AppCompatActivity {

    LinearLayout layoutIngredientes, layoutSeleccion, layoutResultado, layoutCargando;
    Button btnGenerarSugerencia, btnVerMas;
    ImageButton btnBack;
    TextView tvTituloResultado, tvIngredientesResultado;
    ProgressBar progressBar;

    private List<String> ingredientesDisponibles;
    private List<String> ingredientesSeleccionados = new ArrayList<>();
    private JSONArray recetasJSON;
    private Receta recetaSugerida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_sugerencia_ia);

        // Vincular vistas
        layoutIngredientes = findViewById(R.id.layoutIngredientes);
        layoutSeleccion = findViewById(R.id.layoutSeleccion);
        layoutResultado = findViewById(R.id.layoutResultado);
        layoutCargando = findViewById(R.id.layoutCargando);
        btnGenerarSugerencia = findViewById(R.id.btnGenerarSugerencia);
        btnVerMas = findViewById(R.id.btnVerMas);
        btnBack = findViewById(R.id.btnBackSugerencia);
        tvTituloResultado = findViewById(R.id.tvTituloResultado);
        tvIngredientesResultado = findViewById(R.id.tvIngredientesResultado);
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> finish());

        // Cargar ingredientes comunes de El Salvador
        cargarIngredientesComunes();

        // Cargar recetas predefinidas desde JSON
        cargarRecetasDesdeJSON();

        // Mostrar ingredientes en checkboxes
        mostrarIngredientes();

        btnGenerarSugerencia.setOnClickListener(v -> generarSugerencia());

        btnVerMas.setOnClickListener(v -> {
            if (recetaSugerida != null) {
                Intent intent = new Intent(SugerenciaIAActivity.this, activity_detalle.class);
                intent.putExtra("RECETA_OBJ", recetaSugerida);
                startActivity(intent);
            }
        });
    }

    private void cargarIngredientesComunes() {
        ingredientesDisponibles = new ArrayList<>();
        ingredientesDisponibles.add("Tomate");
        ingredientesDisponibles.add("Cebolla");
        ingredientesDisponibles.add("Frijoles");
        ingredientesDisponibles.add("Arroz");
        ingredientesDisponibles.add("Huevos");
        ingredientesDisponibles.add("Queso");
        ingredientesDisponibles.add("Crema");
        ingredientesDisponibles.add("Tortillas");
        ingredientesDisponibles.add("Plátano");
        ingredientesDisponibles.add("Aguacate");
        ingredientesDisponibles.add("Pollo");
        ingredientesDisponibles.add("Carne de res");
        ingredientesDisponibles.add("Ajo");
        ingredientesDisponibles.add("Chile verde");
        ingredientesDisponibles.add("Cilantro");
        ingredientesDisponibles.add("Limón");
        ingredientesDisponibles.add("Sal");
        ingredientesDisponibles.add("Azúcar");
        ingredientesDisponibles.add("Aceite");
        ingredientesDisponibles.add("Harina");
        ingredientesDisponibles.add("Leche");
        ingredientesDisponibles.add("Mantequilla");
        ingredientesDisponibles.add("Papa");
        ingredientesDisponibles.add("Zanahoria");
        ingredientesDisponibles.add("Loroco");
    }

    private void mostrarIngredientes() {
        for (String ingrediente : ingredientesDisponibles) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(ingrediente);
            checkBox.setTextSize(16);
            checkBox.setPadding(16, 12, 16, 12);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    ingredientesSeleccionados.add(ingrediente);
                } else {
                    ingredientesSeleccionados.remove(ingrediente);
                }

                // Actualizar botón
                if (ingredientesSeleccionados.size() > 0) {
                    btnGenerarSugerencia.setEnabled(true);
                    btnGenerarSugerencia.setText("Generar Receta (" + ingredientesSeleccionados.size() + " ingredientes)");
                } else {
                    btnGenerarSugerencia.setEnabled(false);
                    btnGenerarSugerencia.setText("Selecciona al menos 1 ingrediente");
                }
            });

            layoutIngredientes.addView(checkBox);
        }

        btnGenerarSugerencia.setEnabled(false);
    }

    private void cargarRecetasDesdeJSON() {
        try {
            InputStream is = getAssets().open("recetas_sugeridas.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");
            recetasJSON = new JSONArray(json);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar recetas predefinidas", Toast.LENGTH_SHORT).show();
        }
    }

    private void generarSugerencia() {
        if (ingredientesSeleccionados.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un ingrediente", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar pantalla de carga
        layoutSeleccion.setVisibility(View.GONE);
        layoutCargando.setVisibility(View.VISIBLE);
        layoutResultado.setVisibility(View.GONE);

        // Simular procesamiento de IA (2 segundos)
        new Handler().postDelayed(() -> {
            buscarMejorReceta();
        }, 2000);
    }

    private void buscarMejorReceta() {
        try {
            JSONObject mejorReceta = null;
            int maxCoincidencias = 0;

            // Buscar la receta con más coincidencias
            for (int i = 0; i < recetasJSON.length(); i++) {
                JSONObject receta = recetasJSON.getJSONObject(i);
                JSONArray ingredientesReceta = receta.getJSONArray("ingredientes");

                int coincidencias = 0;
                for (int j = 0; j < ingredientesReceta.length(); j++) {
                    String ing = ingredientesReceta.getString(j).toLowerCase();
                    for (String seleccionado : ingredientesSeleccionados) {
                        if (ing.contains(seleccionado.toLowerCase())) {
                            coincidencias++;
                            break;
                        }
                    }
                }

                if (coincidencias > maxCoincidencias) {
                    maxCoincidencias = coincidencias;
                    mejorReceta = receta;
                }
            }

            // Si no hay coincidencias, seleccionar una al azar
            if (mejorReceta == null && recetasJSON.length() > 0) {
                Random random = new Random();
                mejorReceta = recetasJSON.getJSONObject(random.nextInt(recetasJSON.length()));
            }

            if (mejorReceta != null) {
                mostrarResultado(mejorReceta);
            } else {
                Toast.makeText(this, "No se encontraron recetas", Toast.LENGTH_SHORT).show();
                layoutCargando.setVisibility(View.GONE);
                layoutSeleccion.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar recetas", Toast.LENGTH_SHORT).show();
            layoutCargando.setVisibility(View.GONE);
            layoutSeleccion.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarResultado(JSONObject recetaJSON) {
        try {
            String codigo = recetaJSON.getString("codigo");
            String nombre = recetaJSON.getString("nombre");
            JSONArray ingredientesArray = recetaJSON.getJSONArray("ingredientes");
            JSONArray pasosArray = recetaJSON.getJSONArray("proceso");

            // Convertir arrays a strings
            StringBuilder ingredientesStr = new StringBuilder();
            for (int i = 0; i < ingredientesArray.length(); i++) {
                ingredientesStr.append(ingredientesArray.getString(i));
                if (i < ingredientesArray.length() - 1) {
                    ingredientesStr.append("\n");
                }
            }

            StringBuilder procesosStr = new StringBuilder();
            for (int i = 0; i < pasosArray.length(); i++) {
                procesosStr.append(pasosArray.getString(i));
                if (i < pasosArray.length() - 1) {
                    procesosStr.append("\n");
                }
            }

            // Crear objeto Receta
            recetaSugerida = new Receta(
                    -1, // ID temporal
                    codigo,
                    nombre,
                    ingredientesStr.toString(),
                    procesosStr.toString(),
                    null, // Sin imagen
                    false
            );

            // Mostrar en UI
            tvTituloResultado.setText("🎉 " + nombre);
            tvIngredientesResultado.setText(ingredientesStr.toString().replace("\n", " • "));

            layoutCargando.setVisibility(View.GONE);
            layoutResultado.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al mostrar resultado", Toast.LENGTH_SHORT).show();
        }
    }
}