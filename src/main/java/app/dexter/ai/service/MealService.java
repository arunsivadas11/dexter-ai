package app.dexter.ai.service;

import app.dexter.ai.dto.ParsedMeal;
import app.dexter.ai.model.Ingredient;
import app.dexter.ai.model.Meal;
import app.dexter.ai.model.MealSuggestion;
import app.dexter.ai.repository.IngredientRepository;
import app.dexter.ai.repository.MealRepository;
import app.dexter.ai.repository.MealSuggestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

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
    public Ingredient addIngredient(Long telegramUserId, String name, String quantity) {
        Ingredient ing = new Ingredient();
        ing.setTelegramUserId(telegramUserId);
        ing.setName(name);
        ing.setQuantity(quantity);
        return ingredientRepository.save(ing);
    }

    // List ingredients
    public List<Ingredient> listIngredients(Long telegramUserId) {
        return ingredientRepository.findByTelegramUserId(telegramUserId);
    }

    // Suggest meal
    public Meal suggestMeal(Long telegramUserId) {
        List<Ingredient> ingredients = ingredientRepository.findByTelegramUserId(telegramUserId);

        List<String> ingredientNames = ingredients.stream()
                .map(Ingredient::getName)
                .toList();

        ParsedMeal parsedMeal = aiParser.parseMeal(ingredientNames);

        Meal meal = new Meal();
        meal.setTelegramUserId(telegramUserId);
        meal.setName(parsedMeal.name());
        meal.setIngredients(parsedMeal.ingredients()); // directly a List<String>
        return mealRepository.save(meal);
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
