package com.nexushire.model;

/** Base class for every kind of user. Subclasses decide their own role and dashboard (polymorphism). */
public abstract class User {
    private int id;
    private String name;
    private String email;
    private String passwordHash;

    protected User(int id, String name, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public abstract Role getRole();
    public abstract String getDashboardTitle();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    @Override
    public String toString() {
        return getRole() + " #" + id + " " + name + " <" + email + ">";
    }
}
