package com.example.scotia_app.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class Supplier extends User implements Parcelable {

    /**
     * Create a new Supplier with the data in the given parcel.
     *
     * @param in The data of this Supplier.
     */
    private Supplier(Parcel in) {
        super(in);
    }

    /**
     * Create a new Supplier with the data in the given JSONObject.
     *
     * @param userData The data of this Supplier.
     */
    public Supplier(JSONObject userData) {
        super(userData, Persona.supplier);
    }

    public Supplier(String name, String id) { super(name, id, Persona.supplier); }

    /**
     * Passes the attributes of this Supplier into a parcel
     *
     * @param dest The parcel to pass the attributes into
     * @param flags Optional details about how the attributes should be passed
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    /**
     * IDE-generated Parcelable methods
     */
    public static final Creator<Supplier> CREATOR = new Creator<Supplier>() {
        @Override
        public Supplier createFromParcel(Parcel in) {
            return new Supplier(in);
        }

        @Override
        public Supplier[] newArray(int size) {
            return new Supplier[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
