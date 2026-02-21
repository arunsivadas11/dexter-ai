package app.dexter.aiengine.service;

import app.dexter.aiengine.dto.ParsedExpense;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class GeminiExpenseParser {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiExpenseParser(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public ParsedExpense parse(String input) {

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

                Message: "%s"
                """.formatted(input);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        try {
            return objectMapper.readValue(response, ParsedExpense.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + response);
        }
    }
}
