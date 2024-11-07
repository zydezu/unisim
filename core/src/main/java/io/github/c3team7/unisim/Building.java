package io.github.c3team7.unisim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Building extends Sprite {
    
    public static final String ASSET = "centralhall.jpg";

    public Building(Render render, float x, float y, float angle, int ID) {
        super(render);
        rectangle = new Rectangle();
        this.ID = ID;
        setRectangeWidthHeightAuto();
        setPos(x, y, angle);
    }

    public Building(Render render, float x, float y, float angle, float width, float height, int ID) {
        this(render, x, y, angle, ID);
        setWidthHeight(width, height);
    }

    @Override
    public Texture createTexture() {
        return new Texture(Gdx.files.internal(ASSET));
    }
}
