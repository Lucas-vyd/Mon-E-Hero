package com.example.mone_hero_contact;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id; // Identifiant unique pour l'utilisateur, généralement l'ID Firestore
    private String email;
    private String username; // ou tout autre champ que vous pourriez avoir pour un utilisateur
    private List<String> contacts = new ArrayList<>();

    // Constructeur vide requis pour Firestore
    public User() {}

    public User(String id, String email, String username) {
        this.id = id;
        this.email = email;
        this.username = username;
    }

    // Getters et setters pour chaque champ
    @Exclude // Exclure cet attribut lors de la conversion de l'objet en un document Firestore
    public String getId() {
        return id;
    }

    @Exclude // Exclure cet attribut lors de la conversion de l'objet en un document Firestore
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("email") // Ceci est facultatif, car le nom de la méthode correspond au nom du champ
    public String getEmail() {
        return email;
    }

    @PropertyName("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @PropertyName("username")
    public String getUsername() {
        return username;
    }

    @PropertyName("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @PropertyName("contacts")
    public List<String> getContacts() {
        return contacts;
    }

    @PropertyName("contacts")
    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
