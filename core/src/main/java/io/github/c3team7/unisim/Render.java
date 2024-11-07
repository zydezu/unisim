package io.github.c3team7.unisim;

import java.util.LinkedHashSet;
import java.util.Set;

public class Render {
    // stores all sprites that will be rendered (this includes gameplay tiles and title screen graphics)
    protected Set<Sprite> sprites = new LinkedHashSet<>();

    

    public Set<Sprite> getEntities() {
        return sprites;
    }

    // return a sprite by ID
    public Sprite getSpriteByID(int ID) {
        for (Sprite sprite : sprites) {
            if (sprite.getID() == ID) {
                return sprite;
            }
        }
        return null;  // return null if no sprite with the given ID is found
    }
}
