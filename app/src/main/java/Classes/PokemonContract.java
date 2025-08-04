package Classes;

import android.provider.BaseColumns;

public final class PokemonContract {

    private PokemonContract() {}

    public static class PokemonEntry implements BaseColumns {
        public static final String TABLE_NAME = "pokemon_favoris";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NOM = "nom";
        public static final String COLUMN_TYPE1 = "type1";
        public static final String COLUMN_TYPE2 = "type2";
        public static final String COLUMN_IMAGE_URL = "image";
    }
}
