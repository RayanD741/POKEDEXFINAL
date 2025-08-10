package com.example.pokedex_final;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import Classes.DBUtil;
import Classes.Pokemon;
import Classes.PokemonAdapter;
import fragments.PokedexFragment;

public class DetailPokemon extends AppCompatActivity {

    private ImageView imagePokemon;
    private TextView nomPokemon, numeroPokemon, taillePokemon, poidsPokemon, typePokemon;
    private Button btnFavori;

    private Pokemon pokemon;
    private DBUtil dbUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pokemon);

        // Liaison UI
        imagePokemon = findViewById(R.id.imagePokemon);
        nomPokemon = findViewById(R.id.nomPokemon);
        numeroPokemon = findViewById(R.id.numeroPokemon);
        taillePokemon = findViewById(R.id.taillePokemon);
        poidsPokemon = findViewById(R.id.poidsPokemon);
        typePokemon = findViewById(R.id.typesPokemon);
        btnFavori = findViewById(R.id.btnFavori);
        Button btnRetour = findViewById(R.id.btnRetourDetail);
        btnRetour.setOnClickListener(v -> finish()); // ou autre action

        pokemon = getIntent().getParcelableExtra(PokemonAdapter.EXTRA_POKEMON_ID);
        if (pokemon == null) {
            Toast.makeText(this, "Erreur : Pokémon introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }



        // Remplir les champs
        nomPokemon.setText(pokemon.getNomFr());
        numeroPokemon.setText("#" + pokemon.getId());
        taillePokemon.setText(pokemon.getTailleEnMetre() + " m");
        poidsPokemon.setText(pokemon.getPoidsEnKilo() + " kg");
        typePokemon.setText(pokemon.getType());

        if (pokemon.getImageUrl() != null) {
            Picasso.get()
                    .load(pokemon.getImageUrl())
                    .placeholder(R.drawable.placeholder_pokemon)
                    .error(R.drawable.error_pokemon)
                    .into(imagePokemon);
        }

        // Favoris
        dbUtil = new DBUtil(this);
        updateFavoriButton();

        btnFavori.setOnClickListener(v -> {
            if (dbUtil.estFavori(pokemon.getId())) {
                dbUtil.supprimerFavori(pokemon.getId());
                Toast.makeText(this, "Retiré des favoris", Toast.LENGTH_SHORT).show();
            } else {
                dbUtil.ajouterFavori(pokemon);
                Toast.makeText(this, "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
            }
            updateFavoriButton();
        });
    }

    private void updateFavoriButton() {
        boolean estFavori = dbUtil.estFavori(pokemon.getId());
        btnFavori.setText(estFavori ? "Retirer des favoris" : "Ajouter aux favoris");
    }
}
