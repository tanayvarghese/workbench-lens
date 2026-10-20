package com.workbenchlens;

import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class LocalVisionTest {
    private static final String MODEL = "qwen2.5vl:3b";
    private static final String OLLAMA_URL =
            "http://localhost:11434/api/generate";

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

            String imageBase64 = Base64.getEncoder()
                    .encodeToString(Files.readAllBytes(imagePath));

            String imagePrompt = "Identify the likely tool in this image. Describe visible "
                    + "features and likely purpose. If uncertain, say so. Do not guess the brand "
                    + "or exact model.";

            String imageDescription = askOllama(imagePrompt, imageBase64);

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

                    String answer = askOllama(questionPrompt, null);
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
            context.append("Safety notes: ").append(tool.getSafetyNotes()).append("\n\n");
        }

        return context.toString();
    }

    private static String askOllama(String prompt, String imageBase64)
            throws IOException, InterruptedException {
        String requestJson = "{\"model\":\"" + MODEL
                + "\",\"prompt\":\"" + escapeJson(prompt) + "\"";

        if (imageBase64 != null) {
            requestJson += ",\"images\":[\"" + imageBase64 + "\"]";
        }

        requestJson += ",\"stream\":false}";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL))
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Ollama returned HTTP "
                    + response.statusCode() + ": " + response.body());
        }

        return extractResponse(response.body());
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String extractResponse(String json) {
        String marker = "\"response\":\"";
        int start = json.indexOf(marker);

        if (start < 0) {
            return json;
        }

        start += marker.length();
        StringBuilder result = new StringBuilder();
        boolean escaped = false;

        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                switch (c) {
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    default -> result.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return result.toString();
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}