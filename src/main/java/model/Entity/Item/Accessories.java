package model.Entity.Item;

public class Accessories extends Item {

    public Accessories() {
        setCategory(Category.ACCESSORIES);
    }

    @Override
    public String getItemDetails() {
        return "Accessories: " + getName();
    }
}