package com.workbenchlens;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ToolRepository {
    private static final String URL =
            "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
    private static final String USER = "WORKBENCH";

    public List<Tool> loadTools(String password) throws SQLException {
        List<Tool> loadedTools = new ArrayList<>();

        String sql = """
                SELECT name, category, purpose, safety_notes
                FROM tools
                ORDER BY tool_id
                """;

        try (Connection connection =
                     DriverManager.getConnection(URL, USER, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                String name = results.getString("name");
                String category = results.getString("category");
                String purpose = results.getString("purpose");
                String safetyNotes = results.getString("safety_notes");

                if ("Hand tool".equalsIgnoreCase(category)) {
                    loadedTools.add(
                            new HandTool(name, category, purpose, safetyNotes));
                } else if ("Power tool".equalsIgnoreCase(category)) {
                    loadedTools.add(
                            new PowerTool(name, category, purpose, safetyNotes));
                } else if ("Measuring tool".equalsIgnoreCase(category)) {
                    loadedTools.add(
                            new MeasuringTool(name, category, purpose, safetyNotes));
                } else {
                    throw new SQLException("Unknown tool category: " + category);
                }
            }
        }

        return loadedTools;
    }
}