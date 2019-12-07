package com.example.scotia_app.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Driver users are drivers for the supplier who deliver orders.
 */
public class Driver extends User implements Parcelable {

    /**
     * Create a new Driver with the data in the given parcel.
     *
     * @param in The data of this Driver.
     */
    private Driver(Parcel in) {
        super(in);
    }

    /**
     * Create a new Driver with the data in the given JSONObject.
     *
     * @param userData The data of this Driver.
     */
    public Driver(JSONObject userData) {
        super(userData, Persona.driver);
    }

    public Driver(String name, String id) { super(name, id, Persona.driver); }

    /**
     * Passes the attributes of this Driver into a parcel
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
    public static final Creator<Driver> CREATOR = new Creator<Driver>() {
        @Override
        public Driver createFromParcel(Parcel in) {
            return new Driver(in);
        }

        @Override
        public Driver[] newArray(int size) {
            return new Driver[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
