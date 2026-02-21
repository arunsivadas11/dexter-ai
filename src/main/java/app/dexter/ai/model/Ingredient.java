package app.dexter.ai.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ingredients")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @Column(nullable = false)
    private String name;

    @Column
    private String quantity;

    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();
}