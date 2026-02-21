package app.dexter.ai.repository;

import app.dexter.ai.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    // List all ingredients for a user
    List<Ingredient> findByTelegramUserId(Long telegramUserId);

    Optional<Ingredient> findByNameIgnoreCase(String name);
}
