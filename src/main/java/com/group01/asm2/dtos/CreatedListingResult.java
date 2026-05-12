package com.group01.asm2.dtos;

import com.group01.asm2.models.Auction;
import com.group01.asm2.models.Item;

public class CreatedListingResult {
    private final Item item;
    private final Auction auction;

    public CreatedListingResult(Item item, Auction auction) {
        this.item = item;
        this.auction = auction;
    }

    public Item getItem() {
        return item;
    }

    public Auction getAuction() {
        return auction;
    }
}