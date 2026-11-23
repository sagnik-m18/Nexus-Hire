package com.nexushire.model;

public class Employer extends User {
    public Employer(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash);
    }
    @Override public Role getRole() { return Role.EMPLOYER; }
    @Override public String getDashboardTitle() { return "Employer Dashboard"; }
}
