package com.group01.asm2.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class MoneyFormatter {

    private MoneyFormatter() {
    }

    public static String formatUSD(BigDecimal amount) {
        if (amount == null) {
            return "N/A";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(amount);
    }
}