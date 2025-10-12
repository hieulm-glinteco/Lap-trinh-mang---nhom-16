/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.model;

import java.sql.Date;

/**
 *
 * @author admin
 */

public class User {
    private int id;
    private String name;
    private String username;
    private String password;
    private String email, phone;
    private int totalRankScore;
    private Date dob;
    
    public User(){
        
    }
    public User(int id, String name, String username, String password, String email, String phone, int totalRankScore, Date dob) {
        this.id = id; 
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.totalRankScore = totalRankScore;
        this.dob = dob;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getTotalRankScore() {
        return totalRankScore;
    }

    public void setTotalRankScore(int totalRankScore) {
        this.totalRankScore = totalRankScore;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    
    
    
}
