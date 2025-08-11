package fragments;

import android.annotation.SuppressLint;
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
import android.widget.CheckBox;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import Classes.PokeApi;
import Classes.Pokemon;
import Classes.PokemonAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FiltrerFragment extends Fragment {

    private Spinner spinnerType, spinnerGeneration;
    private EditText poidsMin, poidsMax, tailleMin, tailleMax;
    private Button btnAppliquer;
    private RecyclerView recyclerFiltre;
    private CheckBox chkTriNumero;

    private static final int NOMBRE_TOTAL = 1025;

    // listes existantes
    private final String[] types = {"Tous", "Feu", "Eau", "Plante", "Électrik", "Roche", "Spectre"};
    private final String[] generations = {"Toutes", "Gen I", "Gen II", "Gen III", "Gen IV", "Gen V", "Gen VI", "Gen VII", "Gen VIII", "Gen IX"};

    private PokemonAdapter adapter;
    private final List<Pokemon> listeComplete = new ArrayList<>();
    private final List<Pokemon> listeAffichee = new ArrayList<>();

    // critères courants
    private String curType = "all";   // "all","fire","water",...
    private int curGenIdx = 0;        // 0=toutes, 1..9
    private double curPmin = 0, curPmax = Double.MAX_VALUE;
    private double curTmin = 0, curTmax = Double.MAX_VALUE;

    @SuppressLint("WrongViewCast")
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
        chkTriNumero = v.findViewById(R.id.chkTriNumero);

        // Spinners
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

        // CheckBox: tri immédiat quand on coche/décoche

        chkTriNumero.setOnCheckedChangeListener((b, checked) -> appliquerFiltreSurListeComplete());

        // Bouton "Appliquer"
        btnAppliquer.setOnClickListener(view -> {
            // maj critères
            curType   = toTypeKey(spinnerType.getSelectedItem());                   // "all","fire",...
            curGenIdx = generationIndexFromSelection(spinnerGeneration.getSelectedItem());
            curPmin   = parseOrDefault(textOf(poidsMin), 0);
            curPmax   = parseOrDefault(textOf(poidsMax), Double.MAX_VALUE);
            curTmin   = parseOrDefault(textOf(tailleMin), 0);
            curTmax   = parseOrDefault(textOf(tailleMax), Double.MAX_VALUE);
            // recalc complet
            appliquerFiltreSurListeComplete();
        });

        // Chargement API
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

                        // Toujours stocker le complet
                        listeComplete.add(p);

                        // N’afficher que si ça match les critères courants
                        if (matchCurrentCriteria(p)) {
                            listeAffichee.add(p);
                            if (chkTriNumero != null && chkTriNumero.isChecked()) {
                                Collections.sort(listeAffichee, Comparator.comparingInt(Pokemon::getId));
                                adapter.notifyDataSetChanged();
                            } else {
                                adapter.notifyItemInserted(listeAffichee.size() - 1);
                            }
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

        if (chkTriNumero != null && chkTriNumero.isChecked()) {
            Collections.sort(listeAffichee, Comparator.comparingInt(Pokemon::getId));
        }
        adapter.notifyDataSetChanged();
    }

    /** Teste les critères courants sur un Pokémon. */
    private boolean matchCurrentCriteria(Pokemon p) {
        // Type (clé canonique)
        boolean okType = "all".equals(curType) || pokemonHasTypeKey(p, curType);

        // Génération via ID
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

    private boolean pokemonHasTypeKey(Pokemon p, String key) {
        if (p == null || key == null || "all".equals(key)) return true;

        // 1) si tu as mis le nouveau Pokemon.java
        try {
            Set<String> keys = p.getCanonicalTypeKeys();
            if (keys != null && keys.contains(key)) return true;
        } catch (Throwable ignore) { /* fallback si ancien POJO */ }

        // 2) fallback : parser l'ancien champ string p.getType()
        String raw = p.getType();
        if (raw == null) return false;
        String[] tokens = normalize(raw).split("[^a-z]+");
        for (String t : tokens) {
            if (mapToKey(t).equals(key)) return true;
        }
        return false;
    }

    private String toTypeKey(Object selected) {
        String n = normalize(selected == null ? "" : selected.toString());
        switch (n) {
            case "tous": return "all";
            case "feu": case "fire": return "fire";
            case "eau": case "water": return "water";
            case "plante": case "grass": return "grass";
            case "electrik": case "electric": return "electric";
            case "roche": case "rock": return "rock";
            case "spectre": case "ghost": return "ghost";
            default: return "all";
        }
    }

    private String mapToKey(String n) {
        switch (n) {
            case "feu": case "fire": return "fire";
            case "eau": case "water": return "water";
            case "plante": case "grass": return "grass";
            case "electrik": case "electric": return "electric";
            case "roche": case "rock": return "rock";
            case "spectre": case "ghost": return "ghost";
            case "normal": return "normal";
            case "glace": case "ice": return "ice";
            case "combat": case "fighting": return "fighting";
            case "poison": return "poison";
            case "sol": case "ground": return "ground";
            case "vol": case "flying": return "flying";
            case "psy": case "psychic": return "psychic";
            case "insecte": case "bug": return "bug";
            case "dragon": return "dragon";
            case "tenebres": case "ténèbres": case "dark": return "dark";
            case "acier": case "steel": return "steel";
            case "fee": case "fée": case "fairy": return "fairy";
            default: return "";
        }
    }

    private int generationIndexFromSelection(Object sel) {
        String s = normalize(sel == null ? "" : sel.toString()); // ex: "gen i", "gen 1", "génération 3", "kanto"
        if (s.contains("toute")) return 0; // "toutes", "toute"

        // a) chiffre arabe (Gen 1, Génération 2, etc.)
        Matcher m = Pattern.compile("(\\d+)").matcher(s);
        if (m.find()) {
            try {
                int n = Integer.parseInt(m.group(1));
                if (n >= 1 && n <= 9) return n;
            } catch (NumberFormatException ignore) {}
        }

        // b) chiffres romains (i..ix) – on cherche le plus long d'abord
        if (s.contains("IX"))   return 9;
        if (s.contains("VIII")) return 8;
        if (s.contains("VII"))  return 7;
        if (s.contains("VI"))   return 6;
        if (s.contains("V"))    return 5;
        if (s.contains("IV"))   return 4;
        if (s.contains("III"))  return 3;
        if (s.contains("II"))   return 2;
        if (s.contains("I"))   return 1;   // espace-i pour éviter de matcher "viii" etc.

        // c) noms de régions
        if (s.contains("kanto")) return 1;
        if (s.contains("johto")) return 2;
        if (s.contains("hoenn")) return 3;
        if (s.contains("sinnoh")) return 4;
        if (s.contains("unys") || s.contains("unova")) return 5;
        if (s.contains("kalos")) return 6;
        if (s.contains("alola")) return 7;
        if (s.contains("galar")) return 8;
        if (s.contains("paldea")) return 9;

        // défaut
        return 0;
    }


    private int generationIndexForId(int id) {
        if (id <= 151) return 1;
        if (id <= 251) return 2;
        if (id <= 386) return 3;
        if (id <= 493) return 4;
        if (id <= 649) return 5;
        if (id <= 721) return 6;
        if (id <= 809) return 7;
        if (id <= 905) return 8;
        return 9;
    }

    // -------- Helpers généraux --------

    private String textOf(EditText e) {
        return e == null ? null : e.getText().toString();
    }

    private double parseOrDefault(String s, double def) {
        try {
            if (s == null) return def;
            s = s.trim().replace(',', '.');
            return s.isEmpty() ? def : Double.parseDouble(s);
        } catch (Exception e) {
            return def;
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
        String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return noAccent.trim().toLowerCase(Locale.ROOT);
    }
}
