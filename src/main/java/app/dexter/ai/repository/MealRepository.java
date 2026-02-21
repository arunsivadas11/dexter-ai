package app.dexter.ai.repository;

import app.dexter.ai.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    // Get all meals suggested for a user, most recent first
    List<Meal> findByTelegramUserIdOrderBySuggestedAtDesc(Long telegramUserId);
}
