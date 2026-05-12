package com.group01.asm2.models;

public class ItemImage {
    private Integer id;
    private Integer itemId;
    private String imageUrl;
    private Integer displayOrder;

    public ItemImage() {
    }

    public ItemImage(Integer id, Integer itemId, String imageUrl, Integer displayOrder) {
        this.id = id;
        this.itemId = itemId;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }

    public boolean belongsToItem(Integer itemId) {
        return this.itemId != null && this.itemId.equals(itemId);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}