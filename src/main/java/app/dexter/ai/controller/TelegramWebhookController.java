package app.dexter.ai.controller;

import app.dexter.ai.model.Expense;
import app.dexter.ai.model.Ingredient;
import app.dexter.ai.model.Meal;
import app.dexter.ai.model.MealSuggestion;
import app.dexter.ai.service.ExpenseService;
import app.dexter.ai.service.MealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
@Slf4j
public class TelegramWebhookController {

    private final ExpenseService expenseService;
    private final MealService mealService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    private String botToken;

    public TelegramWebhookController(ExpenseService expenseService, MealService mealService) {
        this.expenseService = expenseService;
        this.mealService = mealService;
    }

    @PostMapping
    public ResponseEntity<String> onUpdateReceived(@RequestBody Map<String, Object> update) {
        log.info("Received message from bot: {}", update);
        if (!update.containsKey("message")) {
            return ResponseEntity.ok("No message");
        }

        Map<String, Object> message = (Map<String, Object>) update.get("message");
        Map<String, Object> chat = (Map<String, Object>) message.get("chat");
        Long chatId = Long.valueOf(chat.get("id").toString());
        String text = (String) message.get("text");

        try {
            if (text.startsWith("/add_ingredient")) {
                String[] parts = text.split(" ", 3);
                if (parts.length < 2) {
                    sendMessage(chatId, "Usage: /add_ingredient <name> [quantity]");
                } else {
                    String name = parts[1];
                    String quantity = parts.length == 3 ? parts[2] : "";
                    Ingredient ing = mealService.addIngredient(chatId, name, quantity);
                    sendMessage(chatId, "✅ Added ingredient: " + ing.getName() + " (" + ing.getQuantity() + ")");
                }

            } else if (text.startsWith("/list_ingredients")) {
                List<Ingredient> ingredients = mealService.listIngredients(chatId);
                if (ingredients.isEmpty()) {
                    sendMessage(chatId, "No ingredients found.");
                } else {
                    StringBuilder sb = new StringBuilder("📝 Ingredients:\n");
                    ingredients.forEach(i -> sb.append("- ").append(i.getName())
                            .append(" (").append(i.getQuantity()).append(")\n"));
                    sendMessage(chatId, sb.toString());
                }

            } else if (text.startsWith("/suggest_meal")) {
                Meal meal = mealService.suggestMeal(chatId);
                sendMessage(chatId, "🍽 Suggested Meal: " + meal.getName() +
                        "\nIngredients: " + meal.getIngredients());

            } else if (text.startsWith("/ate_out")) {
                String[] parts = text.split(" ", 2);
                if (parts.length < 2) {
                    sendMessage(chatId, "Usage: /ate_out <item>");
                } else {
                    String eatenItem = parts[1];
                    MealSuggestion suggestion = mealService.suggestAlternative(chatId, eatenItem);
                    sendMessage(chatId, "🥗 Healthier alternative: " + suggestion.getSuggestedAlternative());
                }

            } else if (text.startsWith("/meal_log")) {
                List<Meal> meals = mealService.getMealLog(chatId);
                if (meals.isEmpty()) {
                    sendMessage(chatId, "No meal suggestions logged yet.");
                } else {
                    StringBuilder sb = new StringBuilder("📋 Meal Log:\n");
                    meals.forEach(m -> sb.append("- ").append(m.getName())
                            .append(" | Ingredients: ").append(m.getIngredients()).append("\n"));
                    sendMessage(chatId, sb.toString());
                }

            } else {
                // Default: try parsing as expense
                Expense saved = expenseService.logExpense(chatId, text);
                sendMessage(chatId, "✅ Logged ₹" + saved.getAmount() +
                        " at " + saved.getVendor() +
                        " (" + saved.getCategory() + ")");
            }

        } catch (Exception e) {
            sendMessage(chatId, "⚠️ Error: " + e.getMessage());
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