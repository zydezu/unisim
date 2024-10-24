package io.github.c3team7.unisim;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        System.out.println(Gdx.graphics.getHeight());
        int tileSize = 20;
        for (int y=0; y<Gdx.graphics.getHeight(); y = y + tileSize){
            for (int x=0; x<Gdx.graphics.getWidth(); x = x + tileSize) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor((x % (2 * tileSize) != y % (2 * tileSize)) ? Color.GREEN: Color.RED);
                shapeRenderer.rect(x, y, tileSize, tileSize);
                shapeRenderer.end();
            }
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
