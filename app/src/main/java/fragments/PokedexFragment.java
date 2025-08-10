package fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokedex_final.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Classes.PokeApi;
import Classes.Pokemon;
import Classes.PokemonAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PokedexFragment extends Fragment {

    // UI
    private RecyclerView recyclerView;
    private Spinner spinnerType, spinnerGeneration;
    private EditText poidsMin, poidsMax, tailleMin, tailleMax;
    private Button btnFiltrer;

    // Données
    private PokemonAdapter adapter;
    private final List<Pokemon> listePokemon = new ArrayList<>(); // tous
    private final List<Pokemon> listeFiltre  = new ArrayList<>(); // affichés

    // Constantes
    private static final int NOMBRE_TOTAL = 1025;
    private static final String[] TYPES = {"Tous", "Feu", "Eau", "Plante", "Électrik", "Roche", "Spectre"};
    private static final String[] GENERATIONS = {"Toutes", "Gen I", "Gen II", "Gen III", "Gen IV", "Gen V", "Gen VI", "Gen VII", "Gen VIII", "Gen IX"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pokedex, container, false);

        // Liaison UI
        recyclerView      = view.findViewById(R.id.recyclerPokedex);
        spinnerType       = view.findViewById(R.id.spinnerType);
        spinnerGeneration = view.findViewById(R.id.spinnerGeneration);
        poidsMin          = view.findViewById(R.id.editPoidsMin);
        poidsMax          = view.findViewById(R.id.editPoidsMax);
        tailleMin         = view.findViewById(R.id.editTailleMin);
        tailleMax         = view.findViewById(R.id.editTailleMax);
        btnFiltrer        = view.findViewById(R.id.btnFiltrer);

        // RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new PokemonAdapter(getContext(), listeFiltre);
        recyclerView.setAdapter(adapter);

        // Spinners (IMPORTANT : évite getSelectedItem() = null)
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, TYPES);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setSelection(0, false); // "Tous"

        ArrayAdapter<String> genAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, GENERATIONS);
        spinnerGeneration.setAdapter(genAdapter);
        spinnerGeneration.setSelection(0, false); // "Toutes"

        // Bouton filtrer
        btnFiltrer.setOnClickListener(v -> appliquerFiltre());

        // Charger les données
        chargerPokemonDepuisAPI();

        return view;
    }

    private void chargerPokemonDepuisAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PokeApi api = retrofit.create(PokeApi.class);

        // Simple : 1 requête par ID (oui c’est long, mais c’est l’option "ultra simple")
        for (int i = 1; i <= NOMBRE_TOTAL; i++) {
            Call<Pokemon> call = api.getPokemon(i);
            call.enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (!isAdded()) return; // fragment plus visible
                    if (response.isSuccessful() && response.body() != null) {
                        Pokemon p = response.body();
                        // Alimenter la liste principale
                        listePokemon.add(p);
                        // Afficher tant qu’aucun filtre n’est appliqué
                        listeFiltre.add(p);
                        adapter.notifyItemInserted(listeFiltre.size() - 1);
                    }
                }

                @Override
                public void onFailure(Call<Pokemon> call, Throwable t) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Erreur API", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void appliquerFiltre() {
        // Lecture "safe"
        String typeSel = safe(spinnerType != null ? spinnerType.getSelectedItem() : null); // "tous" si null
        // (Génération non appliquée pour l’instant)
        double pMin = parseOrDefault(textOf(poidsMin), 0);
        double pMax = parseOrDefault(textOf(poidsMax), Double.MAX_VALUE);
        double tMin = parseOrDefault(textOf(tailleMin), 0);
        double tMax = parseOrDefault(textOf(tailleMax), Double.MAX_VALUE);

        // Snapshot pour éviter ConcurrentModificationException pendant que l’API ajoute
        List<Pokemon> snapshot = new ArrayList<>(listePokemon);

        listeFiltre.clear();

        for (Pokemon p : snapshot) {
            if (p == null) continue;

            String type  = p.getType();
            double poids = p.getPoidsEnKilo();
            double taille= p.getTailleEnMetre();

            boolean matchType   = "tous".equals(typeSel) ||
                    (type != null && type.toLowerCase(Locale.ROOT).equals(typeSel));
            boolean matchPoids  = poids >= pMin && poids <= pMax;
            boolean matchTaille = taille >= tMin && taille <= tMax;

            if (matchType && matchPoids && matchTaille) {
                listeFiltre.add(p);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // Helpers

    private String textOf(EditText e) {
        return e == null ? null : e.getText().toString();
    }

    private String safe(Object o) {
        return o == null ? "tous" : o.toString().trim().toLowerCase(Locale.ROOT);
    }

    private double parseOrDefault(String s, double def) {
        try {
            if (s == null) return def;
            s = s.trim().replace(',', '.'); // accepte virgule
            return s.isEmpty() ? def : Double.parseDouble(s);
        } catch (Exception e) {
            return def;
        }
    }
}
