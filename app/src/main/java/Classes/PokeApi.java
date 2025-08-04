package Classes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PokeApi {

    // ✅ Utiliser @Query pour les paramètres d’URL
    @GET("pokemon")
    Call<PokemonSpecies> getAllPokemon(
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("pokemon/{id}")
    Call<Pokemon> getPokemon(@Path("id") int id);
}
