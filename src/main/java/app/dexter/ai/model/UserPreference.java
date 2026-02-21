package app.dexter.ai.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @Column(nullable = false)
    private String memberName; // e.g., "Alice", "Bob", "Everyone" (optional)

    @Column(nullable = false)
    private String itemName; // e.g., "Idly", "French Toast"

    @Column(nullable = false)
    private Boolean likes; // true = like, false = dislike

    @Column(name = "note")
    private String note; // Optional note like "limit once per week"

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
