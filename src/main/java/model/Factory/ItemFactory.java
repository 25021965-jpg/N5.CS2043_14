package model.Factory;

import model.Entity.Item.*;

public class ItemFactory {

    public static Item create(Category category) {
        if (category == null) return new Other();

        return switch (category) {
            case ACCESSORIES      -> new Accessories();
            case COLLECTIBLES     -> new Collectibles();
            case ELECTRONICS      -> new Electronics();
            case FASHION          -> new Fashion();
            case HOME_APPLIANCES  -> new HomeAppliances();
            case VEHICLES         -> new Vehicles();
            case OTHER            -> new Other();
        };
    }

    public static Item createFromCategory(String categoryStr) {
        if (categoryStr == null) return new Other();

        try {
            return create(Category.valueOf(categoryStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return new Other();
        }
    }
}