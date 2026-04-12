package model;

import java.util.List;

public class Item {
    private String id;
    private String name;
    private String description;
    private String category;
    private List<String> images;

    public void setImages(List<String> images) {
        this.images = images;
    }
}
