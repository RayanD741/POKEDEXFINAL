package fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokedex_final.DetailPokemon;
import com.example.pokedex_final.R;

import java.util.ArrayList;
import java.util.List;

import Classes.DBUtil;
import Classes.Pokemon;
import Classes.PokemonAdapter;

public class FavorisFragment extends Fragment {

    private RecyclerView recyclerView;
    private PokemonAdapter adapter;
    private List<Pokemon> listeFavoris;
    private DBUtil dbUtil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favoris, container, false);

        recyclerView = view.findViewById(R.id.recyclerFavoris);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        dbUtil = new DBUtil(getContext());
        listeFavoris = new ArrayList<>(dbUtil.getAllFavoris());

        adapter = new PokemonAdapter(getContext(), listeFavoris, pokemon -> {
            // Ouvre l'écran de détails avec l'option de retirer le favori là-bas
            Intent intent = new Intent(getContext(), DetailPokemon.class);
            intent.putExtra(PokemonAdapter.EXTRA_POKEMON_ID, pokemon);
            startActivity(intent);
        });


        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dbUtil != null) {
            listeFavoris.clear();
            listeFavoris.addAll(dbUtil.getAllFavoris());
            adapter.notifyDataSetChanged();
        }
    }
}
