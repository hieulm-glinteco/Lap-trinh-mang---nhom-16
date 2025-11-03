package com.mycompany.ltmproject.model;

public class GameRound {
    private int id;
    private int gameSessionId;
    private int imageId;
    private int roundNumber;
    private Integer winner; // có thể null nếu chưa có người thắng

    public GameRound() {
    }

    public GameRound(int id, int gameSessionId, int imageId, int roundNumber, Integer winner) {
        this.id = id;
        this.gameSessionId = gameSessionId;
        this.imageId = imageId;
        this.roundNumber = roundNumber;
        this.winner = winner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(int gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Integer getWinner() {
        return winner;
    }

    public void setWinner(Integer winner) {
        this.winner = winner;
    }

    @Override
    public String toString() {
        return "GameRound{" +
                "id=" + id +
                ", gameSessionId=" + gameSessionId +
                ", imageId=" + imageId +
                ", roundNumber=" + roundNumber +
                ", winner=" + winner +
                '}';
    }
}
