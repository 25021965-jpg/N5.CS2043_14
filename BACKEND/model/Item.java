package model;

import java.io.Serializable;
import java.util.List;

public class Item implements Serializable {
    private String id;
    private String name;
    private String description;
    private String category;
    private List<String> images;

    public void setImages(List<String> images) {
        this.images = images;
    }
}
