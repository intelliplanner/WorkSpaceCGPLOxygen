package com.ipssi.rfid.beans;

import java.util.ArrayList;

public class User {
    private int id;
    private String username;
    private String name;
    private boolean isSupperUser;
    private boolean isActive;
    private ArrayList<Integer> privList;

    public User() {
    }

    public User(int id, String username, String name, boolean isSupperUser, boolean isActive) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.isSupperUser = isSupperUser;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSupperUser() {
        return isSupperUser;
    }

    public void setSupperUser(boolean supperUser) {
        isSupperUser = supperUser;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public ArrayList<Integer> getPrivList() {
        return privList;
    }

    public void setPrivList(ArrayList<Integer> privList) {
        this.privList = privList;
    }
}
