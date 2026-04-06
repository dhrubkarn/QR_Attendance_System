package com.attendance.model;

public abstract class User {
    protected int id;
    protected String name;
    protected String email;
    protected String passwordHash;
    
    public User(int id, String name, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }
    
    public abstract String getRole();
    
    public boolean authenticate(String inputPassword) {
        // Simple hash check (use BCrypt in production)
        return this.passwordHash != null && this.passwordHash.equals(inputPassword);
    }
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}