package app.dexter.ai.service;

import app.dexter.ai.dto.ParsedExpense;
import app.dexter.ai.dto.ParsedMeal;
import app.dexter.ai.model.Ingredient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeminiParser {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiParser(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public ParsedExpense parse(String input) {
        log.info("Requesting AI with prompt");
        String response = null;
        try {
            String prompt = """
                    Extract structured expense data from this message.
                    
                    Return ONLY valid JSON in this format:
                    {
                      "amount": number,
                      "currency": "INR",
                      "vendor": string,
                      "category": string,
                      "date": "YYYY-MM-DD"
                    }
                    
                    Do not include markdown formatting.
                    Do not wrap in ```json.
                    Only return raw JSON.
                    
                    Message: "%s"
                    """.formatted(input);

            response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            String cleaned = cleanJson(response);

            return objectMapper.readValue(cleaned, ParsedExpense.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + response);
        }
    }

    private String cleanJson(String response) {
        if (response == null) {
            return "";
        }

        // Remove markdown code fences
        return response.replace("```json", "").replace("```", "").trim();
    }

    public List<ParsedMeal> parseMultipleMeals(List<String> availableIngredients) {
        String input = String.join(", ", availableIngredients);

        String prompt = """
                You are a home chef assistant. Based on the following available ingredients: %s,\s
                    suggest 3-4 meals. Each meal can use a subset of the ingredients, not necessarily all.
                    Your suggestions should follow these preferences:
                    1️⃣ Mostly South Indian meals (like dosa, idli, sambar, upma, rice-based dishes)
                    2️⃣ Sometimes North Indian meals (like paratha, poha, paneer curry)
                    3️⃣ Occasionally English/Continental breakfast (like French toast, scrambled eggs, boiled eggs)
                    
                    Return a JSON array of objects with each meal containing:
                    {
                        "name": "Meal Name",
                        "ingredients": ["ingredient1", "ingredient2", ...]
                    }
                    Only return valid JSON. Do not include any extra text.
            """.formatted(input);

        String response = chatClient.prompt().user(prompt).call().content().trim();
        response = response.replace("```json", "").replace("```", "").trim();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse multiple meals: " + response, e);
        }
    }

    public String suggestHealthierOption(String eatenItem) {
        String prompt = """
            Suggest a healthier alternative to the following meal: %s
            Return only the alternative as plain text
            """.formatted(eatenItem);

        return chatClient.prompt().user(prompt).call().content().trim();
    }
}
