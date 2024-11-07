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
// bye

public class Main extends Game {
    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private Viewport viewport;

    private ShapeRenderer shapeRenderer;

    // private Vector2 playerPosition = new Vector2();
    // private boolean cameraFollowsPlayer = true;

    private SpriteBatch batch;
    private BitmapFont font;

    ScreenInfo screeninfo; // debug

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
    String timeRemainingReadable = "";
    private final float timeAllowed = 300;

    //mouse position
    int mouseX = 0;
    int mouseY = 0;

    protected Map map;
    protected Render render;

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
        // setting up camera
        float aspectRatio = (float) Gdx.graphics.getHeight() / (float) Gdx.graphics.getWidth();
        float viewableWorldWidth = 32.0f;
        camera = new OrthographicCamera(viewableWorldWidth, viewableWorldWidth * aspectRatio);
        // camera.position.set(playerPosition.x, playerPosition.y, 1.0f);

        // hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // hudCamera.position.set(hudCamera.viewportWidth / 2.0f, hudCamera.viewportHeight / 2.0f, 1.0f);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        font = new BitmapFont(Gdx.files.internal("default.fnt"));

        screeninfo = new ScreenInfo(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
                Gdx.graphics.getDisplayMode().refreshRate);
        System.out.println(screeninfo); // DEBUG

        // CREATE MAP THEN CREATE ENTITIES FOR MAP

        // FIXME: these values need changing, i put in previous values

        map = new Map("map.txt"); // generate the map 
        map.placeBuilding(map.getIndexFromTileCoords(3, 4), 2, 3);
        map.placeBuilding(0, 1, 1);
        map.placeBuilding(2303, 1, 1);
        render = new Render();   
        
        // store all sprites entities
        createTitleAssets();

        // new Building(map, 50, 100, 0, 150, 150, 0);
        viewport = new FitViewport(1280, 720, camera);
    }

    private void createTitleAssets() {
        setSpriteCenterX(new Graphic(render, 0, 400, 0, 1, "")); // create + center same line
    }

    // boched attempt at keeping 16/9 resizing window
    @Override
	public void resize(int width, int height) {

        viewport.update(width, height, true); // The 'true' flag ensures the camera updates

        // int tempheight = Gdx.graphics.getHeight();
        // Gdx.graphics.setWindowedMode((int)(tempheight * 1.778), height);


		// camera.setToOrtho(false, width, height);
	}

    private void draw() {
        // BG colour and set background
        ScreenUtils.clear(0, 0, 0, 0);
		camera.update();
        batch.setProjectionMatrix(camera.combined);

        if (gameState == State.GAMEPLAY) {
            drawMap();
        } else {
            int tileSize = 40;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (int y = 0; y < screeninfo.height; y = y + tileSize) {
                for (int x = 0; x < screeninfo.width; x = x + tileSize) {
                    if (x % (2 * tileSize) != y % (2 * tileSize)) {
                        shapeRenderer.setColor(Color.RED);
                        shapeRenderer.rect(x, y, tileSize, tileSize);
                    }
                }
            }
            shapeRenderer.end();
        }

        // DRAWING ORDER -> bottom layer -> top layer ( text is at the end as we want to
        // draw it ontop of the sprites )
        batch.begin();

        // draw sprites first
        for (Sprite sprite : new ArrayList<>(render.getEntities())) {
            sprite.draw(batch, Gdx.graphics.getDeltaTime());
        }

        renderDebugText();
        if (gameState == State.TITLE) {
            renderTitle();
        }
        if (gameState == State.PAUSED) {
            renderPauseScreen();
        }

        batch.end();
    }

    private void drawMap() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < map.HEIGHT; y = y + 1) {
            for (int x = 0; x < map.WIDTH; x = x + 1) {
                shapeRenderer.setColor(getColourFromUID(map.getFromTileCoords(x, y)));
                shapeRenderer.rect(x * map.TILE_SIZE, y * map.TILE_SIZE, map.TILE_SIZE, map.TILE_SIZE);
            }
        }
        shapeRenderer.end(); 
    }

    private Color getColourFromUID(int uid){
        return switch (uid) {
            case 0 -> Color.PURPLE;
            case 1 -> Color.GREEN;
            case 2 -> Color.BLUE;
            case 3 -> Color.RED;
            default -> Color.BROWN;
        };
    }

    private void logic() {
        framesElapsed++;
        if (gameState == State.GAMEPLAY) {
            timeElapsed = timeElapsed + Gdx.graphics.getDeltaTime();
            timeRemaining = timeAllowed - timeElapsed;
            timeRemainingReadable = convertTimeToReadable(timeRemaining);    
        }
    }

    private String convertTimeToReadable(float seconds) {
        int minutes = (int)seconds / 60;
        int tmpSeconds = (int)seconds % 60;

        return String.format("%d:%02d", minutes, tmpSeconds);
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

    public void destroySpritesByIDs(int[] spriteIDs) {
        for (int id : spriteIDs) {
            render.getSpriteByID(id).destroy();
        }
    }

    private void renderDebugText() {
        font.getData().setScale(1);
        font.draw(batch, "GAME STATE: " + gameState, 0, 700);
        font.draw(batch, "FRAMES ELAPSED: " + framesElapsed, 0, 670);
        font.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 0, 640);
        // font.draw(batch, "TIME ELAPSED: " + timeElapsed, 0, 180);

        font.draw(batch, "TIME REMAINING: " + timeRemainingReadable, 0, font.getLineHeight());
    }

    private void renderTitle() {
        font.getData().setScale(2);
        font.draw(batch, "START GAME", 500, 200);
        font.draw(batch, "Press ENTER to start", 500, 150);
    }

    private void renderPauseScreen() {
        font.getData().setScale(3);
        font.draw(batch, "PAUSED", 640, 360);
    }

    @Override
    public void render() {
        // inputs

        inputs();
        logic();
        draw();
    }

    private void inputs() {
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

        // Manage player inputs here
        switch (gameState) {
            case TITLE:
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    // START GAME
                    gameState = State.GAMEPLAY;

                    destroySpritesByIDs(new int[] {1}); // remove title sprites
                }
                break;
            case PAUSED: 
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                    gameState = State.GAMEPLAY;
                }
                break;
            case GAMEPLAY:
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                    gameState = State.PAUSED;
                }
                break;
            default:
                break;
        }

        // mouse pos
        mouseX = Gdx.input.getX();
        mouseY = Gdx.input.getY();

        // TODO: debug
        System.err.println("Mouse pos:" + mouseX + " " + mouseY);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}