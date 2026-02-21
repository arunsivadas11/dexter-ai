package app.dexter.ai.repository;

import app.dexter.ai.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    // Find meal by name across all users (for household-wide deduplication)
    Optional<Meal> findByName(String name);
}
