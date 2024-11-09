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
import com.badlogic.gdx.graphics.Texture.TextureFilter;
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
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

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

    private SpriteBatch batch;

    // fonts
    FreeTypeFontGenerator generator;
    FreeTypeFontParameter parameter;
    private BitmapFont currentFont;
    private final String FONT_PATH = "fonts/segoeui.ttf"; // Place your TTF font in assets/fonts/

    ScreenInfo screeninfo; // debug

    // game state
    enum State {
        TITLE,
        GAMEPLAY,
        PAUSED,
        GAMEOVER
    }

    State gameState = State.TITLE;

    // timer
    int framesElapsed = 0;
    float timeElapsed = 0;
    float timeRemaining = 0;
    String timeRemainingReadable = "";
    private final float timeAllowed = 300;

    // mouse position
    int mouseX = 0;
    int mouseY = 0;

    // menu options
    String[] menuOptions = {
            "Start Game",
            "Instructions",
            "Options (idk if keep)",
            "Quit Game"
    };
    int menuSelection = 0;
    int maxMenuOptions = 4;

    protected Map map;
    protected Render render;

    // tile scrolling effect
    int tileSize = 50;
    float scrollSpeed = 50f; // Speed of scrolling in pixels per second
    float offset = 0f; // Horizontal offset for scrolling

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
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
        viewport = new FitViewport(1280, 720, camera);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        setUpFontGenerator();
        createFont(20, Color.WHITE, 1, Color.BLACK); // first font stored
        // font = new BitmapFont(Gdx.files.internal("default.fnt"));

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
    }

    private void createTitleAssets() {
        centerSpriteX(new Graphic(render, 0, 350, 0f, 1f, 1, "graphics/unisimlogo.png")); // create + center
    }

    private void setUpFontGenerator() {
        // Generate font from TTF file
        generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));
        parameter = new FreeTypeFontParameter();
    }

    private void createFont(int fontsize, Color color, int borderwidth, Color bordercolor) {
        // Set font size
        parameter.size = fontsize;

        // Set color if needed
        parameter.color = color;

        // Optionally set border or shadow
        parameter.borderWidth = borderwidth;
        parameter.borderColor = bordercolor;

        parameter.genMipMaps = true;
        parameter.minFilter = TextureFilter.Linear;
        parameter.magFilter = TextureFilter.Linear;

        // Generate the font
        currentFont = generator.generateFont(parameter); // BitmapFont from TTF

        // Dispose generator to free up resources
        generator.dispose();
    }

    // boched attempt at keeping 16/9 resizing window
    @Override
    public void resize(int width, int height) {

        viewport.update(width, height, true); // The 'true' flag ensures the camera updates

        // int tempheight = Gdx.graphics.getHeight();
        // Gdx.graphics.setWindowedMode((int)(tempheight * 1.778), height);

        // camera.setToOrtho(false, width, height);
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
                // title screen menu selection
                if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W))
                    menuSelection -= 1;
                if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S))
                    menuSelection += 1;
                menuSelection = (menuSelection + maxMenuOptions) % maxMenuOptions;

                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    // START GAME
                    gameState = State.GAMEPLAY;

                    destroySpritesByIDs(new int[] { 1 }); // remove title sprites
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
        int minutes = (int) seconds / 60;
        int tmpSeconds = (int) seconds % 60;

        return String.format("%d:%02d", minutes, tmpSeconds);
    }

    private void draw() {
        // BG colour and set background
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        if (gameState == State.GAMEPLAY) {
            drawMap();
        } else {
            // Update the offset based on time and speed
            offset += scrollSpeed * Gdx.graphics.getDeltaTime(); // Moves right

            // Keep the offset within the tile size range to avoid large numbers
            offset %= tileSize;

            // Start drawing the tiles
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            // Adjust the starting points of x and y based on the offset
            for (int y = -tileSize + (int) offset; y < screeninfo.height + tileSize; y += tileSize) {
                for (int x = -tileSize + (int) offset; x < screeninfo.width + tileSize; x += tileSize) {
                    // pattern
                    if (((x + tileSize) / tileSize + (y + tileSize) / tileSize) % 2 == 0) {
                        shapeRenderer.setColor(Color.BLACK);
                        shapeRenderer.rect(x, y, tileSize, tileSize);
                    } else {
                        shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 1f);
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

    private Color getColourFromUID(int uid) {
        return switch (uid) {
            case 0 -> Color.PURPLE;
            case 1 -> Color.GREEN;
            case 2 -> Color.BLUE;
            case 3 -> Color.RED;
            default -> Color.BROWN;
        };
    }

    // Centers a sprite based on the screen dimensions
    private void centerSprite(Sprite sprite) {
        float centerX = (Gdx.graphics.getWidth() - sprite.getWidth()) / 2.0f;
        float centerY = (Gdx.graphics.getHeight() - sprite.getHeight()) / 2.0f;
        sprite.setPos(centerX, centerY);
    }

    // Overload for setting either X or Y if necessary
    private void centerSpriteX(Sprite sprite) {
        float centerX = (Gdx.graphics.getWidth() - sprite.getWidth()) / 2.0f;
        sprite.setPos(centerX, sprite.rectangle.y); // keep current Y
    }

    private void centerSpriteY(Sprite sprite) {
        float centerY = (Gdx.graphics.getHeight() - sprite.getHeight()) / 2.0f;
        sprite.setPos(sprite.rectangle.x, centerY); // keep current X
    }

    public void destroySpritesByIDs(int[] spriteIDs) {
        for (int id : spriteIDs) {
            render.removeSpriteByID(id);
        }
    }

    private void renderTitle() {
        String menuText = "";
        for (int i = 0; i < menuOptions.length; i++) {
            menuText = menuOptions[i];
            System.err.println(menuText);
            if (i == menuSelection) {
                menuText = "> " + menuText;
            }
            currentFont.draw(batch, menuText, 500, (250 - i * 20));
        }

        currentFont.draw(batch, "Press ENTER to start", 500, 100);
    }

    private void renderPauseScreen() {
        currentFont.draw(batch, "PAUSED", 640, 360);
    }

    private void renderDebugText() {
        currentFont.draw(batch, "GAME STATE: " + gameState, 0, 700);
        currentFont.draw(batch, "FRAMES ELAPSED: " + framesElapsed, 0, 670);
        currentFont.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 0, 640);
        // font.draw(batch, "TIME ELAPSED: " + timeElapsed, 0, 180);

        currentFont.draw(batch, "Mouse pos: " + mouseX + ", " + mouseY, 0, 600);
        currentFont.draw(batch, "menuSelection: " + menuSelection, 0, 580);

        currentFont.draw(batch, "current spriteIDs list: " + render.setOfIDs.toString(), 0, 500);

        currentFont.draw(batch, "TIME REMAINING: " + timeRemainingReadable, 0, currentFont.getLineHeight());
    }

    @Override
    public void render() {
        inputs();
        logic();
        draw();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}