package app.dexter.ai.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "meal_suggestions")
public class MealSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @Column(name = "eaten_item", nullable = false)
    private String eatenItem;

    @Column(name = "suggested_alternative", nullable = false)
    private String suggestedAlternative;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}