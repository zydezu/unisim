package io.github.c3team7.unisim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Sprite {

    protected Texture texture;
    protected Rectangle rectangle;
    protected Map map;
    protected float angle = 0.0f;

    public Sprite(Map map) {
        this.map = map;
        map.getEntities().add(this);
    }

    public final Texture getTexture() {
        if (texture == null) {
            texture = createTexture();
        }
        return texture;
    }

    protected void setPos(float x, float y) {
        setPos(x, y, 90f);
    }

    protected void setPos(float x, float y, float angle) {
        rectangle.x = x;
        rectangle.y = y;
        this.angle = angle;
    }

    protected void setWidthHeight(float width, float height) {
        rectangle.width = width;
        rectangle.height = height;
    }

    public abstract Texture createTexture();

    public void draw(SpriteBatch batch, float timeDelta) {
        batch.draw(getTexture(), rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }
}