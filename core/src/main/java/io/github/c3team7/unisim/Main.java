package io.github.c3team7.unisim;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends Game {
    private ShapeRenderer shapeRenderer;

    private SpriteBatch spriteBatch;
    private BitmapFont font;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();

        font = new BitmapFont(Gdx.files.internal("default.fnt"));
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        class ScreenInfo {
            public ScreenInfo(int width, int height, int refresh) {
                this.width = width;
                this.height = height;
                this.refresh = refresh;
            }

            public String toString() {
                return String.format("Resolution: %1$s x %2$s @ %3$s Hz", width, height, refresh); // string formatting
            }

            int width;
            int height;
            int refresh;
        }

        ScreenInfo screeninfo = new ScreenInfo(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Gdx.graphics.getDisplayMode().refreshRate);
        System.out.println(screeninfo);

        int tileSize = 20;
        for (int y = 0; y < screeninfo.width; y = y + tileSize) {
            for (int x = 0; x < screeninfo.width; x = x + tileSize) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor((x % (2 * tileSize) != y % (2 * tileSize)) ? Color.PURPLE : Color.BLUE);
                shapeRenderer.rect(x, y, tileSize, tileSize);
                shapeRenderer.end();
            }
        }

        spriteBatch.begin();
        font.draw(spriteBatch, "FPS:" + + Gdx.graphics.getFramesPerSecond(), 0, font.getLineHeight());
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}