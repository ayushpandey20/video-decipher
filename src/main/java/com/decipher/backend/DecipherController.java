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
    private String apiKeyUpperProperty;

    @Value("${gemini.api.key:}")
    private String apiKeyLowerProperty;

    private final RestClient restClient = RestClient.builder().build();

    @PostMapping("/api/ask")
    public ResponseEntity<Map<String, String>> askGemini(@RequestBody Map<String, String> request) {
        String videoUrl = request.get("prompt");

        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("response", "[ERROR]: Video URL or prompt cannot be empty."));
        }

        String key = resolveKey();

        if (key == null || key.isBlank()) {
            return ResponseEntity.ok(Map.of("response", "[SYSTEM ERROR]: GEMINI_API_KEY is missing from environment."));
        }

        String cleanKey = key.trim().replaceAll("^\"|\"$", "");

        try {
            String promptText = "Provide a clear structured summary with key takeaways and bullet points for this video topic or link: " + videoUrl.trim();

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of(
                        "parts", List.of(
                            Map.of("text", promptText)
                        )
                    )
                )
            );

            String endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + cleanKey;

            Map<?, ?> response = restClient.post()
                    .uri(endpointUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            String aiResponse = extractResponseText(response);
            return ResponseEntity.ok(Map.of("response", aiResponse));

        } catch (RestClientResponseException e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("response", "[GEMINI API ERROR " + e.getStatusCode() + "]: " + e.getResponseBodyAsString()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("response", "[SYSTEM ERROR]: " + e.getMessage()));
        }
    }

    private String resolveKey() {
        if (apiKeyUpperProperty != null && !apiKeyUpperProperty.isBlank()) {
            return apiKeyUpperProperty;
        }
        if (apiKeyLowerProperty != null && !apiKeyLowerProperty.isBlank()) {
            return apiKeyLowerProperty;
        }
        return System.getenv("GEMINI_API_KEY");
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
            return "Unable to parse response output.";
        }
    }
}
