
package com.mycompany.ltmproject.dao;

import com.mycompany.ltmproject.model.GameSession;
import com.mycompany.ltmproject.model.User;
import com.mycompany.ltmproject.util.DB;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GameSessionDAO {
    
    public static List<GameSession> getGameSessionById(int playerId) {
        List<GameSession> list = new ArrayList<>();

        String sql = """
            SELECT gs.*, 
                   p1.username AS player1Name,
                   p2.username AS player2Name
            FROM GameSession gs
            JOIN Player p1 ON gs.Playerid1 = p1.id
            JOIN Player p2 ON gs.Playerid2 = p2.id
            WHERE gs.Playerid1 = ? OR gs.Playerid2 = ?
            ORDER BY gs.start_time DESC
        """;

        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, playerId);
            ps.setInt(2, playerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GameSession gs = new GameSession();
                    gs.setId(rs.getInt("id"));
                    gs.setPlayerid1(rs.getInt("Playerid1"));
                    gs.setPlayerid2(rs.getInt("Playerid2"));
                    gs.setStart(rs.getTimestamp("start_time"));
                    gs.setEnd(rs.getTimestamp("end_time"));
                    gs.setPlayerscore1(rs.getInt("playerscore1"));
                    gs.setPlayerscore2(rs.getInt("playerscore2"));
                    gs.setWinner(rs.getInt("winner")); // vì winner là INT

                    list.add(gs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
