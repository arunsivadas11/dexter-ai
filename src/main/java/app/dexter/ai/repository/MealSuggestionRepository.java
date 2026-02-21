package app.dexter.ai.repository;

import app.dexter.ai.model.MealSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealSuggestionRepository extends JpaRepository<MealSuggestion, Long> {

    // Get all past suggestions for a user
    List<MealSuggestion> findByTelegramUserIdOrderByCreatedAtDesc(Long telegramUserId);
}
