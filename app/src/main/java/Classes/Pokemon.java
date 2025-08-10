package Classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Pokemon implements Parcelable {

    private int id;

    @SerializedName("name")
    private String nomEn;

    @SerializedName("height")
    private double tailleEnMetre;

    @SerializedName("weight")
    private double poidsEnKilo;

    private String type; // version simplifiée, pour correspondre à ta DB

    private Sprites sprites;

    // Constructeur utilisé pour créer un Pokemon avec un seul type (pour ta DB)
    public Pokemon(int id, String nom, String imageUrl, double poids, double taille, String type) {
        this.id = id;
        this.nomEn = nom;
        this.sprites = new Sprites(imageUrl);
        this.poidsEnKilo = poids * 10;
        this.tailleEnMetre = taille * 10;
        this.type = type;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getNomFr() {
        return nomEn;
    }

    public double getTailleEnMetre() {
        return tailleEnMetre / 10.0;
    }

    public double getPoidsEnKilo() {
        return poidsEnKilo / 10.0;
    }

    public String getType() {
        return type;
    }

    public Sprites getSprites() {
        return sprites;
    }

    public String getImageUrl() {
        return (sprites != null) ? sprites.getFrontDefault() : null;
    }

    // Parcelable implementation

    protected Pokemon(Parcel in) {
        id = in.readInt();
        nomEn = in.readString();
        tailleEnMetre = in.readDouble();
        poidsEnKilo = in.readDouble();
        type = in.readString();
        sprites = in.readParcelable(Sprites.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nomEn);
        dest.writeDouble(tailleEnMetre);
        dest.writeDouble(poidsEnKilo);
        dest.writeString(type);
        dest.writeParcelable(sprites, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Pokemon> CREATOR = new Creator<Pokemon>() {
        @Override
        public Pokemon createFromParcel(Parcel in) {
            return new Pokemon(in);
        }

        @Override
        public Pokemon[] newArray(int size) {
            return new Pokemon[size];
        }
    };
}
