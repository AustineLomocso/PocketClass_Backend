package org.example.edu_linked;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GeminiService {
    @Value("${gemini.api.key}")
    private String apiKey;

    public String generateLessonModule(String studentMessage) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        String requestBody = """
            {
              "system_instruction": {
                "parts": [{
                  "text": "You are a 5th-grade math tutor for students in the rural Philippines. Based on the user's SMS request, generate a comprehensive lesson module. You MUST output ONLY valid JSON. The JSON must exactly match this structure: { \\"lesson_title\\": \\"string\\", \\"complete_module\\": { \\"introduction\\": \\"string\\", \\"step_by_step_examples\\": [\\"string\\"] }, \\"lesson_summary\\": \\"string\\", \\"practice_quizzes\\": [ { \\"question\\": \\"string\\", \\"options\\": [\\"string\\"] } ], \\"answer_key\\": [ { \\"question_number\\": \\"integer\\", \\"correct_answer\\": \\"string\\", \\"explanation\\": \\"string\\" } ], \\"practical_project_idea\\": \\"string\\" }. Use simple English and localized Philippine examples (e.g., mangoes, sari-sari store items, pesos)."
                }]
              },
              "contents": [{"parts": [{"text": "%s"}]}],
              "generationConfig": {
                "responseMimeType": "application/json"
              }
            }
            """.formatted(studentMessage);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // --- NEW EXTRACTION LOGIC ---
        // Parse the massive response to grab just the generated text
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body());

        // Navigate through Gemini's JSON structure: candidates[0] -> content -> parts[0] -> text
        String generatedText = rootNode.path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text").asText();

        // Sometimes LLMs wrap JSON in markdown block tags (```json ... ```). This cleans it up safely.
        return generatedText.replace("```json\n", "").replace("```", "").trim();
    }
}