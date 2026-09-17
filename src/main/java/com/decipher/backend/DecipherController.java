package com.decipher.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class DecipherController {

    @Value("${GEMINI_API_KEY:}")
    private String apiKeyProperty;

    private final RestClient restClient = RestClient.builder().build();

    @PostMapping("/api/ask")
    public ResponseEntity<Map<String, String>> askGemini(@RequestBody Map<String, String> request) {
        String videoUrl = request.get("prompt");

        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("response", "[ERROR]: Video URL or prompt cannot be empty."));
        }

        String key = (apiKeyProperty != null && !apiKeyProperty.isBlank()) 
                ? apiKeyProperty 
                : System.getenv("GEMINI_API_KEY");

        if (key == null || key.isBlank()) {
            return ResponseEntity.ok(Map.of("response", "[SYSTEM ERROR]: GEMINI_API_KEY is missing."));
        }

        try {
            // Structuring YouTube URL as a file_data object rather than plain text
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of(
                        "parts", List.of(
                            Map.of("text", "Analyze and summarize the key insights and main topics from this video:"),
                            Map.of("file_data", Map.of("file_uri", videoUrl.trim()))
                        )
                    )
                )
            );

            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent";

            Map<?, ?> response = restClient.post()
                    .uri(endpoint)
                    .header("x-goog-api-key", key.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            String aiResponse = extractResponseText(response);
            return ResponseEntity.ok(Map.of("response", aiResponse));

        } catch (RestClientResponseException e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("response", "[GEMINI API ERROR]: " + e.getResponseBodyAsString()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("response", "[SYSTEM ERROR]: " + e.getMessage()));
        }
    }

    private String extractResponseText(Map<?, ?> response) {
        try {
            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
            return (String) firstPart.get("text");
        } catch (Exception e) {
            return "Failed to parse Gemini API response output.";
        }
    }
}