package app.dexter.ai.dto;

import java.util.List;

public record ParsedMeal(String name, List<String> ingredients, String note) {

}