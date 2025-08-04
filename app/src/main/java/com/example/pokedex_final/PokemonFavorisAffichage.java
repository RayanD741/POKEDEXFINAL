package com.example.pokedex_final;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Classes.DBUtil;
import Classes.Pokemon;
import Classes.PokemonAdapter;

public class PokemonFavorisAffichage extends AppCompatActivity {

    private RecyclerView recyclerViewFavoris;
    private DBUtil dbUtil;
    private List<Pokemon> favorisList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_favoris_affichage);

        recyclerViewFavoris = findViewById(R.id.recyclerFavoris);
        recyclerViewFavoris.setLayoutManager(new LinearLayoutManager(this));

        dbUtil = new DBUtil(this);
        favorisList = dbUtil.getAllFavoris(); // Vérifie que cette méthode existe bien

        if (favorisList == null || favorisList.isEmpty()) {
            Toast.makeText(this, "Aucun favori enregistré", Toast.LENGTH_SHORT).show();
        } else {
            PokemonAdapter adapter = new PokemonAdapter(this, favorisList);
            recyclerViewFavoris.setAdapter(adapter);
        }
    }
}
