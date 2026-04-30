package model;

import java.io.Serializable;
import java.util.List;

public class Item implements Serializable {

    private String id;
    private String name;
    private String description;
    private List<String> images;
    private Category category;

    public Item() {}

    public Item(String id, String name, Category category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}