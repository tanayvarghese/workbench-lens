package com.workbenchlens;

import java.io.Console;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

abstract class Tool {
    private final String name;
    private final String category;
    private final String purpose;
    private final String safetyNotes;
    private final String verificationStatus;
    private final String catalogueGroup;

    public Tool(String name, String category, String purpose,
                String safetyNotes, String verificationStatus, String catalogueGroup) {
        this.name = name;
        this.category = category;
        this.purpose = purpose;
        this.safetyNotes = safetyNotes;
        this.verificationStatus = verificationStatus;
        this.catalogueGroup = catalogueGroup;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getSafetyNotes() {
        return safetyNotes;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public String getCatalogueGroup() { return catalogueGroup; }
}

class HandTool extends Tool {
    public HandTool(String name, String category, String purpose,
                    String safetyNotes, String verificationStatus, String catalogueGroup) {
        super(name, category, purpose, safetyNotes, verificationStatus, catalogueGroup);
    }
}

class PowerTool extends Tool {
    public PowerTool(String name, String category, String purpose,
                     String safetyNotes, String verificationStatus, String catalogueGroup) {
        super(name, category, purpose, safetyNotes, verificationStatus, catalogueGroup);
    }
}

class MeasuringTool extends Tool {
    public MeasuringTool(String name, String category, String purpose,
                         String safetyNotes, String verificationStatus, String catalogueGroup) {
        super(name, category, purpose, safetyNotes, verificationStatus, catalogueGroup);
    }
}

class ToolCatalogue {
    private final List<Tool> tools = new ArrayList<>();
    private final Map<String, Tool> toolsByName = new HashMap<>();
    private final Set<String> toolNames = new HashSet<>();
    private int duplicateCount;

    public Tool findByName(String searchText) {
        if (searchText == null) {
            return null;
        }

        String key = searchText.trim().toLowerCase(Locale.ROOT);
        return toolsByName.get(key);
    }

    public void loadTools(List<Tool> databaseTools) {
        tools.clear();
        toolsByName.clear();
        toolNames.clear();
        duplicateCount = 0;

        for (Tool tool : databaseTools) {
            String key = tool.getName().trim().toLowerCase(Locale.ROOT);

            if (toolNames.add(key)) {
                tools.add(tool);
                toolsByName.put(key, tool);
            } else {
                duplicateCount++;
            }
        }
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void showAllTools() {
        System.out.println("\nTools in the catalogue:");
        for (Tool tool : tools) {
            System.out.println("- " + tool.getName() + " (" + tool.getCategory()
                    + ", " + tool.getCatalogueGroup() + ") [" + tool.getVerificationStatus() + "]");
        }
    }
}

public class App {
    public static void main(String[] args) {
        Console console = System.console();

        if (console == null) {
            System.out.println("Run the app using the VS Code Run button.");
            return;
        }

        try {
            ToolRepository repository = new ToolRepository();
            List<Tool> databaseTools = repository.loadToolsWithRetry(console);

            ToolCatalogue catalogue = new ToolCatalogue();
            catalogue.loadTools(databaseTools);

            System.out.println("\n=== Workbench Lens ===");
            catalogue.showAllTools();

            if (catalogue.getDuplicateCount() > 0) {
                System.out.println("Skipped duplicate tool names: "
                        + catalogue.getDuplicateCount());
            }

            String search = console.readLine(
                    "\nType a tool's full name to learn about it: ");
            Tool tool = catalogue.findByName(search);

            if (tool == null) {
                System.out.println("I couldn't find that exact tool name in the catalogue.");
            } else {
                System.out.println("\nTool: " + tool.getName());
                System.out.println("Category: " + tool.getCategory());
                System.out.println("Catalogue group: " + tool.getCatalogueGroup());
                System.out.println("Purpose: " + tool.getPurpose());
                System.out.println("Safety notes: " + tool.getSafetyNotes());
                System.out.println("Verification status: "
                        + tool.getVerificationStatus());

                if ("UNVERIFIED".equalsIgnoreCase(tool.getVerificationStatus())) {
                    System.out.println("Note: This entry has not been reviewed yet.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not load tools from Oracle: " + e.getMessage());
        }
    }
}
