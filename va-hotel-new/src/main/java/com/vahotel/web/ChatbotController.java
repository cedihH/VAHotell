package com.vahotel.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vahotel.model.Room;
import com.vahotel.repo.RoomRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;

@Controller
public class ChatbotController {
    private static final Logger log = LoggerFactory.getLogger(ChatbotController.class);
    @Value("${groq.api.key:}") private String key;
    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}") private String url;
    @Value("${groq.api.model:openai/gpt-oss-20b}") private String model;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RoomRepository rooms;

    public ChatbotController(RoomRepository rooms) { this.rooms = rooms; }

    @GetMapping("/chatbot")
    public String page(Authentication authentication) {
        boolean manager = authentication != null && authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_ONSITE_MANAGER")
                        || a.getAuthority().equals("ROLE_ONLINE_MANAGER"));
        return manager ? "redirect:/" : "redirect:/#hotel-assistant";
    }

    @PostMapping("/chatbot")
    public String ask(@RequestParam String question, Model model) {
        question = question == null ? "" : question.trim();
        if (question.isEmpty() || question.length() > 1000) {
            model.addAttribute("error", "Enter a question up to 1,000 characters.");
            model.addAttribute("assistantMode", "Room-data FAQ mode");
            return pageWithAnswer(model, "");
        }
        List<Room> currentRooms = rooms.findByActiveTrue();
        String roomFacts = currentRooms.stream()
                .map(r -> r.getRoomNumber() + " | " + r.getRoomType() + " | ₱" + r.getNightlyPrice()
                        + " per night | " + r.getDescription())
                .reduce((a, b) -> a + "\n" + b).orElse("No active rooms are currently listed.");
        String answer = "VA Hotel rooms currently listed: " + roomFacts
                + "\nAvailability depends on your dates; please use the booking form to check. "
                + "Payments in this app are simulations only.";
        boolean aiAnswered = false;
        String fallbackReason = "";
        if (key != null && !key.isBlank()) {
            try {
                ObjectNode payload = mapper.createObjectNode();
                payload.put("model", this.model);
                ArrayNode msgs = payload.putArray("messages");
                msgs.addObject().put("role", "system").put("content",
                        "You are VA Hotel's concise guest support assistant. Use only these current room records as facts:\n"
                        + roomFacts + "\nDo not infer availability for dates, invent hotel policies or prices, "
                        + "or claim access to private bookings/payments. Say to contact staff when the records "
                        + "do not answer. Payments are simulations only.");
                msgs.addObject().put("role", "user").put("content", question);
                HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(25))
                        .header("Authorization", "Bearer " + key).header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
                HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
                JsonNode json = mapper.readTree(res.body());
                String responseText = json.path("choices").path(0).path("message").path("content").asText("").trim();
                if (res.statusCode() == 200 && !responseText.isBlank()) {
                    answer = responseText;
                    aiAnswered = true;
                } else {
                    fallbackReason = json.path("error").path("message").asText("HTTP " + res.statusCode());
                    if (fallbackReason.length() > 240) fallbackReason = fallbackReason.substring(0, 240);
                    log.warn("Groq request returned HTTP {} ({}). Showing the room-data FAQ fallback.", res.statusCode(), fallbackReason);
                }
            } catch (Exception exception) {
                fallbackReason = exception.getClass().getSimpleName();
                log.warn("Groq request failed; showing the room-data FAQ fallback: {}", exception.getMessage());
            }
        }
        model.addAttribute("question", question);
        model.addAttribute("answer", answer);
        model.addAttribute("assistantMode", aiAnswered ? "Groq AI"
                : (key == null || key.isBlank() ? "Room-data FAQ mode" : "Groq unavailable · FAQ fallback"));
        if (!fallbackReason.isBlank()) model.addAttribute("assistantDiagnostic", fallbackReason);
        model.addAttribute("rooms", currentRooms);
        return "chatbot";
    }

    private String pageWithAnswer(Model model, String answer) {
        model.addAttribute("answer", answer);
        model.addAttribute("rooms", rooms.findByActiveTrue());
        return "chatbot";
    }
}
