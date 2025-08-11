package Classes;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Pokemon implements Parcelable {

    private int id;

    @SerializedName("name")
    private String nomEn;

    @SerializedName("height")
    private double tailleEnMetre;

    @SerializedName("weight")
    private double poidsEnKilo;

    // Chaîne libre utilisée dans ta DB (on la laisse)
    private String type;

    // Sprites existants (on ne touche pas)
    private Sprites sprites;

    // ✅ AJOUT : structure PokeAPI pour les types réels (ex: [{"slot":1,"type":{"name":"fire"}}...])
    @SerializedName("types")
    private java.util.List<PokemonTypeSlot> typesSlots;

    // ---- Constructeurs existants ----
    // Utilisé par ta DB/favoris
    public Pokemon(int id, String nom, String imageUrl, double poids, double taille, String type) {
        this.id = id;
        this.nomEn = nom;
        this.sprites = new Sprites(imageUrl);
        this.poidsEnKilo = poids * 10;      // tu stockes visiblement en hg
        this.tailleEnMetre = taille * 10;   // et en dm : on garde pareil
        this.type = type;
    }

    // ---- Getters existants ----
    public int getId() { return id; }

    public String getNomFr() { return nomEn; }

    public double getTailleEnMetre() { return tailleEnMetre / 10.0; }

    public double getPoidsEnKilo() { return poidsEnKilo / 10.0; }

    public String getType() { return type; }

    public Sprites getSprites() { return sprites; }

    public String getImageUrl() {
        return (sprites != null) ? sprites.getFrontDefault() : null;
    }

    // =========================
    // ✅ AJOUTS POUR LE FILTRAGE
    // =========================

    /** Retourne les types "canoniques" (en anglais) du Pokémon (ex: ["fire","flying"]). */
    public Set<String> getCanonicalTypeKeys() {
        Set<String> out = new HashSet<>();

        // 1) Si ta propriété "type" (DB) est renseignée (ex: "Feu / Vol")
        if (type != null) {
            String[] tokens = normalize(type).split("[^a-z]+");
            for (String t : tokens) {
                String key = mapToKey(t);
                if (!key.isEmpty()) out.add(key);
            }
        }

        // 2) Types officiels renvoyés par PokeAPI (plus fiable)
        if (typesSlots != null) {
            for (PokemonTypeSlot slot : typesSlots) {
                if (slot != null && slot.type != null && slot.type.name != null) {
                    String key = mapToKey(normalize(slot.type.name));
                    if (!key.isEmpty()) out.add(key);
                }
            }
        }
        return out;
    }

    // Normalisation : minuscules + sans accents
    private static String normalize(String s) {
        if (s == null) return "";
        String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+","");
        return noAccent.trim().toLowerCase(Locale.ROOT);
    }

    // FR/EN -> clé canonique EN
    private static String mapToKey(String n) {
        switch (n) {
            case "normal": return "normal";
            case "feu": case "fire": return "fire";
            case "eau": case "water": return "water";
            case "plante": case "grass": return "grass";
            case "electrik": case "électrik": case "electric": return "electric";
            case "glace": case "ice": return "ice";
            case "combat": case "fighting": return "fighting";
            case "poison": return "poison";
            case "sol": case "ground": return "ground";
            case "vol": case "flying": return "flying";
            case "psy": case "psychic": return "psychic";
            case "insecte": case "bug": return "bug";
            case "roche": case "rock": return "rock";
            case "spectre": case "ghost": return "ghost";
            case "dragon": return "dragon";
            case "tenebres": case "ténèbres": case "dark": return "dark";
            case "acier": case "steel": return "steel";
            case "fee": case "fée": case "fairy": return "fairy";
        }
        return "";
    }

    // =========================
    // Parcelable (avec les AJOUTS)
    // =========================
    protected Pokemon(Parcel in) {
        id = in.readInt();
        nomEn = in.readString();
        tailleEnMetre = in.readDouble();
        poidsEnKilo = in.readDouble();
        type = in.readString();
        sprites = in.readParcelable(Sprites.class.getClassLoader());
        // ✅ lire la liste des types
        typesSlots = in.createTypedArrayList(PokemonTypeSlot.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nomEn);
        dest.writeDouble(tailleEnMetre);
        dest.writeDouble(poidsEnKilo);
        dest.writeString(type);
        dest.writeParcelable(sprites, flags);
        // ✅ écrire la liste des types
        dest.writeTypedList(typesSlots);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Pokemon> CREATOR = new Creator<Pokemon>() {
        @Override public Pokemon createFromParcel(Parcel in) { return new Pokemon(in); }
        @Override public Pokemon[] newArray(int size) { return new Pokemon[size]; }
    };

    // =========================
    // ✅ Classes internes pour PokeAPI
    // =========================
    public static class PokemonTypeSlot implements Parcelable {
        @SerializedName("slot") public int slot;
        @SerializedName("type") public TypeName type;

        public PokemonTypeSlot() {}

        protected PokemonTypeSlot(Parcel in) {
            slot = in.readInt();
            type = in.readParcelable(TypeName.class.getClassLoader());
        }

        public static final Creator<PokemonTypeSlot> CREATOR = new Creator<PokemonTypeSlot>() {
            @Override public PokemonTypeSlot createFromParcel(Parcel in) { return new PokemonTypeSlot(in); }
            @Override public PokemonTypeSlot[] newArray(int size) { return new PokemonTypeSlot[size]; }
        };

        @Override public int describeContents() { return 0; }
        @Override public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(slot);
            dest.writeParcelable(type, flags);
        }
    }

    public static class TypeName implements Parcelable {
        @SerializedName("name") public String name;

        public TypeName() {}

        protected TypeName(Parcel in) { name = in.readString(); }

        public static final Creator<TypeName> CREATOR = new Creator<TypeName>() {
            @Override public TypeName createFromParcel(Parcel in) { return new TypeName(in); }
            @Override public TypeName[] newArray(int size) { return new TypeName[size]; }
        };

        @Override public int describeContents() { return 0; }
        @Override public void writeToParcel(Parcel dest, int flags) { dest.writeString(name); }
    }
}
