package app.dexter.aiengine.controller;

import app.dexter.aiengine.model.Expense;
import app.dexter.aiengine.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class TelegramWebhookController {

    private final ExpenseRepository expenseRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    private String botToken;

    public TelegramWebhookController(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @PostMapping
    public ResponseEntity<String> onUpdateReceived(@RequestBody Map<String, Object> update) {

        if (update.containsKey("message")) {
            Map<String, Object> message = (Map<String, Object>) update.get("message");

            Long chatId = Long.valueOf(message.get("chat").toString()
                    .replaceAll(".*id=(\\d+).*", "$1"));

            String text = (String) message.get("text");

            // Save to DB
            Expense expense = new Expense();
            expense.setTelegramUserId(chatId);
            expense.setMessage(text);
            expenseRepository.save(expense);

            // Send reply
            sendMessage(chatId, "✅ Saved: " + text);
        }

        return ResponseEntity.ok("OK");
    }

    private void sendMessage(Long chatId, String text) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);

        restTemplate.postForObject(url, body, String.class);
    }
}