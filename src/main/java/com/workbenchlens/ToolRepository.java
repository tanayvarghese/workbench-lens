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

    public List<Tool> loadToolsWithRetry(Console console)
            throws SQLException {
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
                SELECT t.name,
                       t.category,
                       t.purpose,
                       t.safety_notes,
                       t.verification_status,
                       COALESCE(
                           g.catalogue_group,
                           CASE
                               WHEN UPPER(t.category) IN ('HAND TOOL', 'POWER TOOL')
                                   THEN 'Handheld tools'
                               WHEN UPPER(t.category) = 'MEASURING TOOL'
                                   THEN 'Measuring / miscellaneous / dangerous'
                               ELSE 'Unassigned'
                           END
                       ) AS catalogue_group,
                       k.food_contact_safe,
                       k.cleaning_notes,
                       k.kitchen_use,
                       gd.plant_or_task,
                       gd.maintenance_notes,
                       gd.garden_use
                FROM tools t
                LEFT JOIN (
                    SELECT tool_id, 'Electrical tools' AS catalogue_group
                    FROM electrical_tools
                    UNION ALL
                    SELECT tool_id, 'Handheld tools'
                    FROM handheld_tools
                    UNION ALL
                    SELECT tool_id, 'Waterworks tools'
                    FROM waterworks_tools
                    UNION ALL
                    SELECT tool_id, 'Measuring / miscellaneous / dangerous'
                    FROM measuring_misc_dangerous
                    UNION ALL
                    SELECT tool_id, 'Kitchen tools'
                    FROM kitchen_tools
                    UNION ALL
                    SELECT tool_id, 'Garden tools'
                    FROM garden_tools
                ) g ON g.tool_id = t.tool_id
                LEFT JOIN kitchen_tools k ON k.tool_id = t.tool_id
                LEFT JOIN garden_tools gd ON gd.tool_id = t.tool_id
                ORDER BY t.tool_id
                """;

        try (Connection connection =
                     DriverManager.getConnection(URL, USER, password);
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                String name = results.getString("name");
                String category = results.getString("category");
                String purpose = results.getString("purpose");
                String safetyNotes = results.getString("safety_notes");
                String status = results.getString("verification_status");
                String catalogueGroup =
                        results.getString("catalogue_group");

                if ("Kitchen tool".equalsIgnoreCase(category)) {
                    purpose = appendDetail(
                            purpose, "Kitchen use",
                            results.getString("kitchen_use"));
                    safetyNotes = appendDetail(
                            safetyNotes, "Food contact safe",
                            results.getString("food_contact_safe"));
                    safetyNotes = appendDetail(
                            safetyNotes, "Cleaning notes",
                            results.getString("cleaning_notes"));

                    loadedTools.add(new KitchenTool(
                            name, category, purpose, safetyNotes,
                            status, catalogueGroup));

                } else if ("Garden tool".equalsIgnoreCase(category)) {
                    purpose = appendDetail(
                            purpose, "Garden use",
                            results.getString("garden_use"));
                    purpose = appendDetail(
                            purpose, "Plant or task",
                            results.getString("plant_or_task"));
                    safetyNotes = appendDetail(
                            safetyNotes, "Maintenance notes",
                            results.getString("maintenance_notes"));

                    loadedTools.add(new GardenTool(
                            name, category, purpose, safetyNotes,
                            status, catalogueGroup));

                } else if ("Hand tool".equalsIgnoreCase(category)) {
                    loadedTools.add(new HandTool(
                            name, category, purpose, safetyNotes,
                            status, catalogueGroup));

                } else if ("Power tool".equalsIgnoreCase(category)) {
                    loadedTools.add(new PowerTool(
                            name, category, purpose, safetyNotes,
                            status, catalogueGroup));

                } else if ("Measuring tool".equalsIgnoreCase(category)) {
                    loadedTools.add(new MeasuringTool(
                            name, category, purpose, safetyNotes,
                            status, catalogueGroup));

                } else {
                    throw new SQLException(
                            "Unknown tool category: " + category);
                }
            }
        }

        return loadedTools;
    }

    private static String appendDetail(
            String original, String label, String detail) {
        if (detail == null || detail.isBlank()) {
            return original;
        }

        String prefix =
                original == null || original.isBlank()
                        ? ""
                        : original + "\n";

        return prefix + label + ": " + detail;
    }

    private boolean isInvalidPassword(SQLException e) {
        return e.getErrorCode() == 1017
                || (e.getMessage() != null
                && e.getMessage().contains("ORA-01017"));
    }
}