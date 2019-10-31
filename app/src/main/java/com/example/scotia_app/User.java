package com.example.scotia_app;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String mName;
    private Persona mPersonaType;
    private String mId;

    User(String name, Persona personaType, String id) {
        this.mName = name;
        this.mPersonaType = personaType;
        this.mId = id;
    }

    private User(Parcel in) {
        mName = in.readString();
        mPersonaType = Persona.valueOf(in.readString());
        mId = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {

            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getPersonaType() {
        return this.mPersonaType.name();
    }

    public void setPersonaType(Persona personaType) {
        this.mPersonaType = personaType;
    }

    public String getId() {
        return this.mId;
    }

    public void setId(String id) {
        this.mId = id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeString(mPersonaType.name());
        parcel.writeString(mId);
    }
}

enum Persona
{
    driver, customer, supplier;
}