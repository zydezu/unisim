package io.github.c3team7.unisim;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Render {
    protected Set<Sprite> sprites = new LinkedHashSet<>();
    protected Set<Integer> setOfIDs = new HashSet<>();

    public Set<Sprite> getEntities() {
        return sprites;
    }

    public void addSprite(Sprite sprite) {
        if (setOfIDs.add(sprite.ID)) {
            getEntities().add(sprite);
            return;
        }
        System.err.println("WARNING! Tried to add a sprite but the ID already exists! ID: " + sprite.ID);
    }

    // return a sprite by ID
    public Sprite getSpriteByID(int ID) {
        for (Sprite sprite : sprites) {
            if (sprite.getID() == ID) {
                return sprite;
            }
        }
        return null; // return null if no sprite with the given ID is found
    }

    public void removeSprite(Sprite sprite) {
        if (setOfIDs.remove(sprite.ID)) { // check if sprite exists
            getEntities().remove(sprite);
            return;
        }
        System.err.println("WARNING! Tried to remove a sprite that doesn't exist! ID: " + sprite.ID);
    }

    public void removeSpriteByID(int ID) {
        Sprite sprite = getSpriteByID(ID);
        if (sprite != null) {
            if (setOfIDs.remove(ID)) { // check if sprite exists
                getEntities().remove(sprite);
                return;
            }
        }
        System.err.println("WARNING! Tried to remove a sprite that doesn't exist! ID: " + ID);
    }
}
