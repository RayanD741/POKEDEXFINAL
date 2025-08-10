package Classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Sprites implements Parcelable {

    @SerializedName("front_default") // correspondance avec la clé JSON de l'API
    private String frontDefault;

    public Sprites(String frontDefault) {
        this.frontDefault = frontDefault;
    }

    protected Sprites(Parcel in) {
        frontDefault = in.readString();
    }

    public static final Creator<Sprites> CREATOR = new Creator<Sprites>() {
        @Override
        public Sprites createFromParcel(Parcel in) {
            return new Sprites(in);
        }

        @Override
        public Sprites[] newArray(int size) {
            return new Sprites[size];
        }
    };

    public String getFrontDefault() {
        return frontDefault;
    }

    public void setFrontDefault(String frontDefault) {
        this.frontDefault = frontDefault;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(frontDefault);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
