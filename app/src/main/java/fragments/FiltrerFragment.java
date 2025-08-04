package fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.pokedex_final.R;

public class FiltrerFragment extends Fragment {

    private Spinner spinnerType, spinnerGeneration;
    private EditText poidsMin, poidsMax, tailleMin, tailleMax;
    private Button btnAppliquer;

    private String[] types = {"Tous", "Feu", "Eau", "Plante", "Électrik", "Roche", "Spectre"}; // Ajoute plus si tu veux
    private String[] generations = {"Toutes", "Gen I", "Gen II", "Gen III", "Gen IV", "Gen V", "Gen VI", "Gen VII", "Gen VIII", "Gen IX"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filtrer, container, false);

        spinnerType = view.findViewById(R.id.spinnerType);
        spinnerGeneration = view.findViewById(R.id.spinnerGeneration);
        poidsMin = view.findViewById(R.id.poidsMin);
        poidsMax = view.findViewById(R.id.poidsMax);
        tailleMin = view.findViewById(R.id.tailleMin);
        tailleMax = view.findViewById(R.id.tailleMax);
        btnAppliquer = view.findViewById(R.id.btnAppliquer);

        spinnerType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, types));
        spinnerGeneration.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, generations));

        btnAppliquer.setOnClickListener(v -> {
            // TODO : Appelle une méthode pour filtrer et affiche une nouvelle liste
            Toast.makeText(getContext(), "Filtrage en cours...", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
