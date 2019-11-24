package com.example.scotia_app.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An abstract superclass which stores the attributes of the currently logged in user. Implements
 * Parcelable so that it can be passed to different fragments and activities
 */
public abstract class User implements Parcelable {

    private String name;
    private String id;
    private Persona persona;

    /**
     * Create a new User with the data in the given parcel.
     *
     * @param in The data of this User.
     */
    User(Parcel in) {
        this.name = in.readString();
        this.id = in.readString();
        this.persona = Persona.valueOf(in.readString());
    }

    /** Create a new User with the data in the given JSONObject.
     *
     * @param userData The data of this User.
     * @param persona The persona of this User.
     */
    User(JSONObject userData, Persona persona) {
        try {
            this.name = userData.getString("name");
            this.id = userData.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.persona = persona;
    }

    User(String name, String id, Persona personaType) {
        this.name = name;
        this.id = id;
        this.persona = personaType;
    }

    /**
     * Passes the attributes of the user into a parcel
     *
     * @param dest The parcel to pass the attributes into
     * @param flags Optional details about how the attributes should be passed
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(persona.toString());
    }

    /**
     * Returns the URL to access the cloud function which returns this User's invoices
     */
    public String getInvoiceURL(Filter filter) {
        return "https://us-central1-scotiabank-app.cloudfunctions.net/get-invoices-by-user-id?" +
                "id=" + getId() + "&filter=" + filter;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Persona getPersona() {
        return persona;
    }

}