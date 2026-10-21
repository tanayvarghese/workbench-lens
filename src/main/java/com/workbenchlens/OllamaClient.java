package com.workbenchlens;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/** Shared client for local Ollama text and vision requests. */
public class OllamaClient {
    private static final String MODEL = "minicpm-v";
    private static final URI ENDPOINT = URI.create("http://localhost:11434/api/generate");
    private static final ObjectMapper JSON = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    public String ask(String prompt, byte[] image) throws IOException, InterruptedException {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("model", MODEL);
        payload.put("prompt", prompt);
        payload.put("stream", false);
        if (image != null) payload.put("images", new String[]{Base64.getEncoder().encodeToString(image)});
        HttpRequest request = HttpRequest.newBuilder(ENDPOINT)
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(payload)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Ollama returned HTTP " + response.statusCode());
        }
        JsonNode body = JSON.readTree(response.body());
        if (body.path("response").isMissingNode()) throw new IOException("Ollama returned an invalid response.");
        return body.path("response").asText();
    }
}
