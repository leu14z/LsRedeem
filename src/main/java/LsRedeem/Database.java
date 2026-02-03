package me.leuz.lsredeem;

import java.sql.*;

public class Database {
    private Connection connection;
    private final String path;

    public Database(String path) {
        this.path = path;
        setup();
    }

    private void setup() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement s = connection.createStatement();
            // Cria a tabela para salvar Player, Código e IP
            s.execute("CREATE TABLE IF NOT EXISTS redeemed_codes (player TEXT, code TEXT, ip TEXT)");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public boolean hasRedeemed(String player, String code, String ip, boolean checkIP) {
        String query = checkIP ?
                "SELECT * FROM redeemed_codes WHERE code = ? AND (player = ? OR ip = ?)" :
                "SELECT * FROM redeemed_codes WHERE code = ? AND player = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, code);
            ps.setString(2, player);
            if (checkIP) ps.setString(3, ip);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public void addLog(String player, String code, String ip) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO redeemed_codes VALUES (?, ?, ?)")) {
            ps.setString(1, player);
            ps.setString(2, code);
            ps.setString(3, ip);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void resetSpecific(String player, String code) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM redeemed_codes WHERE player = ? AND code = ?")) {
            ps.setString(1, player);
            ps.setString(2, code);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void resetAll(String player) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM redeemed_codes WHERE player = ?")) {
            ps.setString(1, player);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
