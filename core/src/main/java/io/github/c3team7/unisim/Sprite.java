package io.github.c3team7.unisim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Sprite {

    protected Texture texture;
    protected Rectangle rectangle;
    protected Render render;
    protected float angle = 0.0f;
    protected float scalex = 1.0f;
    protected float scaley = 1.0f;
    protected int ID;

    // this instantiator adds the sprite to the Map class's LinkedHashSet (list of
    // entities that need to be rendered)
    public Sprite(Render render) {
        this.render = render;
        render.getEntities().add(this);
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
        setPos(x, y, this.angle);
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
        texture = getTexture(); // we need to get the texture (since this calculation is done before drawing
                                // starts) here otherwise CRASH!
        rectangle.setSize(texture.getWidth(), texture.getHeight());
    }

    protected void setScale(float scalex, float scaley) {
        this.scalex = scalex;
        this.scaley = scaley;
    }

    public abstract Texture createTexture();

    public void draw(SpriteBatch batch, float timeDelta) {
        // batch.draw(getTexture(), rectangle.x, rectangle.y, rectangle.width, rectangle.height);

        batch.draw(getTexture(),
                rectangle.x, rectangle.y, // Position
                rectangle.width / 2, rectangle.height / 2, // Origin at center
                rectangle.width, rectangle.height, // Dimensions
                scalex, scaley, // Scaling
                angle, // Rotation
                0, 0, // Texture region start (bottom-left corner)
                (int) getWidth(), (int) getHeight(), // Texture region dimensions
                false, false); // Flip on X and Y
    }

    public void destroy() {
        render.getEntities().remove(this);
    }

    public void dispose() {
        getTexture().dispose();
    }
}