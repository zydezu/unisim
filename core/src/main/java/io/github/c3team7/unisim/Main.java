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
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.viewport.FitViewport;


// TO DO
//
// Research AssetManager() - https://libgdx.com/wiki/managing-your-assets
// HAI ! ^_^

public class Main extends Game {
    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private ShapeRenderer shapeRenderer;

    private Vector2 playerPosition = new Vector2();
    private boolean cameraFollowsPlayer = true;

    private SpriteBatch batch;
    private BitmapFont font;

    ScreenInfo screeninfo;

    //game state
    enum State {
        TITLE,
        GAMEPLAY,
        PAUSED,
        GAMEOVER
    }

    State gameState = State.TITLE; 

    //timer
    int framesElapsed = 0;
    float timeElapsed = 0;
    float timeRemaining = 0;
    private final float timeAllowed = 300;

    //mouse position
    int mouseX = 0;
    int mouseY = 0;

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
        camera = new OrthographicCamera(viewableWorldWidth, viewableWorldWidth * aspectRatio);
        camera.position.set(playerPosition.x, playerPosition.y, 1.0f);

        hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.position.set(hudCamera.viewportWidth / 2.0f, hudCamera.viewportHeight / 2.0f, 1.0f);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        font = new BitmapFont(Gdx.files.internal("default.fnt"));

        screeninfo = new ScreenInfo(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
                Gdx.graphics.getDisplayMode().refreshRate);
        System.out.println(screeninfo); // DEBUG

        // CREATE MAP THEN CREATE ENTITIES FOR MAP

        map = new Map(); // store all sprites entities
        new Building(map, 50, 100, 0, 150, 150, 0);


        new Graphic(map, 0, 150, 0, 1, "");
        setSpriteCenter(map.getSpriteByID(1));
    }

    // boched attempt at keeping 16/9 resizing window
    @Override
	public void resize(int width, int height) {
        int tempheight = Gdx.graphics.getHeight();
        Gdx.graphics.setWindowedMode((int)(tempheight * 1.778), height);

		camera.setToOrtho(false, width, height);
	}

    @Override
    public void render() {
        // inputs

        // Fullscreen Toggle
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


        // mouse pos
        mouseX = Gdx.input.getX();
        mouseY = Gdx.input.getY();

        ///debug
        System.err.println("Mouse pos:" + mouseX + " " + mouseY);

        //BG colour and set background
        ScreenUtils.clear((float)Math.sin(timeElapsed), (float)Math.sin(timeElapsed + 15), (float)Math.sin(timeElapsed + 30), 1);
		camera.update();
        batch.setProjectionMatrix(camera.combined);

        int tileSize = 20;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < screeninfo.height; y = y + tileSize) {
            for (int x = 0; x < screeninfo.width; x = x + tileSize) {
                if (x % (2 * tileSize) != y % (2 * tileSize)) {
                    shapeRenderer.setColor(Color.BLUE);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                }
            }
        }
        shapeRenderer.end();   

        framesElapsed++;
        timeElapsed = timeElapsed + Gdx.graphics.getDeltaTime();
        timeRemaining = timeAllowed - timeElapsed;
        // delta time is the amount of time since the last frame AKA games use this
        // value to keep movement at the same speed no matter the framerate

        // DRAWING ORDER -> bottom layer -> top layer ( text is at the end as we want to
        // draw it ontop of the sprites )
        batch.begin();

        // draw sprites first
        for (Sprite sprite : new ArrayList<>(map.getEntities())) {
            sprite.draw(batch, Gdx.graphics.getDeltaTime());
        }

        renderDebugText();
        if (gameState == State.TITLE) {
            renderTitle();
        }

        batch.end();
    }
    
    // should probably rewrite this entire section
    private void setSpriteCenterX(Sprite tempSprite) {
        tempSprite.rectangle.x = getSpriteCenterX(tempSprite);
    }

    private void setSpriteCenterY(Sprite tempSprite) {
        tempSprite.rectangle.y = getSpriteCenterY(tempSprite);
    }

    private void setSpriteCenter(Sprite tempSprite) {
        tempSprite.setPos(getSpriteCenterX(tempSprite), getSpriteCenterY(tempSprite));
    }

    private float getSpriteCenterX(Sprite tempSprite) {
        return (Gdx.graphics.getWidth() - tempSprite.getWidth()) / 2.0f;
    }

    private float getSpriteCenterY(Sprite tempSprite) {
        return (Gdx.graphics.getHeight() - tempSprite.getHeight()) / 2.0f;
    }

    private void renderDebugText() {
        font.getData().setScale(1);
        font.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 0, font.getLineHeight());

        font.draw(batch, "FRAMES ELAPSED: " + framesElapsed, 0, 150);
        font.draw(batch, "TIME ELAPSED: " + timeElapsed, 0, 180);

        font.draw(batch, "TIME REMAINING: " + timeRemaining, 0, 210);

        font.draw(batch, "GAME STATE: " + gameState, 0, 300);

        font.getData().setScale(2);
        font.draw(batch, "UNISIM", 200, 500);
    }

    private void renderTitle() {
        font.draw(batch, "TIME REMAINING: " + timeRemaining, 0, 210);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}