package com.mycompany.ltmproject.dao;

import com.mycompany.ltmproject.model.GameRound;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameRoundDAO {

    private Connection conn;

    public GameRoundDAO(Connection conn) {
        this.conn = conn;
    }

    // Thêm round mới
    public boolean insert(GameRound round) throws SQLException {
        String sql = "INSERT INTO gameround (gameSessionId, imageId, roundNumber, winner) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, round.getGameSessionId());
            ps.setInt(2, round.getImageId());
            ps.setInt(3, round.getRoundNumber());
            if (round.getWinner() != null)
                ps.setInt(4, round.getWinner());
            else
                ps.setNull(4, java.sql.Types.INTEGER);

            return ps.executeUpdate() > 0;
        }
    }

    // Lấy danh sách các round của 1 session
    public List<GameRound> getRoundsBySession(int sessionId) throws SQLException {
        List<GameRound> list = new ArrayList<>();
        String sql = "SELECT * FROM gameround WHERE gameSessionId = ? ORDER BY roundNumber ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                GameRound round = new GameRound(
                        rs.getInt("id"),
                        rs.getInt("gameSessionId"),
                        rs.getInt("imageId"),
                        rs.getInt("roundNumber"),
                        (Integer) rs.getObject("winner")
                );
                list.add(round);
            }
        }
        return list;
    }

    // Cập nhật người thắng
    public boolean updateWinner(int roundId, int winnerId) throws SQLException {
        String sql = "UPDATE gameround SET winner = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, winnerId);
            ps.setInt(2, roundId);
            return ps.executeUpdate() > 0;
        }
    }

    // Lấy round hiện tại theo số round
    public GameRound getRoundByNumber(int sessionId, int roundNumber) throws SQLException {
        String sql = "SELECT * FROM gameround WHERE gameSessionId = ? AND roundNumber = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, roundNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new GameRound(
                        rs.getInt("id"),
                        rs.getInt("gameSessionId"),
                        rs.getInt("imageId"),
                        rs.getInt("roundNumber"),
                        (Integer) rs.getObject("winner")
                );
            }
        }
        return null;
    }
}
