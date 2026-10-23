package com.workbenchlens;

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToolRepository {
    private static final String URL =
            "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
    private static final String USER = "WORKBENCH";
    private static final int MAX_PASSWORD_ATTEMPTS = 3;

    public List<Tool> loadToolsWithRetry(Console console) throws SQLException {
        for (int attempt = 1; attempt <= MAX_PASSWORD_ATTEMPTS; attempt++) {
            char[] password = console.readPassword(
                    "WORKBENCH password (attempt %d of %d): ",
                    attempt, MAX_PASSWORD_ATTEMPTS);

            if (password == null) {
                throw new SQLException("Password entry was cancelled.");
            }

            try {
                return loadTools(new String(password));
            } catch (SQLException e) {
                if (!isInvalidPassword(e)) {
                    throw e;
                }

                if (attempt == MAX_PASSWORD_ATTEMPTS) {
                    throw new SQLException(
                            "Login failed after three attempts. Please run the app again.",
                            e);
                }

                console.printf("Incorrect password. Please try again.%n");
            } finally {
                Arrays.fill(password, '\0');
            }
        }

        throw new SQLException("Could not connect to the Oracle database.");
    }

    public List<Tool> loadTools(String password) throws SQLException {
        List<Tool> loadedTools = new ArrayList<>();

        String sql = """
                SELECT t.name, t.category, t.purpose, t.safety_notes,
                       t.verification_status,
                       COALESCE(g.catalogue_group, 'Unassigned') AS catalogue_group
                FROM tools t
                LEFT JOIN (
                    SELECT tool_id, 'Electrical tools' AS catalogue_group FROM electrical_tools
                    UNION ALL
                    SELECT tool_id, 'Handheld tools' FROM handheld_tools
                    UNION ALL
                    SELECT tool_id, 'Waterworks tools' FROM waterworks_tools
                    UNION ALL
                    SELECT tool_id, 'Measuring / miscellaneous / dangerous' FROM measuring_misc_dangerous
                ) g ON g.tool_id = t.tool_id
                ORDER BY t.tool_id
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
                String status = results.getString("verification_status");
                String catalogueGroup = results.getString("catalogue_group");

                if ("Hand tool".equalsIgnoreCase(category)) {
                    loadedTools.add(new HandTool(
                            name, category, purpose, safetyNotes, status, catalogueGroup));
                } else if ("Power tool".equalsIgnoreCase(category)) {
                    loadedTools.add(new PowerTool(
                            name, category, purpose, safetyNotes, status, catalogueGroup));
                } else if ("Measuring tool".equalsIgnoreCase(category)) {
                    loadedTools.add(new MeasuringTool(
                            name, category, purpose, safetyNotes, status, catalogueGroup));
                } else {
                    throw new SQLException("Unknown tool category: " + category);
                }
            }
        }

        return loadedTools;
    }

    private boolean isInvalidPassword(SQLException e) {
        return e.getErrorCode() == 1017
                || (e.getMessage() != null
                && e.getMessage().contains("ORA-01017"));
    }
}
