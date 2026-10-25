package com.workbenchlens;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class LocalVisionTest {
    private static final OllamaClient OLLAMA = new OllamaClient();

    public static void main(String[] args) throws InterruptedException {
        Console console = System.console();

        if (console == null) {
            System.out.println("Run this using the VS Code Run button.");
            return;
        }   

        try {
            String imageName = console.readLine("Enter the full path to a tool photo: ");
            Path imagePath = Path.of(imageName.trim());

            if (!Files.isRegularFile(imagePath)) {
                System.out.println("I couldn't find that image file. Check the path and try again.");
                return;
            }

            byte[] image = Files.readAllBytes(imagePath);

            String imagePrompt = "Identify the likely tool in this image. Describe visible "
                    + "features and likely purpose. If uncertain, say so. Do not guess the brand "
                    + "or exact model.";

            String imageDescription = OLLAMA.ask(imagePrompt, image);

            System.out.println("\nAI image description:");
            System.out.println(imageDescription);

            char[] password = console.readPassword(
                    "\nWORKBENCH password (typing stays hidden): ");

            try {
                ToolRepository repository = new ToolRepository();
                List<Tool> tools = repository.loadTools(new String(password));

                if (tools.isEmpty()) {
                    System.out.println("The Oracle catalogue has no tools yet.");
                    return;
                }

                String catalogueContext = buildCatalogueContext(tools);
                System.out.println("\nOracle catalogue loaded: " + tools.size() + " tools.");
                System.out.println("Ask questions about the tool. Type 'exit' to quit.");

                while (true) {
                    String question = console.readLine("\nYour question: ");

                    if (question == null || question.trim().equalsIgnoreCase("exit")) {
                        System.out.println("Workbench Lens session ended.");
                        break;
                    }

                    if (question.isBlank()) {
                        System.out.println("Enter a question, or type 'exit' to quit.");
                        continue;
                    }

                    String questionPrompt = """
                            You are Workbench Lens, a tool-information assistant.
                            Answer the user's question using the Oracle catalogue and image description below.

                            Rules:
                            - Treat Oracle catalogue entries as the source of stored facts.
                            - The image description is an AI estimate, not a confirmed identification.
                            - If the catalogue does not contain the requested fact, say it is not in the catalogue.
                            - Do not invent a brand, exact model, or technical specification.
                            - Give cautious, practical safety guidance; recommend the manufacturer's manual
                              for model-specific instructions.

                            AI image description:
                            %s

                            Oracle catalogue:
                            %s

                            User question:
                            %s
                            """.formatted(imageDescription, catalogueContext, question);

                    String answer = OLLAMA.ask(questionPrompt, null);
                    System.out.println("\nWorkbench Lens answer:");
                    System.out.println(answer);
                }
            } finally {
                Arrays.fill(password, '\0');
            }
        } catch (IOException e) {
            System.out.println("Could not read the image or contact local Ollama: "
                    + e.getMessage());
            System.out.println("Check that Ollama is running and try again.");
        } catch (SQLException e) {
            System.out.println("Could not load the Oracle catalogue: "
                    + e.getMessage());
        }
    }

    private static String buildCatalogueContext(List<Tool> tools) {
        StringBuilder context = new StringBuilder();

        for (Tool tool : tools) {
            context.append("Tool: ").append(tool.getName()).append('\n');
            context.append("Category: ").append(tool.getCategory()).append('\n');
            context.append("Purpose: ").append(tool.getPurpose()).append('\n');
            context.append("Safety notes: ").append(tool.getSafetyNotes()).append('\n');
            context.append("Verification status: ")
                    .append(tool.getVerificationStatus()).append("\n\n");
        }

        return context.toString();
    }

}
