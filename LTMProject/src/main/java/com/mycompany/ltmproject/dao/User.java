/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.dao;

/**
 *
 * @author admin
 */

public class User {
    private int id;
    private String name;
    private String username;
    private String password;

    public User(int id, String name, String username, String password) {
        this.id = id; this.name = name;
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', username='%s', password='%s'}", id, name, username, password);
    }
    
    
}
