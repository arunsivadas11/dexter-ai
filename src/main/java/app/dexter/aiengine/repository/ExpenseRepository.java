package app.dexter.aiengine.repository;

import app.dexter.aiengine.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}