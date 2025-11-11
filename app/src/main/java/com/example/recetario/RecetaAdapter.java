package com.example.recetario;

import android.content.Context;
import android.content.Intent;
import android.net.Uri; // Importar Uri
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecetaAdapter extends RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder> {

    private final Context context;
    private final List<Receta> listaRecetas;

    public RecetaAdapter(Context context, List<Receta> listaRecetas) {
        this.context = context;
        this.listaRecetas = listaRecetas;
    }

    @NonNull
    @Override
    public RecetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_receta, parent, false);
        return new RecetaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecetaViewHolder holder, int position) {
        Receta receta = listaRecetas.get(position);

        holder.tvTitulo.setText(receta.getNombre());
        // Mostramos el código como descripción
        holder.tvDesc.setText("Código: " + receta.getCodigo());


        // --- CAMBIO MEJORA: Cargar imagen desde URI ---
        String imgPath = receta.getImagen(); // Esto es la URI como String

        try {
            if (imgPath != null && !imgPath.isEmpty()) {
                Uri imgUri = Uri.parse(imgPath);
                // Cargar imagen desde la URI
                holder.imgReceta.setImageURI(imgUri);
            } else {
                // Si no hay imagen, poner una por defecto
                holder.imgReceta.setImageResource(R.mipmap.ic_launcher);
            }
        } catch (Exception e) {
            // Si la URI es inválida o el archivo fue borrado, poner imagen por defecto
            e.printStackTrace();
            holder.imgReceta.setImageResource(R.mipmap.ic_launcher);
        }
        // --- Fin del cambio ---


        // --- Click Listener para ir al Detalle ---
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, activity_detalle.class);
            i.putExtra("RECETA_OBJ", receta);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return listaRecetas.size();
    }

    // ViewHolder
    static class RecetaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgReceta;
        TextView tvTitulo, tvDesc;

        public RecetaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgReceta = itemView.findViewById(R.id.imgRecetaItem);
            tvTitulo = itemView.findViewById(R.id.tvTituloItem);
            tvDesc = itemView.findViewById(R.id.tvDescItem);
        }
    }
}