package model.Entity.Item;

public class HomeAppliances extends Item {

    public HomeAppliances() {
        setCategory(Category.HOME_APPLIANCES);
    }

    @Override
    public String getItemDetails() {
        return "Home appliance: " + getName();
    }
}