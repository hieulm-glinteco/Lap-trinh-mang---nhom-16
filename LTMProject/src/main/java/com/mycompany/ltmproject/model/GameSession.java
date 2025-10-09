/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.model;

import java.sql.Date;

/**
 *
 * @author TT
 */
public class GameSession {
    private int id, playerid1, playerid2;
    private Date start, end;
    private int playerscore1, playerscore2, winner;

    public GameSession() {
    }

    public GameSession(int id, int playerid1, int playerid2, Date start, Date end, int playerscore1, int playerscore2, int winner) {
        this.id = id;
        this.playerid1 = playerid1;
        this.playerid2 = playerid2;
        this.start = start;
        this.end = end;
        this.playerscore1 = playerscore1;
        this.playerscore2 = playerscore2;
        this.winner = winner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlayerid1() {
        return playerid1;
    }

    public void setPlayerid1(int playerid1) {
        this.playerid1 = playerid1;
    }

    public int getPlayerid2() {
        return playerid2;
    }

    public void setPlayerid2(int playerid2) {
        this.playerid2 = playerid2;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public int getPlayerscore1() {
        return playerscore1;
    }

    public void setPlayerscore1(int playerscore1) {
        this.playerscore1 = playerscore1;
    }

    public int getPlayerscore2() {
        return playerscore2;
    }

    public void setPlayerscore2(int playerscore2) {
        this.playerscore2 = playerscore2;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }
    
    
}
