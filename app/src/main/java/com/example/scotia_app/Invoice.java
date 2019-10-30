package com.example.scotia_app;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class Invoice implements Parcelable  {

    // TODO: Eric, here is a helpful video outlining everything: https://www.youtube.com/watch?v=WBbsvqSu0is

    // TODO: Eric, please take a look at this constructor in the User class and do the same.
    private Invoice(Parcel in) {
    }

    /** TODO: Eric, please populate this constructor based on the data retrieved in the JSONObject.
     * TODO: Alternatively, you can parse the JSON object in InvoicesFragment then pass the separate
     * TODO: components into this constructor. I am not sure what is considered best practice. Maybe
     * TODO: ask Paul tomorrow.
     */
    public Invoice (JSONObject invoiceData) {

    }

    public static final Creator<Invoice> CREATOR = new Creator<Invoice>() {
        @Override
        public Invoice createFromParcel(Parcel in) {
            return new Invoice(in);
        }

        @Override
        public Invoice[] newArray(int size) {
            return new Invoice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // TODO: Eric, please write this method. See what I did in User for reference. Best, Salman
    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
