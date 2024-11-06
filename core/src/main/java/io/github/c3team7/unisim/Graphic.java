package io.github.c3team7.unisim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

//

public class Graphic extends Sprite {
    
    public static String ASSET = "assets\\graphics\\defaultgraphic.png";

    // automatically set the width and height of the sprite its to original resolution
    public Graphic(Map map, float x, float y, float angle, String asset) {
        super(map);
        rectangle = new Rectangle();
        setRectangeWidthHeightAuto();
        setPos(x, y, angle);
        if (asset != "") {
            ASSET = asset;
        }
    }

    // manually set the width and height of the sprite (useful for scaling down large images)
    public Graphic(Map map, float x, float y, float angle, float width, float height, String asset) {
        this(map, x, y, angle, asset);
        setWidthHeight(width, height);
    }

    @Override
    public Texture createTexture() {
        return new Texture(Gdx.files.internal(ASSET));
    }
}
