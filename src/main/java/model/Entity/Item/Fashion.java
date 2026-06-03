package model.Entity.Item;

public class Fashion extends Item {

    public Fashion() {
        setCategory(Category.FASHION);
    }

    @Override
    public String getItemDetails() {
        return "Fashion item: " + getName();
    }
}