package app.dexter.aiengine.service;

import app.dexter.aiengine.dto.ParsedExpense;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeminiExpenseParser {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiExpenseParser(ChatClient.Builder builder) {
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
        response = response.replace("```json", "")
                .replace("```", "")
                .trim();

        return response;
    }
}
