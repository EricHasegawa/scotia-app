package com.example.scotia_app.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Customer users are small business who will place and pay for orders.
 */
public class Customer extends User implements Parcelable {

    private String address;

    /**
     * Create a new Customer with the data in the given parcel.
     *
     * @param in The data of this Customer.
     */
    private Customer(Parcel in) {
        super(in);
        this.address = in.readString();
    }

    /**
     * Create a new Customer with the data in the given JSONObject.
     *
     * @param userData The data of this Customer.
     */
    public Customer(JSONObject userData) {
        super(userData, Persona.customer);
        try {
            this.address = userData.getString("address");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Customer(String name, String id) {
        super(name, id, Persona.customer);
    }

    /**
     * Passes the attributes of this Customer into a parcel
     *
     * @param dest The parcel to pass the attributes into
     * @param flags Optional details about how the attributes should be passed
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.address);
    }

    /**
     * IDE-generated Parcelable methods
     */
    public static final Creator<Customer> CREATOR = new Creator<Customer>() {
        @Override
        public Customer createFromParcel(Parcel in) {
            return new Customer(in);
        }

        @Override
        public Customer[] newArray(int size) {
            return new Customer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getAddress() {
        return address;
    }
}
