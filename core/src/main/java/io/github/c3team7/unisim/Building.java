package io.github.c3team7.unisim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Building extends Sprite {
    
    public static final String ASSET = "centralhall.jpg";

    public Building(Map map, float x, float y, float angle) {
        super(map);
        rectangle = new Rectangle();
        setPos(x, y, angle);
    }

    public Building(Map map, float x, float y, float angle, float width, float height) {
        this(map, x, y, angle);
        setWidthHeight(width, height);
    }

    @Override
    public Texture createTexture() {
        return new Texture(Gdx.files.internal(ASSET));
    }
}
