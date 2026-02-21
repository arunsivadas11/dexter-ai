package app.dexter.ai.service;

import app.dexter.ai.dto.ParsedExpense;
import app.dexter.ai.dto.ParsedMeal;
import app.dexter.ai.model.UserPreference;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<ParsedMeal> parseMultipleMealsWithPreferences(List<String> ingredientNames,
                                                              List<UserPreference> preferences) {

        // Prepare dislikes/notes for AI prompt
        StringBuilder dislikeNote = new StringBuilder();
        for (UserPreference pref : preferences) {
            if (!pref.getLikes()) {
                dislikeNote.append(pref.getItemName());
                if (pref.getNote() != null && !pref.getNote().isBlank()) {
                    dislikeNote.append(" (").append(pref.getNote()).append(")");
                }
                dislikeNote.append(", ");
            }
        }
        String dislikeSummary = !dislikeNote.isEmpty() ? dislikeNote.substring(0, dislikeNote.length()-2) : "None";

        String inputIngredients = String.join(", ", ingredientNames);

        // AI prompt
        String prompt = """
            You are a home chef assistant. Suggest 3–4 meals using the following available ingredients: %s.
            Each meal can use a subset of ingredients (2–3 items is fine). 
            Consider all family members' dislikes/limitations: %s. Do not suggest these items frequently.
            Cuisine preference: mostly South Indian, sometimes North Indian, rarely English/Continental (like French Toast, Scrambled Eggs).
            Return a JSON array of objects:
               [
                {
                    "name": "Meal Name",
                    "ingredients": ["ingredient1", "ingredient2"],
                    "note": "Optional note including which member dislikes/likes this meal"
                }
               ]
            Only return valid JSON without extra text.
            """.formatted(inputIngredients, dislikeSummary);

        // Call Gemini API
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content()
                .trim();
        response = response.replace("```json", "").replace("```", "").trim();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response, new TypeReference<List<ParsedMeal>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse meals from AI response: " + response, e);
        }
    }

    public String suggestHealthierOption(String eatenItem) {
        String prompt = """
            Suggest a healthier alternative to the following meal: %s
            Return only the alternative as plain text
            """.formatted(eatenItem);

        return chatClient.prompt().user(prompt).call().content().trim();
    }

    public List<UserPreference> parseUserPreferences(Long telegramUserId, String userInput) {

        String prompt = """
        You are a smart assistant that converts free-text family meal preferences
        into a structured format for a database. The user input is: "%s".
        
        Rules:
        1️⃣ Identify the member(s) this applies to (default 'Everyone' if not mentioned).
        2️⃣ Identify the dish or ingredient.
        3️⃣ Determine like/dislike or limitation.
        4️⃣ Add an optional note if there are suggestions like 'less frequent' or 'weekdays only'.
        5️⃣ Return JSON array of objects:
        [
          {
            "memberName": "Alice",
            "itemName": "Idly",
            "likes": false,
            "note": "Limit once per week, weekdays only"
          }
        ]
        Only return valid JSON.
        """.formatted(userInput);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content()
                .trim();

        response = response.replace("```json", "").replace("```", "").trim();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response, new TypeReference<List<UserPreference>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse user preferences: " + response, e);
        }
    }
}
