package model.Entity.Item;

import model.Entity.Entity;

import java.util.ArrayList;
import java.util.List;

public abstract class Item extends Entity {

    private String item_id;
    private String name;
    private String description;
    private List<String> images = new ArrayList<>();
    private Category category;

    public Item() {
    }

    public Item(String item_id, String name, Category category) {
        this.item_id = item_id;
        this.name = name;
        this.category = category;
    }

    // Entity (abstract)

    @Override
    public String getId() {
        return item_id;
    }

    @Override
    public String getSummary() {
        return "[" + getCategory() + "] " + name;
    }

    public abstract String getItemDetails();

    // Getters / Setters

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
}