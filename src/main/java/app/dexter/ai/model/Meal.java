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
                @UniqueConstraint(name = "uk_telegram_user_meal_name", columnNames = {"name"})
        }
)
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> ingredients; // now stored as proper JSONB

    @Column(name = "note")
    private String note; // NEW: optional note about preferences, limits, etc.

    @Column(name = "suggested_at")
    private LocalDateTime suggestedAt = LocalDateTime.now();
}