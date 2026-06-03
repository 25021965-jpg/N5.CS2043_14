package model.Entity.Item;

public class Vehicles extends Item {

    public Vehicles() {
        setCategory(Category.VEHICLES);
    }

    @Override
    public String getItemDetails() {
        return "Vehicle: " + getName();
    }
}