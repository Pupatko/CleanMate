package com.cleanmate.presentation.apartment;

public class ApartmentTask {

    public enum Type {
        BASIC("Základný"),
        PHOTO("Foto požadované"),
        CHECKLIST("Kontrolný zoznam");

        public final String label;
        Type(String label) { this.label = label; }

        @Override public String toString() { return label; }
    }

    private String name;
    private String description;
    private Type type;

    public ApartmentTask(String name, String description, Type type) {
        this.name = name;
        this.description = description == null ? "" : description;
        this.type = type == null ? Type.BASIC : type;
    }

    public ApartmentTask(String name) {
        this(name, "", Type.BASIC);
    }

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public Type   getType()        { return type; }

    public void setName(String v)        { name = v; }
    public void setDescription(String v) { description = v == null ? "" : v; }
    public void setType(Type v)          { type = v == null ? Type.BASIC : v; }

    @Override
    public String toString() {
        return "[" + type.label + "] " + name;
    }
}
