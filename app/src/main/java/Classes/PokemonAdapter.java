package Classes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokedex_final.DetailPokemon;
import com.example.pokedex_final.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {

    public static final String EXTRA_POKEMON_ID = "com.example.pokedex_final.EXTRA_POKEMON_ID";

    private Context context;
    private List<Pokemon> pokemonList;
    private OnPokemonClickListener listener;

    public PokemonAdapter(Context context, List<Pokemon> pokemonList) {
        this.context = context;
        this.pokemonList = pokemonList;
        this.listener = null;
    }

    public PokemonAdapter(Context context, List<Pokemon> pokemonList, OnPokemonClickListener listener) {
        this.context = context;
        this.pokemonList = pokemonList;
        this.listener = listener;
    }

    public void setPokemonList(List<Pokemon> list) {
        this.pokemonList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pokemon_card_layout, parent, false);
        return new PokemonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokemonViewHolder holder, int position) {
        Pokemon pokemon = pokemonList.get(position);

        holder.nom.setText(pokemon.getNomFr());
        holder.numero.setText("#" + pokemon.getId());

        String imageUrl = pokemon.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_pokemon)
                    .error(R.drawable.error_pokemon)
                    .into(holder.image);
        } else if (pokemon.getSprites() != null && pokemon.getSprites().getFrontDefault() != null) {
            Picasso.get()
                    .load(pokemon.getSprites().getFrontDefault())
                    .placeholder(R.drawable.placeholder_pokemon)
                    .error(R.drawable.error_pokemon)
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPokemonClick(pokemon);
            } else {
                Intent intent = new Intent(context, DetailPokemon.class);
                intent.putExtra(EXTRA_POKEMON_ID, pokemon.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (pokemonList == null) ? 0 : pokemonList.size();
    }

    public static class PokemonViewHolder extends RecyclerView.ViewHolder {
        TextView nom, numero;
        ImageView image;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.nomPokemon);
            numero = itemView.findViewById(R.id.numeroPokemon);
            image = itemView.findViewById(R.id.imagePokemon);
        }
    }

    public interface OnPokemonClickListener {
        void onPokemonClick(Pokemon pokemon);
    }
}
