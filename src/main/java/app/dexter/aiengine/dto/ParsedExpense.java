package app.dexter.aiengine.dto;

import java.math.BigDecimal;

public record ParsedExpense(
        BigDecimal amount,
        String currency,
        String vendor,
        String category,
        String date
) {}