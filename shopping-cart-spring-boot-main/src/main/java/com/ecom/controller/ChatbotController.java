package com.ecom.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/user/chatbot")
public class ChatbotController {

    @Value("${chatbot.api.base-url}")
    private String apiBaseUrl;

    @Value("${chatbot.api.key}")
    private String apiKey;

    @Value("${chatbot.api.model}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SYSTEM_PROMPT = """
            You are a friendly and knowledgeable Game Store Assistant for an online game e-commerce platform.
            Your role is to help customers with:
            - Recommending games based on their preferences (genre, play style, budget)
            - Providing information about game genres, features, and gameplay
            - Answering questions about the store (purchasing, wallet top-up, game library, digital delivery)
            - Explaining game categories and helping users discover new games
            - Providing general gaming tips and news

            Guidelines:
            - Be enthusiastic and passionate about games
            - Keep responses concise but informative (2-4 sentences for simple questions)
            - If asked about specific prices or stock, mention that users can browse the Game Store page for the latest info
            - You can respond in both Thai and English, matching the user's language
            - Use gaming terminology naturally
            - If you don't know something specific about the store, be honest and suggest checking the website

            Store features available: Game Store browsing, Shopping Cart, Wallet system (top-up with PromptPay QR),
            Digital Game Library, Community posts, User profiles, and Secure digital delivery.
            """;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestBody Map<String, Object> request, Principal principal) {

        Map<String, Object> response = new HashMap<>();

        try {
            String userMessage = (String) request.get("message");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> history = (List<Map<String, String>>) request.getOrDefault("history",
                    new ArrayList<>());

            if (userMessage == null || userMessage.isBlank()) {
                response.put("success", false);
                response.put("reply", "กรุณาพิมพ์ข้อความ");
                return ResponseEntity.badRequest().body(response);
            }

            // Build messages array for the API
            List<Map<String, String>> messages = new ArrayList<>();

            // System prompt
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.add(systemMsg);

            // Add conversation history (last 10 exchanges max to save tokens)
            int historyStart = Math.max(0, history.size() - 20);
            for (int i = historyStart; i < history.size(); i++) {
                messages.add(history.get(i));
            }

            // Current user message
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            // Build API request body
            Map<String, Object> apiRequest = new HashMap<>();
            apiRequest.put("model", modelName);
            apiRequest.put("messages", messages);
            apiRequest.put("stream", false);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(apiRequest, headers);

            // Call KKU Gemini API
            String apiUrl = apiBaseUrl + "/chat/completions";
            @SuppressWarnings("unchecked")
            Map<String, Object> apiResponse = restTemplate.postForObject(apiUrl, entity, Map.class);

            // Extract reply from response
            if (apiResponse != null && apiResponse.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String reply = (String) message.get("content");
                    response.put("success", true);
                    response.put("reply", reply);
                    return ResponseEntity.ok(response);
                }
            }

            response.put("success", false);
            response.put("reply", "ขออภัย ไม่สามารถประมวลผลได้ในขณะนี้ กรุณาลองใหม่อีกครั้ง");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("reply", "เกิดข้อผิดพลาด: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
