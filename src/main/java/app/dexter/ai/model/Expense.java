package app.dexter.ai.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramUserId;

    private BigDecimal amount;
    private String currency;
    private String vendor;
    private String category;
    private LocalDate expenseDate;

    private LocalDateTime createdAt = LocalDateTime.now();
}
