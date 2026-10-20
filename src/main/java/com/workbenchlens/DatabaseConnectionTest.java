package com.workbenchlens;

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        Console console = System.console();

        if (console == null) {
            System.out.println("Run this from the VS Code Terminal so the password stays hidden.");
            return;
        }

        char[] password = console.readPassword("WORKBENCH password: ");
        String url = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";

        try (Connection connection =
                     DriverManager.getConnection(url, "WORKBENCH", new String(password));
             Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(
                     "SELECT tool_id, name, category FROM tools")) {

            System.out.println("Connected to Oracle! Tools:");
            while (results.next()) {
                System.out.printf("%d | %s | %s%n",
                        results.getInt("tool_id"),
                        results.getString("name"),
                        results.getString("category"));
            }
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}