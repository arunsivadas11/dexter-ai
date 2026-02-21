package app.dexter.ai.service;

import app.dexter.ai.dto.ParsedExpense;
import app.dexter.ai.dto.ParsedMeal;
import app.dexter.ai.model.Ingredient;
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

    public ParsedMeal parseMeal(List<String> ingredients) {
        String input = String.join(", ", ingredients);

        String prompt = """
            Suggest a meal that can be made using these ingredients: %s
            Return structured JSON:
            {
              "name": "Meal Name",
              "ingredients": ["ingredient1","ingredient2"]
            }
            Only return valid JSON
            """.formatted(input);

        String response = chatClient.prompt().user(prompt).call().content();
        String cleaned = cleanJson(response);

        try {
            return objectMapper.readValue(cleaned, ParsedMeal.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse meal: " + response);
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
