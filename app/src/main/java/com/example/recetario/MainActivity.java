package com.example.recetario;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // ============ DECLARACIÓN DE VISTAS ============
    RecyclerView rvRecetas;
    FloatingActionButton fabAgregar;
    SearchView searchView; // Búsqueda unificada
    LinearLayout layoutChipInfo; // Contenedor del chip informativo
    TextView tvTipoBusqueda; // Texto que muestra el tipo de búsqueda

    // ============ VARIABLES DE LÓGICA ============
    DbHelper dbHelper;
    List<Receta> listaRecetas; // Lista que almacena las recetas
    RecetaAdapter adapter; // Adaptador para el RecyclerView

    // ============ MÉTODO onCreate ============
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Aplica el tema personalizado
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        // Inicializa el helper de base de datos
        dbHelper = new DbHelper(this);
        listaRecetas = new ArrayList<>();

        // ============ ENLAZAR VISTAS CON EL LAYOUT ============
        rvRecetas = findViewById(R.id.rvRecetas);
        fabAgregar = findViewById(R.id.fabAgregar);
        searchView = findViewById(R.id.searchView);
        layoutChipInfo = findViewById(R.id.layoutChipInfo);
        tvTipoBusqueda = findViewById(R.id.tvTipoBusqueda);

        // ============ CONFIGURAR RECYCLERVIEW ============
        // Establece el layout manager (lista vertical)
        rvRecetas.setLayoutManager(new LinearLayoutManager(this));
        // Crea e inicializa el adaptador
        adapter = new RecetaAdapter(this, listaRecetas);
        rvRecetas.setAdapter(adapter);

        // ============ CONFIGURAR LISTENERS ============

        // Listener del FAB: Abre la actividad para agregar nueva receta
        fabAgregar.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AddEditActivity.class);
            startActivity(i);
        });

        // ============ BÚSQUEDA INTELIGENTE UNIFICADA ============
        // Listener que detecta cambios en el texto de búsqueda
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            // Método que se ejecuta al presionar Enter/Buscar
            @Override
            public boolean onQueryTextSubmit(String query) {
                realizarBusqueda(query);
                return true;
            }

            // Método que se ejecuta cada vez que cambia el texto
            @Override
            public boolean onQueryTextChange(String newText) {
                // Si el campo está vacío, muestra todas las recetas
                if (newText.isEmpty()) {
                    layoutChipInfo.setVisibility(View.GONE); // Oculta el chip informativo
                    loadRecetas(null, false); // Carga todas las recetas
                } else if (newText.length() >= 2) {
                    // Búsqueda en tiempo real después de 2 caracteres
                    realizarBusqueda(newText);
                }
                return true;
            }
        });
    }

    // ============ MÉTODO onResume ============
    // Se ejecuta cada vez que la actividad vuelve al primer plano
    @Override
    protected void onResume() {
        super.onResume();
        // Recarga todas las recetas (útil después de agregar/editar/eliminar)
        loadRecetas(null, false);
    }

    // ============ MÉTODO DE BÚSQUEDA INTELIGENTE ============
    /**
     * Determina automáticamente si la búsqueda es por código o por nombre
     * Criterio: Si el texto contiene números o empieza con 'R', busca por código
     *           De lo contrario, busca por nombre
     */
    private void realizarBusqueda(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadRecetas(null, false);
            layoutChipInfo.setVisibility(View.GONE);
            return;
        }

        String queryTrim = query.trim();
        boolean esPorCodigo = false;

        // ============ LÓGICA DE DETECCIÓN ============
        // Detecta si es búsqueda por código basándose en patrones comunes
        if (queryTrim.matches(".*\\d.*")) {
            // Contiene números (ej: R001, 001, REC123)
            esPorCodigo = true;
        } else if (queryTrim.toUpperCase().startsWith("R")) {
            // Empieza con R (ej: R, RC, REC)
            esPorCodigo = true;
        }

        // ============ MOSTRAR INDICADOR VISUAL ============
        // Muestra un chip indicando el tipo de búsqueda
        layoutChipInfo.setVisibility(View.VISIBLE);
        if (esPorCodigo) {
            tvTipoBusqueda.setText("🔍 Buscando por: Código");
            loadRecetas(queryTrim.toUpperCase(), true); // Búsqueda por código (mayúsculas)
        } else {
            tvTipoBusqueda.setText("🔍 Buscando por: Nombre");
            loadRecetas(queryTrim, false); // Búsqueda por nombre
        }
    }

    // ============ MÉTODO PARA CARGAR RECETAS ============
    /**
     * Carga las recetas desde la base de datos
     * @param query Término de búsqueda (null para cargar todas)
     * @param porCodigo true si busca por código, false si busca por nombre
     */
    private void loadRecetas(String query, boolean porCodigo) {
        // Limpia la lista actual
        listaRecetas.clear();
        Cursor cursor = null;

        // ============ EJECUTAR CONSULTA SEGÚN EL TIPO ============
        if (query == null || query.isEmpty()) {
            // Sin filtro: trae todas las recetas
            cursor = dbHelper.getAllRecetas();
        } else if (porCodigo) {
            // Búsqueda por código
            cursor = dbHelper.getRecetaByCodigo(query);
        } else {
            // Búsqueda por nombre
            cursor = dbHelper.getRecetaByNombre(query);
        }

        // Valida que el cursor no sea nulo
        if (cursor == null) {
            Toast.makeText(this, "Error al consultar la base de datos", Toast.LENGTH_SHORT).show();
            return;
        }

        // ============ FEEDBACK AL USUARIO ============
        // Muestra mensaje si no hay resultados en una búsqueda
        if (cursor.getCount() == 0 && query != null) {
            Toast.makeText(this, "No se encontraron recetas con: " + query, Toast.LENGTH_SHORT).show();
        }

        // ============ PROCESAR RESULTADOS ============
        // Recorre el cursor y crea objetos Receta
        if (cursor.moveToFirst()) {
            do {
                listaRecetas.add(new Receta(
                        cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_CODIGO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_NOMBRE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_INGREDIENTES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_PROCESO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_IMAGEN))
                ));
            } while (cursor.moveToNext());
        }

        // Cierra el cursor para liberar recursos
        cursor.close();

        // Notifica al adaptador que los datos han cambiado
        adapter.notifyDataSetChanged();
    }
}