package org.example.edu_linked;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {
    private final GeminiService geminiService;
    private final FirestoreService firestoreService;

    public WebhookController(GeminiService geminiService, FirestoreService firestoreService) {
        this.geminiService = geminiService;
        this.firestoreService = firestoreService;
    }

    @PostMapping("/sms")
    public ResponseEntity<String> receiveSms(@RequestBody SmsRequest request) {
        try {
            // 1. Generate the JSON module
            String aiResponse = geminiService.generateLessonModule(request.message());

            // 2. Save it to Firestore
            firestoreService.saveLessonModule(request.phoneNumber(), aiResponse);

            // 3. Return the JSON to Postman so you can see it!
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(aiResponse);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}