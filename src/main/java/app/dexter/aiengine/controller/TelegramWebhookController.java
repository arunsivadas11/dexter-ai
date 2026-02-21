package app.dexter.aiengine.controller;

import app.dexter.aiengine.model.Expense;
import app.dexter.aiengine.service.ExpenseService;
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

    private final ExpenseService expenseService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    private String botToken;

    public TelegramWebhookController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<String> onUpdateReceived(@RequestBody Map<String, Object> update) {

        if (update.containsKey("message")) {
            Map<String, Object> message = (Map<String, Object>) update.get("message");

            Map<String, Object> chat = (Map<String, Object>) message.get("chat");
            Long chatId = Long.valueOf(chat.get("id").toString());
            String text = (String) message.get("text");

            Expense saved = expenseService.logExpense(chatId, text);

            sendMessage(chatId,
                    "✅ Logged ₹" + saved.getAmount() +
                            " at " + saved.getVendor() +
                            " (" + saved.getCategory() + ")");
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