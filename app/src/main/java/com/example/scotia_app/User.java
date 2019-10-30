package com.example.scotia_app;

class User {

    private String name;
    private Persona personaType;
    private String id;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Persona getPersonaType() {
        return this.personaType;
    }

    public void setPersonaType(Persona personaType) {
        this.personaType = personaType;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    User(String name, Persona personaType, String id) {
        this.name = name;
        this.personaType = personaType;
        this.id = id;
    }

}

enum Persona
{
    Driver, Customer, Supplier;
}