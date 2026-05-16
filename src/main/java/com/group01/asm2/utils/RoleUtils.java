package com.group01.asm2.utils;

import com.group01.asm2.enums.UserRole;
import com.group01.asm2.models.Person;

public final class RoleUtils {
    private RoleUtils() {}

    public static boolean isSeller(Person person) {
        return person != null && person.getRole() == UserRole.SELLER;
    }

    public static boolean isBuyer(Person person) {
        return person != null && person.getRole() == UserRole.BUYER;
    }

    public static boolean isSystemAdmin(Person person) {
        return person != null && person.getRole() == UserRole.SYSTEM_ADMINISTRATOR;
    }

    public static boolean isAuctionAdmin(Person person) {
        return person != null && person.getRole() == UserRole.AUCTION_ADMINISTRATOR;
    }

    public static boolean isRegisteredUser(Person person) {
        return person != null &&
            (person.getRole() == UserRole.BUYER || person.getRole() == UserRole.SELLER);
    }
}