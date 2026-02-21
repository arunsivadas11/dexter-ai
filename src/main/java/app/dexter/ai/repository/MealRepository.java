package app.dexter.ai.repository;

import app.dexter.ai.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    // Get all meals for a user, most recent first
    List<Meal> findByTelegramUserIdOrderBySuggestedAtDesc(Long telegramUserId);

    // Find a meal by userId + meal name (for uniqueness check)
    Optional<Meal> findByTelegramUserIdAndName(Long telegramUserId, String name);
}
