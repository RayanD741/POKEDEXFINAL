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

import java.text.Normalizer;
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

public class FiltrerFragment extends Fragment {

    private Spinner spinnerType, spinnerGeneration;
    private EditText poidsMin, poidsMax, tailleMin, tailleMax;
    private Button btnAppliquer;
    private RecyclerView recyclerFiltre;

    private static final int NOMBRE_TOTAL = 1025;

    private final String[] types = {"Tous", "Feu", "Eau", "Plante", "Électrik", "Roche", "Spectre"};
    private final String[] generations = {"Toutes", "Gen I", "Gen II", "Gen III", "Gen IV", "Gen V", "Gen VI", "Gen VII", "Gen VIII", "Gen IX"};

    private PokemonAdapter adapter;
    private final List<Pokemon> listeComplete = new ArrayList<>(); // tous les Pokémon chargés
    private final List<Pokemon> listeAffichee = new ArrayList<>(); // liste filtrée affichée

    // ---- Critères courants (utilisés pour filtrer ce qui arrive de l’API) ----
    private String curType = "tous";  // canonique, sans accents, en minuscules
    private int curGenIdx = 0;        // 0 = toutes, 1..9
    private double curPmin = 0, curPmax = Double.MAX_VALUE;
    private double curTmin = 0, curTmax = Double.MAX_VALUE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_filtrer, container, false);

        // Liaison UI
        spinnerType = v.findViewById(R.id.spinnerType);
        spinnerGeneration = v.findViewById(R.id.spinnerGeneration);
        poidsMin = v.findViewById(R.id.poidsMin);
        poidsMax = v.findViewById(R.id.poidsMax);
        tailleMin = v.findViewById(R.id.tailleMin);
        tailleMax = v.findViewById(R.id.tailleMax);
        btnAppliquer = v.findViewById(R.id.btnAppliquer);
        recyclerFiltre = v.findViewById(R.id.recyclerFiltre);

        // Spinners (évite getSelectedItem() = null)
        spinnerType.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, types));
        spinnerGeneration.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, generations));
        spinnerType.setSelection(0, false);       // "Tous"
        spinnerGeneration.setSelection(0, false); // "Toutes"

        // RecyclerView
        recyclerFiltre.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new PokemonAdapter(getContext(), listeAffichee);
        recyclerFiltre.setAdapter(adapter);

        // Actions
        btnAppliquer.setOnClickListener(view -> {
            // 1) mettre à jour les critères courants
            curType   = canonType(safe(spinnerType.getSelectedItem()));
            curGenIdx = generationIndexFromSelection(safe(spinnerGeneration.getSelectedItem()));
            curPmin   = parseOrDefault(textOf(poidsMin), 0);
            curPmax   = parseOrDefault(textOf(poidsMax), Double.MAX_VALUE);
            curTmin   = parseOrDefault(textOf(tailleMin), 0);
            curTmax   = parseOrDefault(textOf(tailleMax), Double.MAX_VALUE);

            // 2) recalculer l’affichage à partir de la liste complète
            appliquerFiltreSurListeComplete();
        });

        // Charge tous les Pokémon (affiche selon critères courants — par défaut "Tous")
        chargerPokemonDepuisAPI();

        return v;
    }

    private void chargerPokemonDepuisAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        PokeApi api = retrofit.create(PokeApi.class);

        for (int i = 1; i <= NOMBRE_TOTAL; i++) {
            Call<Pokemon> call = api.getPokemon(i);
            call.enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        Pokemon p = response.body();

                        // Toujours alimenter la liste complète
                        listeComplete.add(p);

                        // ✅ N’ajouter à l’affichage que si ça matche les critères courants
                        if (matchCurrentCriteria(p)) {
                            listeAffichee.add(p);
                            adapter.notifyItemInserted(listeAffichee.size() - 1);
                        }
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

    /** Recalcule entièrement l’affichage selon les critères courants. */
    private void appliquerFiltreSurListeComplete() {
        List<Pokemon> snapshot = new ArrayList<>(listeComplete);
        listeAffichee.clear();

        for (Pokemon p : snapshot) {
            if (p != null && matchCurrentCriteria(p)) {
                listeAffichee.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }

    /** Teste les critères courants sur un Pokémon. */
    private boolean matchCurrentCriteria(Pokemon p) {
        // Type
        boolean okType = matchesType(p.getType(), curType);

        // Génération (depuis l’ID)
        int genOfId = generationIndexForId(p.getId());
        boolean okGen = (curGenIdx == 0) || (genOfId == curGenIdx);

        // Poids & taille
        double poids  = p.getPoidsEnKilo();
        double taille = p.getTailleEnMetre();
        boolean okPoids  = poids  >= curPmin && poids  <= curPmax;
        boolean okTaille = taille >= curTmin && taille <= curTmax;

        return okType && okGen && okPoids && okTaille;
    }

    // -------- Helpers filtrage --------

    // Compare le type sélectionné (canonique) avec les types du Pokémon (gère "Feu/Vol", "fire, flying", accents, etc.)
    private boolean matchesType(String pokemonTypeRaw, String selectedTypeCanonical) {
        if ("tous".equals(selectedTypeCanonical) || selectedTypeCanonical.isEmpty()) return true;
        if (pokemonTypeRaw == null) return false;

        String[] parts = pokemonTypeRaw.split("[,/|\\s]+"); // sépare par , / | ou espace
        for (String part : parts) {
            if (canonType(part).equals(selectedTypeCanonical)) return true;
        }
        return false;
    }

    // Met en forme un nom de type vers une clé canonique (français) sans accents
    private String canonType(String s) {
        String n = normalize(s);
        switch (n) {
            // Français natif
            case "tous": return "tous";
            case "feu": return "feu";
            case "eau": return "eau";
            case "plante": return "plante";
            case "electrik": return "electrik"; // "Électrik" → "electrik"
            case "roche": return "roche";
            case "spectre": return "spectre";
            // Équivalents anglais (au cas où l’API te renvoie en EN)
            case "fire": return "feu";
            case "water": return "eau";
            case "grass": return "plante";
            case "electric": return "electrik";
            case "rock": return "roche";
            case "ghost": return "spectre";
        }
        return n; // fallback (si tu ajoutes d’autres types plus tard)
    }

    private String normalize(String s) {
        if (s == null) return "";
        String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return noAccent.trim().toLowerCase(Locale.ROOT);
    }

    // Convertit "Gen I" -> 1, "Gen II" -> 2 ... "Toutes" -> 0
    private int generationIndexFromSelection(String sel) {
        sel = normalize(sel);
        if (sel.isEmpty() || "toutes".equals(sel)) return 0;
        switch (sel) {
            case "gen i":   return 1;
            case "gen ii":  return 2;
            case "gen iii": return 3;
            case "gen iv":  return 4;
            case "gen v":   return 5;
            case "gen vi":  return 6;
            case "gen vii": return 7;
            case "gen viii":return 8;
            case "gen ix":  return 9;
            default: return 0;
        }
    }

    // Map ID -> génération (PokeAPI, jusqu’à 1025)
    private int generationIndexForId(int id) {
        if (id <= 151) return 1;           // Gen I
        if (id <= 251) return 2;           // Gen II
        if (id <= 386) return 3;           // Gen III
        if (id <= 493) return 4;           // Gen IV
        if (id <= 649) return 5;           // Gen V
        if (id <= 721) return 6;           // Gen VI
        if (id <= 809) return 7;           // Gen VII
        if (id <= 905) return 8;           // Gen VIII
        return 9;                           // Gen IX (906–1025)
    }

    // -------- Helpers généraux --------

    private String textOf(EditText e) {
        return e == null ? null : e.getText().toString();
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString().trim();
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
