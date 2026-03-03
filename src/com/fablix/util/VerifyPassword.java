package com.fablix.util;

import org.jasypt.util.password.StrongPasswordEncryptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VerifyPassword {

    public static void main(String[] args) throws Exception {
        System.out.println(verifyCredentials("a@email.com", "a2"));
        System.out.println(verifyCredentials("a@email.com", "a3"));
    }

    private static boolean verifyCredentials(String email, String password) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(DbConfig.URL, DbConfig.USER, DbConfig.PASSWORD);
             PreparedStatement statement = connection.prepareStatement("SELECT password FROM customers WHERE email = ?")) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String encryptedPassword = rs.getString("password");
                return new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
            }
        }
    }
}
