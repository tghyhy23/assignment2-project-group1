package com.group01.asm2.models;

import java.math.BigDecimal;

public record RelatedAuction(
        String title,
        BigDecimal price,
        String meta
) {
}