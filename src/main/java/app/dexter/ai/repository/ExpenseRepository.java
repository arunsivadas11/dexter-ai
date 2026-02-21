package app.dexter.ai.repository;

import app.dexter.ai.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Find all expenses for a specific Telegram user
    List<Expense> findByTelegramUserId(Long telegramUserId);
}