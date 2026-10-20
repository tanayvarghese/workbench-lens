package com.workbenchlens;

import java.io.Console;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class Tool {
    private final String name;
    private final String category;
    private final String purpose;
    private final String safetyNotes;

    public Tool(String name, String category, String purpose, String safetyNotes) {
        this.name = name;
        this.category = category;
        this.purpose = purpose;
        this.safetyNotes = safetyNotes;
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
}

class HandTool extends Tool {
    public HandTool(String name, String category, String purpose, String safetyNotes) {
        super(name, category, purpose, safetyNotes);
    }
}

class PowerTool extends Tool {
    public PowerTool(String name, String category, String purpose, String safetyNotes) {
        super(name, category, purpose, safetyNotes);
    }
}

class MeasuringTool extends Tool {
    public MeasuringTool(String name, String category, String purpose, String safetyNotes) {
        super(name, category, purpose, safetyNotes);
    }
}

class ToolCatalogue {
    private final List<Tool> tools = new ArrayList<>();

    public Tool findByName(String searchText) {
        for (Tool tool : tools) {
            if (tool.getName().toLowerCase().contains(searchText.toLowerCase())) {
                return tool;
            }
        }
        return null;
    }

    public void loadTools(List<Tool> databaseTools) {
        tools.clear();
        tools.addAll(databaseTools);
    }

    public void showAllTools() {
        System.out.println("\nTools in the catalogue:");
        for (Tool tool : tools) {
            System.out.println("- " + tool.getName() + " (" + tool.getCategory() + ")");
        }
    }
}

public class App {
    public static void main(String[] args) {
        Console console = System.console();

        if (console == null) {
            System.out.println("Run the app using the VS Code Run button so the password prompt works.");
            return;
        }

        char[] password = console.readPassword("WORKBENCH password: ");

        try {
            ToolRepository repository = new ToolRepository();
            List<Tool> databaseTools =
                    repository.loadTools(new String(password));

            ToolCatalogue catalogue = new ToolCatalogue();
            catalogue.loadTools(databaseTools);

            System.out.println("\n=== Workbench Lens ===");
            catalogue.showAllTools();

            String search = console.readLine("\nType a tool name to learn about it: ");
            Tool tool = catalogue.findByName(search);

            if (tool == null) {
                System.out.println("I couldn't find that tool in the catalogue yet.");
            } else {
                System.out.println("\nTool: " + tool.getName());
                System.out.println("Category: " + tool.getCategory());
                System.out.println("Purpose: " + tool.getPurpose());
                System.out.println("Safety notes: " + tool.getSafetyNotes());
            }
        } catch (SQLException e) {
            System.out.println("Could not load tools from Oracle: " + e.getMessage());
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}