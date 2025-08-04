package Classes;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Pokemon implements Serializable {

    private int id;

    @SerializedName("name")
    private String nom;

    private double height;  // en décimètres
    private double weight;  // en hectogrammes
    private String imageUrl;

    private Sprites sprites;

    private String premierType;
    private String secondType;

    public Pokemon(int id, String nom, double poids, double taille, String premierType, String secondType, Sprites sprites) {
        this.id = id;
        this.nom = nom;
        this.weight = poids;
        this.height = taille;
        this.premierType = premierType;
        this.secondType = secondType;
        this.sprites = sprites;
    }

    public Pokemon(int id, String nom, String imageUrl, double poids, double taille, String type) {
        this.id = id;
        this.nom = nom;
        this.imageUrl = imageUrl;
        this.weight = poids * 10;  // conversion inverse
        this.height = taille * 10; // conversion inverse
        this.premierType = type;
        this.sprites = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomFr() {
        return nom;
    }

    public void setNomFr(String nom) {
        this.nom = nom;
    }

    public double getTailleEnMetre() {
        return height / 10.0;
    }
    public String getImageUrl() {
        return imageUrl;
    }


    public double getPoidsEnKilo() {
        return weight / 10.0;
    }

    public double getRawTaille() {
        return height;
    }

    public double getRawPoids() {
        return weight;
    }

    public void setRawTaille(double height) {
        this.height = height;
    }

    public void setRawPoids(double weight) {
        this.weight = weight;
    }

    public String getPremierType() {
        return premierType;
    }

    public void setPremierType(String premierType) {
        this.premierType = premierType;
    }

    public String getSecondType() {
        return secondType;
    }

    public void setSecondType(String secondType) {
        this.secondType = secondType;
    }

    public Sprites getSprites() {
        return sprites;
    }

    public void setSprites(Sprites sprites) {
        this.sprites = sprites;
    }
}
