package model.Entity.Item;

public class Other extends Item {

    public Other() {
        setCategory(Category.OTHER);
    }

    @Override
    public String getItemDetails() {
        return "Other item: " + getName();
    }
}