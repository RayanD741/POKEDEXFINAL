package Classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBUtil extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pokedex.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_FAVORIS = "favoris";

    private static final String COL_ID = "id";
    private static final String COL_NOM = "nom";
    private static final String COL_IMAGE_URL = "imageUrl";
    private static final String COL_POIDS = "poids";
    private static final String COL_TAILLE = "taille";
    private static final String COL_TYPE = "type";

    public DBUtil(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_FAVORIS + " (" +
                COL_ID + " INTEGER PRIMARY KEY, " +
                COL_NOM + " TEXT, " +
                COL_IMAGE_URL + " TEXT, " +
                COL_POIDS + " REAL, " +
                COL_TAILLE + " REAL, " +
                COL_TYPE + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORIS);
        onCreate(db);
    }

    // Ajouter un favori
    public void ajouterFavori(Pokemon p) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_ID, p.getId());
        values.put(COL_NOM, p.getNomFr());
        values.put(COL_IMAGE_URL, p.getImageUrl());

        values.put(COL_POIDS, p.getPoidsEnKilo());
        values.put(COL_TAILLE, p.getTailleEnMetre());
        values.put(COL_TYPE, p.getType());


        db.insert(TABLE_FAVORIS, null, values);
        db.close();
    }

    // Supprimer un favori
    public void supprimerFavori(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORIS, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Vérifier si un favori est déjà enregistré
    public boolean estFavori(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORIS,
                new String[]{COL_ID},
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);
        boolean existe = cursor.moveToFirst();
        cursor.close();
        db.close();
        return existe;
    }

    // Récupérer tous les favoris
    public List<Pokemon> getAllFavoris() {
        List<Pokemon> favorisList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAVORIS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String nom = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOM));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_URL));
                double poids = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_POIDS));
                double taille = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TAILLE));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE));

                Pokemon pokemon = new Pokemon(id, nom, imageUrl, poids, taille, type);
                favorisList.add(pokemon);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return favorisList;
    }
}
