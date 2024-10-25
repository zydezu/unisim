package io.github.c3team7.unisim;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends Game {
    private OrthographicCamera gameCamera;
    private OrthographicCamera hudCamera;
    private ShapeRenderer shapeRenderer;

    private Vector2 playerPosition = new Vector2();
    private boolean cameraFollowsPlayer = true;

    private SpriteBatch spriteBatch;
    private BitmapFont font;

    ScreenInfo screeninfo;

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

    @Override
    public void create() {
        float aspectRatio = (float) Gdx.graphics.getHeight() / (float) Gdx.graphics.getWidth();
        float viewableWorldWidth = 32.0f;
        gameCamera = new OrthographicCamera(viewableWorldWidth, viewableWorldWidth * aspectRatio);
        gameCamera.position.set(playerPosition.x, playerPosition.y, 1.0f);

        hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.position.set(hudCamera.viewportWidth / 2.0f, hudCamera.viewportHeight / 2.0f, 1.0f);

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();

        font = new BitmapFont(Gdx.files.internal("default.fnt"));    

        screeninfo = new ScreenInfo(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Gdx.graphics.getDisplayMode().refreshRate);
        System.out.println(screeninfo);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameCamera.update();

        int tileSize = 20;
        for (int y = 0; y < screeninfo.height; y = y + tileSize) {
            for (int x = 0; x < screeninfo.width; x = x + tileSize) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor((x % (2 * tileSize) != y % (2 * tileSize)) ? Color.PURPLE : Color.BLUE);
                shapeRenderer.rect(x, y, tileSize, tileSize);
                shapeRenderer.end();
            }
        }

        spriteBatch.begin();
        font.getData().setScale(1);
        font.draw(spriteBatch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 0, font.getLineHeight());
        font.getData().setScale(5);
        font.draw(spriteBatch, "UNISIM", 200, 500);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}