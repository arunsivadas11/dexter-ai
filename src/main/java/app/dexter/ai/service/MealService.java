package app.dexter.ai.service;

import app.dexter.ai.dto.ParsedMeal;
import app.dexter.ai.model.Ingredient;
import app.dexter.ai.model.Meal;
import app.dexter.ai.model.MealSuggestion;
import app.dexter.ai.repository.IngredientRepository;
import app.dexter.ai.repository.MealRepository;
import app.dexter.ai.repository.MealSuggestionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MealService {

    private final IngredientRepository ingredientRepository;
    private final MealRepository mealRepository;
    private final MealSuggestionRepository suggestionRepository;
    private final GeminiParser aiParser;

    public MealService(IngredientRepository ingredientRepository,
                       MealRepository mealRepository,
                       MealSuggestionRepository suggestionRepository,
                       GeminiParser aiParser) {
        this.ingredientRepository = ingredientRepository;
        this.mealRepository = mealRepository;
        this.suggestionRepository = suggestionRepository;
        this.aiParser = aiParser;
    }

    // Add ingredient
    public Ingredient addIngredient(Long telegramUserId, String name) {
        Ingredient ing = new Ingredient();
        ing.setTelegramUserId(telegramUserId);
        ing.setName(name);
        return ingredientRepository.save(ing);
    }

    // List ingredients
    public List<Ingredient> listIngredients(Long telegramUserId) {
        return ingredientRepository.findByTelegramUserId(telegramUserId);
    }

    // Suggest meal
    public List<Meal> suggestMeals(Long telegramUserId) {

        // 1️⃣ Get all ingredients for this user
        List<Ingredient> ingredients = ingredientRepository.findByTelegramUserId(telegramUserId);
        List<String> ingredientNames = ingredients.stream()
                .map(Ingredient::getName)
                .sorted(String::compareToIgnoreCase) // optional: alphabetical
                .toList();

        if (ingredientNames.isEmpty()) {
            return Collections.emptyList();
        }

        // 2️⃣ AI suggests 3–4 meals using subsets of ingredients
        List<ParsedMeal> parsedMeals = aiParser.parseMultipleMeals(ingredientNames);

        List<Meal> results = new ArrayList<>();

        for (ParsedMeal parsedMeal : parsedMeals) {

            // 3️⃣ Check if meal already exists in DB for this user
            Meal meal = mealRepository.findByTelegramUserIdAndName(telegramUserId, parsedMeal.name())
                    .orElseGet(() -> {
                        Meal newMeal = new Meal();
                        newMeal.setTelegramUserId(telegramUserId);
                        newMeal.setName(parsedMeal.name());
                        newMeal.setIngredients(parsedMeal.ingredients());
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

    public List<Meal> getMealLog(Long telegramUserId) {
        return mealRepository.findByTelegramUserIdOrderBySuggestedAtDesc(telegramUserId);
    }
}
