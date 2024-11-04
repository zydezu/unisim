package io.github.c3team7.unisim;

import java.util.ArrayList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.Input;
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
    int framesElapsed = 0;
    float timeElapsed = 0;
    float timeRemaining = 0;
    private final float timeAllowed = 300;

    protected Map map;

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

        screeninfo = new ScreenInfo(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
                Gdx.graphics.getDisplayMode().refreshRate);
        System.out.println(screeninfo); // DEBUG

        // CREATE MAP THEN CREATE ENTITIES FOR MAP

        map = new Map(); // store all sprites entities
        new Building(map, 50, 100, 0, 150, 150);
    }

    @Override
    public void render() {
        // inputs

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            Boolean fullScreen = Gdx.graphics.isFullscreen();
            Monitor currMonitor = Gdx.graphics.getMonitor();
            if (fullScreen) {
                Gdx.graphics.setWindowedMode(1280, 720);
            } else {
                DisplayMode displayMode = Gdx.graphics.getDisplayMode(currMonitor);
                if (!Gdx.graphics.setFullscreenMode(displayMode)) {
                    // failed
                }
            }

        }

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

        framesElapsed++;
        timeElapsed = timeElapsed + Gdx.graphics.getDeltaTime();
        timeRemaining = timeAllowed - timeElapsed;
        // delta time is the amount of time since the last frame AKA games use this
        // value to keep movement at the same speed no matter the framerate

        // DRAWING ORDER -> bottom layer -> top layer ( text is at the end as we want to
        // draw it ontop of the sprites )
        spriteBatch.begin();

        // draw sprites first
        for (Sprite sprite : new ArrayList<>(map.getEntities())) {
            sprite.draw(spriteBatch, Gdx.graphics.getDeltaTime());
        }

        font.getData().setScale(1);
        font.draw(spriteBatch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 0, font.getLineHeight());

        font.draw(spriteBatch, "FRAMES ELAPSED: " + framesElapsed, 0, 150);
        font.draw(spriteBatch, "TIME ELAPSED: " + timeElapsed, 0, 180);
        font.draw(spriteBatch, "TIME REMAINING: " + timeRemaining, 0, 210);

        font.getData().setScale(2);
        font.draw(spriteBatch, "UNISIM", 200, 500);

        spriteBatch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}