package com.example.recetario;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView rvRecetas;
    SearchView searchView;
    LinearLayout layoutChipInfo, layoutBanner;
    TextView tvTipoBusqueda, tvBannerTitle, tvBannerSubtitle;
    BottomNavigationView bottomNavigationView;

    DbHelper dbHelper;
    List<Receta> listaRecetas;
    RecetaAdapter adapter;

    private int currentView = R.id.nav_home; // Vista actual (home por defecto)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        dbHelper = new DbHelper(this);
        listaRecetas = new ArrayList<>();

        // Vincular vistas
        rvRecetas = findViewById(R.id.rvRecetas);
        searchView = findViewById(R.id.searchView);
        layoutChipInfo = findViewById(R.id.layoutChipInfo);
        tvTipoBusqueda = findViewById(R.id.tvTipoBusqueda);
        layoutBanner = findViewById(R.id.layoutBanner);
        tvBannerTitle = findViewById(R.id.tvBannerTitle);
        tvBannerSubtitle = findViewById(R.id.tvBannerSubtitle);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Configurar RecyclerView
        rvRecetas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecetaAdapter(this, listaRecetas, new RecetaAdapter.OnFavoritoClickListener() {
            @Override
            public void onFavoritoClick(Receta receta, int position) {
                toggleFavorito(receta, position);
            }
        });
        rvRecetas.setAdapter(adapter);

        // Configurar Bottom Navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    currentView = R.id.nav_home;
                    searchView.setVisibility(View.VISIBLE);
                    layoutBanner.setVisibility(View.VISIBLE);
                    updateBanner("Mi Recetario", "Explora todas tus recetas favoritas");
                    loadRecetas(null, false, false);
                    return true;
                } else if (itemId == R.id.nav_add) {
                    Intent i = new Intent(MainActivity.this, AddEditActivity.class);
                    startActivity(i);
                    return true;
                } else if (itemId == R.id.nav_favorites) {
                    currentView = R.id.nav_favorites;
                    searchView.setVisibility(View.GONE);
                    layoutChipInfo.setVisibility(View.GONE);
                    layoutBanner.setVisibility(View.VISIBLE);
                    updateBanner("Mis Favoritos ❤️", "Tus recetas guardadas con amor");
                    loadRecetas(null, false, true);
                    return true;
                } else if (itemId == R.id.nav_ia) {
                    Intent i = new Intent(MainActivity.this, SugerenciaIAActivity.class);
                    startActivity(i);
                    return true;
                }
                return false;
            }
        });

        // Búsqueda inteligente
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                realizarBusqueda(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    layoutChipInfo.setVisibility(View.GONE);
                    loadRecetas(null, false, currentView == R.id.nav_favorites);
                } else if (newText.length() >= 2) {
                    realizarBusqueda(newText);
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar según la vista actual
        if (currentView == R.id.nav_favorites) {
            loadRecetas(null, false, true);
        } else {
            loadRecetas(null, false, false);
        }
    }

    private void updateBanner(String title, String subtitle) {
        tvBannerTitle.setText(title);
        tvBannerSubtitle.setText(subtitle);
    }

    private void realizarBusqueda(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadRecetas(null, false, currentView == R.id.nav_favorites);
            layoutChipInfo.setVisibility(View.GONE);
            return;
        }

        String queryTrim = query.trim();
        boolean esPorCodigo = queryTrim.matches(".*\\d.*") || queryTrim.toUpperCase().startsWith("R");

        layoutChipInfo.setVisibility(View.VISIBLE);
        if (esPorCodigo) {
            tvTipoBusqueda.setText("🔍 Buscando por: Código");
            loadRecetas(queryTrim.toUpperCase(), true, false);
        } else {
            tvTipoBusqueda.setText("🔍 Buscando por: Nombre");
            loadRecetas(queryTrim, false, false);
        }
    }

    private void loadRecetas(String query, boolean porCodigo, boolean soloFavoritos) {
        listaRecetas.clear();
        Cursor cursor = null;

        if (soloFavoritos) {
            cursor = dbHelper.getFavoritasRecetas();
        } else if (query == null || query.isEmpty()) {
            cursor = dbHelper.getAllRecetas();
        } else if (porCodigo) {
            cursor = dbHelper.getRecetaByCodigo(query);
        } else {
            cursor = dbHelper.getRecetaByNombre(query);
        }

        if (cursor == null) {
            Toast.makeText(this, "Error al consultar la base de datos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cursor.getCount() == 0 && query != null) {
            Toast.makeText(this, "No se encontraron recetas", Toast.LENGTH_SHORT).show();
        }

        if (cursor.moveToFirst()) {
            do {
                int favoritoValue = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.COL_FAVORITO));
                listaRecetas.add(new Receta(
                        cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_CODIGO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_NOMBRE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_INGREDIENTES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_PROCESO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_IMAGEN)),
                        favoritoValue == 1
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void toggleFavorito(Receta receta, int position) {
        boolean nuevoEstado = !receta.esFavorito();
        boolean result = dbHelper.toggleFavorito(receta.getId(), nuevoEstado);

        if (result) {
            receta.setEsFavorito(nuevoEstado);
            adapter.notifyItemChanged(position);

            String mensaje = nuevoEstado ? "Agregado a favoritos ❤️" : "Removido de favoritos";
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();

            // Si estamos en la vista de favoritos y se desmarcó, recargamos
            if (currentView == R.id.nav_favorites && !nuevoEstado) {
                loadRecetas(null, false, true);
            }
        }
    }
}