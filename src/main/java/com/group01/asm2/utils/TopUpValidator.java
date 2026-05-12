package com.group01.asm2.utils;

public final class TopUpValidator {

    private static final double MIN_AMOUNT = 10.0;
    private static final double MAX_AMOUNT = 5000.0;

    private TopUpValidator() {
    }

    public static double validateAmount(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Top-up amount is required.");
        }

        double amount;

        try {
            amount = Double.parseDouble(input.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Please enter a valid number.");
        }

        if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
            throw new IllegalArgumentException("Amount must be between $10 and $5000.");
        }

        return amount;
    }
}