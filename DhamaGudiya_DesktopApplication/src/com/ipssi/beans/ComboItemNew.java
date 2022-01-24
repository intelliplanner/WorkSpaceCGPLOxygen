/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.beans;

/**
 *
 * @author IPSSI
 */
public class ComboItemNew {

    private int id;
    private int minesId;
    private String name;
    private String prefix;

    public ComboItemNew(int id,String name, int minesId,  String prefix) {
        this.id = id;
        this.minesId = minesId;
        this.name = name;
        this.prefix = prefix;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMinesId() {
        return minesId;
    }

    public void setMinesId(int minesId) {
        this.minesId = minesId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
}
