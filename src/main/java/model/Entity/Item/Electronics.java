package model.Entity.Item;

public class Electronics extends Item {

    public Electronics() {
        setCategory(Category.ELECTRONICS);
    }

    @Override
    public String getItemDetails() {
        return "Electronic item: " + getName();
    }
}