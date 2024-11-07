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
    protected int ID;

    // this instantiator adds the sprite to the Map class's LinkedHashSet (list of entities that need to be rendered)
    public Sprite(Map map) {
        this.map = map;
        map.getEntities().add(this);
    }

    public int getID() {
        return ID;
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

    public float getWidth() {
        return rectangle.width;
    }

    public float getHeight() {
        return rectangle.height;
    }

    protected void setWidthHeight(float width, float height) {
        rectangle.setSize(width, height);
    }

    protected void setRectangeWidthHeightAuto() {
        texture = getTexture(); // we need to get the texture (since this calculation is done before drawing starts) here otherwise CRASH!
        rectangle.setSize(texture.getWidth(), texture.getHeight());
    }

    public abstract Texture createTexture();

    public void draw(SpriteBatch batch, float timeDelta) {
        batch.draw(getTexture(), rectangle.x, rectangle.y,
                getWidth()/2, getWidth()/2,
                getWidth(), getHeight(),
                1.0f, 1.0f, angle,
                0, 0,
                (int) getWidth(), (int) getHeight(),
                false, false);
    }
    
    public void destroy() {
        map.getEntities().remove(this);
    }

    public void dispose() {
        getTexture().dispose();
    }
}