package app.dexter.ai.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(
        name = "meals",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_telegram_user_meal_name", columnNames = {"telegram_user_id", "name"})
        }
)
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @Column(nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> ingredients; // now stored as proper JSONB

    @Column(name = "suggested_at")
    private LocalDateTime suggestedAt = LocalDateTime.now();
}