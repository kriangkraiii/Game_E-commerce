package com.ecom.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/user/chatbot")
public class ChatbotController {

    @Value("${chatbot.api.base-url}")
    private String apiBaseUrl;

    @Value("${chatbot.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "pdf", "txt", "docx", "pptx", "xlsx", "csv", "json");

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private static final Set<String> TEXT_EXTENSIONS = Set.of("txt", "csv", "json");

    // ── Provider → daily token limit ──
    private static final Map<String, Integer> PROVIDER_LIMITS = new LinkedHashMap<>();
    static {
        PROVIDER_LIMITS.put("Gemini", 350000);
        PROVIDER_LIMITS.put("Claude", 150000);
        PROVIDER_LIMITS.put("OpenAI", 150000);
        PROVIDER_LIMITS.put("Meta AI", 200000);
        PROVIDER_LIMITS.put("Nova (AWS)", 200000);
        PROVIDER_LIMITS.put("Deepseek", 1000000);
        PROVIDER_LIMITS.put("xAI", 200000);
        PROVIDER_LIMITS.put("Perplexity", 200000);
        PROVIDER_LIMITS.put("Qwen", 200000);
        PROVIDER_LIMITS.put("Mistral", 150000);
    }

    // ── Model ID → Provider mapping ──
    private static final Map<String, String> MODEL_PROVIDER = new LinkedHashMap<>();
    static {
        MODEL_PROVIDER.put("gemini-3.1-pro-preview", "Gemini");
        MODEL_PROVIDER.put("gemini-3-pro-preview", "Gemini");
        MODEL_PROVIDER.put("gemini-3-flash-preview", "Gemini");
        MODEL_PROVIDER.put("gemini-2.5-pro", "Gemini");
        MODEL_PROVIDER.put("gemini-2.5-flash", "Gemini");
        MODEL_PROVIDER.put("gemini-2.5-flash-lite", "Gemini");
        MODEL_PROVIDER.put("claude-sonnet-4.6", "Claude");
        MODEL_PROVIDER.put("claude-sonnet-4.5", "Claude");
        MODEL_PROVIDER.put("claude-haiku-4.5", "Claude");
        MODEL_PROVIDER.put("claude-sonnet-4", "Claude");
        MODEL_PROVIDER.put("claude-3.7-sonnet", "Claude");
        MODEL_PROVIDER.put("gpt-5.2", "OpenAI");
        MODEL_PROVIDER.put("gpt-5.1", "OpenAI");
        MODEL_PROVIDER.put("gpt-5.1-codex", "OpenAI");
        MODEL_PROVIDER.put("gpt-5", "OpenAI");
        MODEL_PROVIDER.put("gpt-5-mini", "OpenAI");
        MODEL_PROVIDER.put("gpt-5-nano", "OpenAI");
        MODEL_PROVIDER.put("llama-4-maverick", "Meta AI");
        MODEL_PROVIDER.put("llama-4-scout", "Meta AI");
        MODEL_PROVIDER.put("deepseek-v3.2", "Deepseek");
        MODEL_PROVIDER.put("deepseek-v3.2-exp", "Deepseek");
        MODEL_PROVIDER.put("deepseek-chat-v3.1", "Deepseek");
        MODEL_PROVIDER.put("grok-4.1-fast", "xAI");
        MODEL_PROVIDER.put("grok-4", "xAI");
        MODEL_PROVIDER.put("grok-3", "xAI");
        MODEL_PROVIDER.put("qwen3-235b-a22b-2507", "Qwen");
        MODEL_PROVIDER.put("qwen3-next-80b-a3b-instruct", "Qwen");
        MODEL_PROVIDER.put("qwen3-coder-flash", "Qwen");
        MODEL_PROVIDER.put("qwen3-coder", "Qwen");
        MODEL_PROVIDER.put("qwen3-vl-32b-instruct", "Qwen");
        MODEL_PROVIDER.put("mistral-large-2512", "Mistral");
        MODEL_PROVIDER.put("mistral-medium-3", "Mistral");
        MODEL_PROVIDER.put("codestral-2508", "Mistral");
        MODEL_PROVIDER.put("devstral-medium", "Mistral");
        MODEL_PROVIDER.put("codestral-2501", "Mistral");
        MODEL_PROVIDER.put("nova-pro-v1", "Nova (AWS)");
    }

    private final ConcurrentHashMap<String, Integer> dailyTokenUsage = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = """
            You are a friendly and knowledgeable Game Store Assistant for an online game e-commerce platform.
            Your role is to help customers with:
            - Recommending games based on their preferences (genre, play style, budget)
            - Providing information about game genres, features, and gameplay
            - Answering questions about the store (purchasing, wallet top-up, game library, digital delivery)
            - Explaining game categories and helping users discover new games
            - Providing general gaming tips and news
            - Analyzing images or files that users upload (screenshots, game info, etc.)

            Guidelines:
            - Be enthusiastic and passionate about games
            - Keep responses concise but informative (2-4 sentences for simple questions)
            - If asked about specific prices or stock, mention that users can browse the Game Store page for the latest info
            - You can respond in both Thai and English, matching the user's language
            - Use gaming terminology naturally
            - If a user uploads an image, describe what you see and relate it to gaming if possible
            - If a user uploads a text/data file, analyze the content and provide helpful insights

            Store features available: Game Store browsing, Shopping Cart, Wallet system (top-up with PromptPay QR),
            Digital Game Library, Community posts, User profiles, and Secure digital delivery.
            """;

    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> getModels() {
        Map<String, Object> result = new HashMap<>();
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : MODEL_PROVIDER.entrySet()) {
            grouped.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }
        result.put("providers", grouped);
        result.put("defaultModel", "gemini-2.5-flash");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/usage")
    public ResponseEntity<Map<String, Object>> getUsage() {
        Map<String, Object> result = new HashMap<>();
        String today = LocalDate.now().toString();

        List<Map<String, Object>> providerUsages = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : PROVIDER_LIMITS.entrySet()) {
            String provider = entry.getKey();
            int limit = entry.getValue();
            int used = dailyTokenUsage.getOrDefault(provider + ":" + today, 0);
            double percent = limit > 0 ? Math.min(100.0, (used * 100.0) / limit) : 0;

            Map<String, Object> info = new HashMap<>();
            info.put("provider", provider);
            info.put("used", used);
            info.put("limit", limit);
            info.put("percent", Math.round(percent * 10.0) / 10.0);
            providerUsages.add(info);
        }

        result.put("usage", providerUsages);
        result.put("date", today);
        return ResponseEntity.ok(result);
    }

    // ── Send chat message (with optional file attachment) ──
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam("message") String userMessage,
            @RequestParam(value = "model", defaultValue = "gemini-2.5-flash") String modelId,
            @RequestParam(value = "history", defaultValue = "[]") String historyJson,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Parse history JSON
            List<Map<String, Object>> history;
            try {
                history = objectMapper.readValue(historyJson, new TypeReference<List<Map<String, Object>>>() {
                });
            } catch (Exception e) {
                history = new ArrayList<>();
            }

            if ((userMessage == null || userMessage.isBlank()) && file == null) {
                response.put("success", false);
                response.put("reply", "กรุณาพิมพ์ข้อความหรือแนบไฟล์");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate file if present
            if (file != null && !file.isEmpty()) {
                if (file.getSize() > MAX_FILE_SIZE) {
                    response.put("success", false);
                    response.put("reply", "⚠️ ไฟล์ขนาดเกิน 10MB กรุณาลดขนาดไฟล์");
                    return ResponseEntity.badRequest().body(response);
                }

                String ext = getFileExtension(file.getOriginalFilename());
                if (!ALLOWED_EXTENSIONS.contains(ext)) {
                    response.put("success", false);
                    response.put("reply", "⚠️ ไม่รองรับไฟล์นามสกุล ." + ext);
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Validate model
            String provider = MODEL_PROVIDER.get(modelId);
            if (provider == null) {
                response.put("success", false);
                response.put("reply", "ไม่พบโมเดล: " + modelId);
                return ResponseEntity.badRequest().body(response);
            }

            // Check daily limit
            String today = LocalDate.now().toString();
            String usageKey = provider + ":" + today;
            int currentUsage = dailyTokenUsage.getOrDefault(usageKey, 0);
            int limit = PROVIDER_LIMITS.getOrDefault(provider, 0);

            if (currentUsage >= limit) {
                response.put("success", false);
                response.put("reply", "⚠️ โควตา " + provider + " วันนี้หมดแล้ว (" + limit
                        + " tokens) กรุณาเลือกโมเดลจาก provider อื่น หรือรอหลังเที่ยงคืนครับ");
                return ResponseEntity.ok(response);
            }

            // Build messages array for the API
            List<Map<String, Object>> messages = new ArrayList<>();

            // System prompt
            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.add(systemMsg);

            // History (last 20 messages)
            int historyStart = Math.max(0, history.size() - 20);
            for (int i = historyStart; i < history.size(); i++) {
                messages.add(history.get(i));
            }

            // Build user message (with or without file)
            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");

            if (file != null && !file.isEmpty()) {
                String ext = getFileExtension(file.getOriginalFilename());

                if (IMAGE_EXTENSIONS.contains(ext)) {
                    // Image: send as multimodal content (text + image_url)
                    List<Map<String, Object>> contentParts = new ArrayList<>();

                    if (userMessage != null && !userMessage.isBlank()) {
                        Map<String, Object> textPart = new HashMap<>();
                        textPart.put("type", "text");
                        textPart.put("text", userMessage);
                        contentParts.add(textPart);
                    }

                    // Encode image as base64 data URI
                    String base64 = Base64.getEncoder().encodeToString(file.getBytes());
                    String mimeType = "image/" + (ext.equals("jpg") ? "jpeg" : ext);
                    String dataUri = "data:" + mimeType + ";base64," + base64;

                    Map<String, Object> imagePart = new HashMap<>();
                    imagePart.put("type", "image_url");
                    Map<String, String> imageUrl = new HashMap<>();
                    imageUrl.put("url", dataUri);
                    imagePart.put("image_url", imageUrl);
                    contentParts.add(imagePart);

                    userMsg.put("content", contentParts);

                } else if (TEXT_EXTENSIONS.contains(ext)) {
                    // Text file: read content and append
                    String fileContent = new BufferedReader(
                            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining("\n"));

                    // Truncate very large files
                    if (fileContent.length() > 10000) {
                        fileContent = fileContent.substring(0, 10000) + "\n... (truncated)";
                    }

                    String combinedMessage = (userMessage != null && !userMessage.isBlank() ? userMessage + "\n\n" : "")
                            + "📎 File: " + file.getOriginalFilename() + "\n```\n" + fileContent + "\n```";
                    userMsg.put("content", combinedMessage);

                } else {
                    // Other doc types (pdf, docx, pptx, xlsx): send as base64 file
                    // Try sending as file reference with description
                    String base64 = Base64.getEncoder().encodeToString(file.getBytes());
                    String mimeType = file.getContentType() != null ? file.getContentType()
                            : "application/octet-stream";

                    List<Map<String, Object>> contentParts = new ArrayList<>();

                    String textContent = (userMessage != null && !userMessage.isBlank() ? userMessage + "\n\n" : "")
                            + "📎 Attached file: " + file.getOriginalFilename() + " (" + formatFileSize(file.getSize())
                            + ")";
                    Map<String, Object> textPart = new HashMap<>();
                    textPart.put("type", "text");
                    textPart.put("text", textContent);
                    contentParts.add(textPart);

                    // Try to send as file URL (some models support this)
                    Map<String, Object> filePart = new HashMap<>();
                    filePart.put("type", "file");
                    Map<String, String> fileData = new HashMap<>();
                    fileData.put("filename", file.getOriginalFilename());
                    fileData.put("file_data", "data:" + mimeType + ";base64," + base64);
                    filePart.put("file", fileData);
                    contentParts.add(filePart);

                    userMsg.put("content", contentParts);
                }
            } else {
                userMsg.put("content", userMessage);
            }

            messages.add(userMsg);

            // API request
            Map<String, Object> apiRequest = new HashMap<>();
            apiRequest.put("model", modelId);
            apiRequest.put("messages", messages);
            apiRequest.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(apiRequest, headers);
            String apiUrl = apiBaseUrl + "/chat/completions";

            @SuppressWarnings("unchecked")
            Map<String, Object> apiResponse = restTemplate.postForObject(apiUrl, entity, Map.class);

            if (apiResponse != null && apiResponse.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String reply = (String) message.get("content");

                    int tokensUsed = extractTokenUsage(apiResponse);
                    dailyTokenUsage.merge(usageKey, tokensUsed, (a, b) -> a + b);

                    int updatedUsage = dailyTokenUsage.getOrDefault(usageKey, 0);
                    double percent = limit > 0 ? Math.min(100.0, (updatedUsage * 100.0) / limit) : 0;

                    response.put("success", true);
                    response.put("reply", reply);
                    response.put("tokensUsed", tokensUsed);
                    response.put("providerUsage", Math.round(percent * 10.0) / 10.0);
                    response.put("provider", provider);
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

    private String getFileExtension(String filename) {
        if (filename == null)
            return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }

    private String formatFileSize(long size) {
        if (size >= 1024 * 1024)
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        if (size >= 1024)
            return String.format("%.0f KB", size / 1024.0);
        return size + " B";
    }

    private int extractTokenUsage(Map<String, Object> apiResponse) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) apiResponse.get("usage");
            if (usage != null && usage.containsKey("total_tokens")) {
                Object total = usage.get("total_tokens");
                if (total instanceof Number) {
                    return ((Number) total).intValue();
                }
            }
        } catch (Exception ignored) {
        }
        return 500;
    }
}
