package client.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FavouriteManagerTest {

    @BeforeEach
    void setUp() {
        FavouriteManager.clear();
    }

    // ==================== isFavourite ====================

    @Test
    void isFavourite_emptySet_returnsFalse() {
        assertFalse(FavouriteManager.isFavourite("item-1"));
    }

    @Test
    void isFavourite_afterAdd_returnsTrue() {
        FavouriteManager.addFavourite("item-1");
        assertTrue(FavouriteManager.isFavourite("item-1"));
    }

    @Test
    void isFavourite_differentItem_returnsFalse() {
        FavouriteManager.addFavourite("item-1");
        assertFalse(FavouriteManager.isFavourite("item-2"));
    }

    // ==================== addFavourite ====================

    @Test
    void addFavourite_sameItemTwice_onlyStoredOnce() {
        FavouriteManager.addFavourite("item-1");
        FavouriteManager.addFavourite("item-1");
        assertEquals(1, FavouriteManager.favouriteItemIds.size());
    }

    // ==================== removeFavourite ====================

    @Test
    void removeFavourite_existingItem_removedSuccessfully() {
        FavouriteManager.addFavourite("item-1");
        FavouriteManager.removeFavourite("item-1");
        assertFalse(FavouriteManager.isFavourite("item-1"));
    }

    @Test
    void removeFavourite_nonExistingItem_noError() {
        assertDoesNotThrow(() -> FavouriteManager.removeFavourite("not-there"));
    }

    // ==================== clear ====================

    @Test
    void clear_removesAllItems() {
        FavouriteManager.addFavourite("item-1");
        FavouriteManager.addFavourite("item-2");
        FavouriteManager.clear();
        assertTrue(FavouriteManager.favouriteItemIds.isEmpty());
    }

    // ==================== loadFromResponse ====================

    @Test
    void loadFromResponse_null_clearsSet() {
        FavouriteManager.addFavourite("item-old");
        FavouriteManager.loadFromResponse(null);
        assertTrue(FavouriteManager.favouriteItemIds.isEmpty());
    }

    @Test
    void loadFromResponse_blank_clearsSet() {
        FavouriteManager.addFavourite("item-old");
        FavouriteManager.loadFromResponse("   ");
        assertTrue(FavouriteManager.favouriteItemIds.isEmpty());
    }

    @Test
    void loadFromResponse_singleEntry_parsedCorrectly() {
        FavouriteManager.loadFromResponse("auction-1;item-42;extra");
        assertTrue(FavouriteManager.isFavourite("item-42"));
    }

    @Test
    void loadFromResponse_multipleEntries_allParsed() {
        FavouriteManager.loadFromResponse("a1;item-1;x|a2;item-2;y|a3;item-3;z");
        assertTrue(FavouriteManager.isFavourite("item-1"));
        assertTrue(FavouriteManager.isFavourite("item-2"));
        assertTrue(FavouriteManager.isFavourite("item-3"));
    }

    @Test
    void loadFromResponse_entryWithOnlyOneField_skipped() {
        FavouriteManager.loadFromResponse("onlyone|a2;item-5");
        assertFalse(FavouriteManager.isFavourite("onlyone"));
        assertTrue(FavouriteManager.isFavourite("item-5"));
    }

    @Test
    void loadFromResponse_replacesExistingData() {
        FavouriteManager.addFavourite("old-item");
        FavouriteManager.loadFromResponse("a1;new-item;x");
        assertFalse(FavouriteManager.isFavourite("old-item"));
        assertTrue(FavouriteManager.isFavourite("new-item"));
    }

    @Test
    void loadFromResponse_emptyItemId_addedToSet() {
        FavouriteManager.loadFromResponse("a1;;x");
        assertTrue(FavouriteManager.isFavourite(""));
    }
}