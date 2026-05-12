package com.group01.asm2.models;

public class NotificationItem {
    private final Item item;
    private final String title;
    private final String message;
    private final String time;
    private final boolean unread;

    public NotificationItem(Item item, String title, String message, String time, boolean unread) {
        this.item = item;
        this.title = title;
        this.message = message;
        this.time = time;
        this.unread = unread;
    }

    public Item getItem() {
        return item;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public boolean isUnread() {
        return unread;
    }
}