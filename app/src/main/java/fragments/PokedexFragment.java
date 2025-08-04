package fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private RecyclerView recyclerView;
    private PokemonAdapter adapter;
    private List<Pokemon> listePokemon = new ArrayList<>();
    private List<Pokemon> listeFiltre = new ArrayList<>();

    private final int NOMBRE_TOTAL = 1025;

    private Spinner spinnerType, spinnerGeneration;
    private EditText poidsMin, poidsMax, tailleMin, tailleMax;
    private Button boutonFiltrer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pokedex, container, false);

        recyclerView = view.findViewById(R.id.recyclerPokedex);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new PokemonAdapter(getContext(), listeFiltre);
        recyclerView.setAdapter(adapter);

        spinnerType = view.findViewById(R.id.spinnerType);
        spinnerGeneration = view.findViewById(R.id.spinnerGeneration);
        poidsMin = view.findViewById(R.id.editPoidsMin);
        poidsMax = view.findViewById(R.id.editPoidsMax);
        tailleMin = view.findViewById(R.id.editTailleMin);
        tailleMax = view.findViewById(R.id.editTailleMax);
        boutonFiltrer = view.findViewById(R.id.btnFiltrer);

        boutonFiltrer.setOnClickListener(v -> appliquerFiltre());

        chargerPokemonDepuisAPI();

        return view;
    }

    private void chargerPokemonDepuisAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PokeApi api = retrofit.create(PokeApi.class);

        for (int i = 1; i <= NOMBRE_TOTAL; i++) {
            int finalI = i;
            Call<Pokemon> pokeCall = api.getPokemon(i);
            pokeCall.enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Pokemon p = response.body();
                        listePokemon.add(p);
                        listeFiltre.add(p);
                        adapter.notifyItemInserted(listeFiltre.size() - 1);
                    }
                }

                @Override
                public void onFailure(Call<Pokemon> call, Throwable t) {
                    Toast.makeText(getContext(), "Erreur API", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void appliquerFiltre() {
        String typeSelectionne = spinnerType.getSelectedItem().toString().toLowerCase(Locale.ROOT);
        String generationSelectionnee = spinnerGeneration.getSelectedItem().toString();

        double pMin = poidsMin.getText().toString().isEmpty() ? 0 : Double.parseDouble(poidsMin.getText().toString());
        double pMax = poidsMax.getText().toString().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(poidsMax.getText().toString());
        double tMin = tailleMin.getText().toString().isEmpty() ? 0 : Double.parseDouble(tailleMin.getText().toString());
        double tMax = tailleMax.getText().toString().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(tailleMax.getText().toString());

        listeFiltre.clear();

        for (Pokemon p : listePokemon) {
            boolean matchType = typeSelectionne.equals("tous") || p.getPremierType().toLowerCase().equals(typeSelectionne);
            boolean matchPoids = p.getPoidsEnKilo() >= pMin && p.getPoidsEnKilo() <= pMax;
            boolean matchTaille = p.getTailleEnMetre() >= tMin && p.getTailleEnMetre() <= tMax;

            if (matchType && matchPoids && matchTaille) {
                listeFiltre.add(p);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
