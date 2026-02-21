package app.dexter.ai.service;

import app.dexter.ai.dto.ParsedMeal;
import app.dexter.ai.model.Ingredient;
import app.dexter.ai.model.Meal;
import app.dexter.ai.model.MealSuggestion;
import app.dexter.ai.model.UserPreference;
import app.dexter.ai.repository.IngredientRepository;
import app.dexter.ai.repository.MealRepository;
import app.dexter.ai.repository.MealSuggestionRepository;
import app.dexter.ai.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MealService {

    private final IngredientRepository ingredientRepository;
    private final MealRepository mealRepository;
    private final MealSuggestionRepository suggestionRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final GeminiParser aiParser;

    public MealService(IngredientRepository ingredientRepository, MealRepository mealRepository,
                       MealSuggestionRepository suggestionRepository, UserPreferenceRepository preferenceRepository,
                       GeminiParser aiParser) {
        this.ingredientRepository = ingredientRepository;
        this.mealRepository = mealRepository;
        this.suggestionRepository = suggestionRepository;
        this.preferenceRepository = preferenceRepository;
        this.aiParser = aiParser;
    }

    // Add ingredient
    public void addIngredients(Long telegramUserId, List<String> items) {
        for (String item : items) {
            String trimmed = item.trim().toLowerCase();
            if (trimmed.isEmpty()) continue;

            // Check if ingredient exists in table (household-level)
            ingredientRepository.findByNameIgnoreCase(trimmed)
                    .orElseGet(() -> {
                        Ingredient ingredient = new Ingredient();
                        ingredient.setName(trimmed);
                        ingredient.setTelegramUserId(telegramUserId); // who added it
                        ingredient.setCreatedAt(LocalDateTime.now());
                        return ingredientRepository.save(ingredient);
                    });
        }
    }

    // List ingredients
    public List<Ingredient> listIngredients(Long telegramUserId) {
        return ingredientRepository.findByTelegramUserId(telegramUserId);
    }

    // Suggest meal
    public List<Meal> suggestMeals(Long telegramUserId) {

        // 1️⃣ Fetch ingredients
        List<Ingredient> ingredients = ingredientRepository.findByTelegramUserId(telegramUserId);
        if (ingredients.isEmpty()) return List.of();

        List<String> ingredientNames = ingredients.stream()
                .map(Ingredient::getName)
                .sorted(String::compareToIgnoreCase)
                .toList();

        // 2️⃣ Fetch preferences
        List<UserPreference> preferences = preferenceRepository.findAll();

        // 3️⃣ Call GeminiParser
        List<ParsedMeal> parsedMeals = aiParser.parseMultipleMealsWithPreferences(ingredientNames, preferences);

        // 4️⃣ Store meals in DB (deduplicate by user+meal name)
        List<Meal> results = new ArrayList<>();
        for (ParsedMeal pm : parsedMeals) {
            Meal meal = mealRepository.findByName(pm.name())
                    .orElseGet(() -> {
                        Meal newMeal = new Meal();
                        newMeal.setName(pm.name());
                        newMeal.setIngredients(pm.ingredients());
                        newMeal.setNote(pm.note());
                        newMeal.setSuggestedAt(LocalDateTime.now());
                        return mealRepository.save(newMeal);
                    });
            results.add(meal);
        }

        return results;
    }

    // Suggest healthier alternative
    public MealSuggestion suggestAlternative(Long telegramUserId, String eatenItem) {
        String alternative = aiParser.suggestHealthierOption(eatenItem);
        MealSuggestion suggestion = new MealSuggestion();
        suggestion.setTelegramUserId(telegramUserId);
        suggestion.setEatenItem(eatenItem);
        suggestion.setSuggestedAlternative(alternative);
        return suggestionRepository.save(suggestion);
    }

    public void savePreferencesFromInput(Long telegramUserId, String telegramUserName, String userInput) {
        // 1️⃣ Parse free-text input via AI
        List<UserPreference> parsedPreferences = aiParser.parseUserPreferences(telegramUserId, userInput);

        // 2️⃣ Save all preferences to DB
        for (UserPreference pref : parsedPreferences) {
            pref.setTelegramUserId(telegramUserId);
            pref.setMemberName(telegramUserName);
            pref.setCreatedAt(LocalDateTime.now());
            preferenceRepository.save(pref);
        }
    }

    // Optionally: fetch dislikes for meal suggestions
    public List<UserPreference> getDislikes(Long telegramUserId) {
        return preferenceRepository.findByTelegramUserIdAndLikesFalse(telegramUserId);
    }
}
