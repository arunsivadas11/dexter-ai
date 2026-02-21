package app.dexter.ai.repository;

import app.dexter.ai.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    // Get all preferences for a user
    List<UserPreference> findByTelegramUserId(Long telegramUserId);

    // Get dislikes for a user (for AI prompt)
    List<UserPreference> findByTelegramUserIdAndLikesFalse(Long telegramUserId);
}
