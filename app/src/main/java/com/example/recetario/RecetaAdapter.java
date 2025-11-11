package com.example.recetario;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
    private final OnFavoritoClickListener favoritoListener;

    public interface OnFavoritoClickListener {
        void onFavoritoClick(Receta receta, int position);
    }

    public RecetaAdapter(Context context, List<Receta> listaRecetas, OnFavoritoClickListener listener) {
        this.context = context;
        this.listaRecetas = listaRecetas;
        this.favoritoListener = listener;
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
        holder.tvDesc.setText("Código: " + receta.getCodigo());

        // Cargar imagen
        String imgPath = receta.getImagen();
        try {
            if (imgPath != null && !imgPath.isEmpty()) {
                Uri imgUri = Uri.parse(imgPath);
                holder.imgReceta.setImageURI(imgUri);
            } else {
                holder.imgReceta.setImageResource(R.drawable.receta1);
            }
        } catch (Exception e) {
            holder.imgReceta.setImageResource(R.drawable.receta1);
        }

        // Configurar icono de favorito
        if (receta.esFavorito()) {
            holder.imgFavorito.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            holder.imgFavorito.setImageResource(R.drawable.ic_favorite_border);
        }

        // Click en favorito
        holder.imgFavorito.setOnClickListener(v -> {
            if (favoritoListener != null) {
                favoritoListener.onFavoritoClick(receta, holder.getAdapterPosition());
            }
        });

        // Click en el item completo
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

    static class RecetaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgReceta, imgFavorito;
        TextView tvTitulo, tvDesc;

        public RecetaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgReceta = itemView.findViewById(R.id.imgRecetaItem);
            imgFavorito = itemView.findViewById(R.id.imgFavorito);
            tvTitulo = itemView.findViewById(R.id.tvTituloItem);
            tvDesc = itemView.findViewById(R.id.tvDescItem);
        }
    }
}