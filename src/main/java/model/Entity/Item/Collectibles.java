package model.Entity.Item;

public class Collectibles extends Item {

    public Collectibles() {
        setCategory(Category.COLLECTIBLES);
    }

    @Override
    public String getItemDetails() {
        return "Collectible: " + getName();
    }
}