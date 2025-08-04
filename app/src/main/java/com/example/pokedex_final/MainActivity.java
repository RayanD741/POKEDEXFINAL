package com.example.pokedex_final;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import fragments.FavorisFragment;
import fragments.ParametresFragment;
import fragments.PokedexFragment;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Nouveau listener avec if/else (corrige le problème de constante dans switch)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.menu_pokedex) {
                selectedFragment = new PokedexFragment();
            } else if (itemId == R.id.menu_favoris) {
                selectedFragment = new FavorisFragment();
            } else if (itemId == R.id.nav_filtrer) {
                selectedFragment = new ParametresFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });

        // Affiche le fragment par défaut
        bottomNavigationView.setSelectedItemId(R.id.menu_pokedex);
    }
}
