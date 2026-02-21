package app.dexter.aiengine.service;

import app.dexter.aiengine.dto.ParsedExpense;
import app.dexter.aiengine.model.Expense;
import app.dexter.aiengine.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ExpenseService {

    private final GeminiExpenseParser parser;
    private final ExpenseRepository repository;

    public ExpenseService(GeminiExpenseParser parser, ExpenseRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    public Expense logExpense(Long telegramUserId, String message) {

        ParsedExpense parsed = parser.parse(message);

        Expense expense = new Expense();
        expense.setTelegramUserId(telegramUserId);
        expense.setAmount(parsed.amount());
        expense.setCurrency(parsed.currency());
        expense.setVendor(parsed.vendor());
        expense.setCategory(parsed.category());
        expense.setExpenseDate(LocalDate.parse(parsed.date()));

        return repository.save(expense);
    }
}
