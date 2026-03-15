package com.fablix.util;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UpdateSecurePassword {

    /*
     * Run this only once on a plaintext-password customers table.
     * Re-running will hash already-hashed values and break login.
     */
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection connection = DriverManager.getConnection(DbConfig.URL, DbConfig.USER, DbConfig.PASSWORD)) {
            try (PreparedStatement alterStmt =
                         connection.prepareStatement("ALTER TABLE customers MODIFY COLUMN password VARCHAR(128)")) {
                alterStmt.executeUpdate();
            }

            PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
            List<String[]> updates = new ArrayList<>();

            try (PreparedStatement queryStmt = connection.prepareStatement("SELECT id, password FROM customers");
                 ResultSet rs = queryStmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String plainPassword = rs.getString("password");
                    String encryptedPassword = passwordEncryptor.encryptPassword(plainPassword);
                    updates.add(new String[]{id, encryptedPassword});
                }
            }

            try (PreparedStatement updateStmt =
                         connection.prepareStatement("UPDATE customers SET password = ? WHERE id = ?")) {
                for (String[] row : updates) {
                    updateStmt.setString(1, row[1]);
                    updateStmt.setString(2, row[0]);
                    updateStmt.addBatch();
                }
                updateStmt.executeBatch();
            }
        }
    }
}
