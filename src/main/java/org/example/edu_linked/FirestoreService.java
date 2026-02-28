package org.example.edu_linked;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class FirestoreService {
    public void saveLessonModule(String phoneNumber, String geminiJsonOutput) {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> data = new HashMap<>();
        data.put("module_content", geminiJsonOutput);
        data.put("timestamp", System.currentTimeMillis());
        data.put("status", "ready_for_sync");

        db.collection("students").document(phoneNumber)
                .collection("modules").document(UUID.randomUUID().toString())
                .set(data);
    }
}