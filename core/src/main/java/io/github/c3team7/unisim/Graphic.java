package io.github.c3team7.unisim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Graphic extends Sprite {
    
    public static String ASSET = "graphics/defaultgraphic.png";

    // automatically set the width and height of the sprite its to original resolution
    public Graphic(Render render, float x, float y, float angle, int ID, String asset) {
        super(render);
        rectangle = new Rectangle();
        this.ID = ID;
        if (asset != "") {
            ASSET = asset;
        }
        setPos(x, y, angle);
        setRectangeWidthHeightAuto(); // should be last in this subroutine, you can't change the texture after this 
    }

    // manually set the width and height of the sprite (useful for scaling down large images)
    public Graphic(Render render, float x, float y, float angle, float width, float height, int ID, String asset) {
        this(render, x, y, angle, ID, asset);
        setWidthHeight(width, height);
    }

    public Graphic(Render render, float x, float y, float angle, float scale, int ID, String asset) {
        this(render, x, y, angle, ID, asset);
        setScale(scale, scale);
    }

    @Override
    public Texture createTexture() {
        return new Texture(Gdx.files.internal(ASSET));
    }
}
