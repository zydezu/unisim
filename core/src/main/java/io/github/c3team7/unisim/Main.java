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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.List;
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
    private BitmapFont normalFont;
    private BitmapFont boldFont;

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
    float globalTimeElapsed = 0;
    float timeElapsed = 0;
    float timeRemaining = 0;
    String timeRemainingReadable = "";
    private final float timeAllowed = 300;

    // mouse position
    int mouseX = 0;
    int mouseY = 0;

    // menu options
    String[] menuOptions = { // should probably be moved to a .txt file
            "Start Game",
            "Instructions",
            "Options (idk if keep)",
            "Exit to Desktop"
    };
    String[] pauseOptions = { // should probably be moved to a .txt file
            "Continue",
            "Restart",
            "Quit Game"
    };
    ArrayList<Rectangle> optionRects = new ArrayList<>();
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
        batch.enableBlending();

        normalFont = createFont("fonts/segoeui.ttf", 20, Color.WHITE, 1, Color.BLACK);
        boldFont = createFont("fonts/Montserrat-Bold.ttf", 32, Color.WHITE, 1, Color.BLACK);

        screeninfo = new ScreenInfo(1280, 720, Gdx.graphics.getDisplayMode().refreshRate);

        initGame();
    }

    private void initGame() {
        gameState = State.TITLE;
        menuSelection = 0;
        maxMenuOptions = 4;

        timeElapsed = 0;

        // CREATE MAP THEN CREATE ENTITIES FOR MAP
        map = new Map("map.txt"); // generate the map
        map.placeBuilding(map.getIndexFromTileCoords(3, 4), 2, 3);
        map.placeBuilding(0, 1, 1);
        map.placeBuilding(2303, 1, 1);
        render = new Render();

        // store all sprites entities
        createTitleAssets();

        // get rects for each menu option to select with mouse
        optionRects.clear();
        GlyphLayout layout = new GlyphLayout();
        for (int i = 0; i < menuOptions.length; i++) {
            layout.setText(boldFont, menuOptions[i]);

            System.err.println(500 + " " + (225 - i * 40) + " to > " + (500 + layout.width) + " "
                    + ((225 - i * 40) + layout.height));

            optionRects.add(new Rectangle(500, (225 - i * 40), layout.width, layout.height * 2));
        }
        // new Building(map, 50, 100, 0, 150, 150, 0);
    }

    private void setUpFontGenerator(String font) {
        // Generate font from TTF file
        generator = new FreeTypeFontGenerator(Gdx.files.internal(font));
        parameter = new FreeTypeFontParameter();
    }

    private BitmapFont createFont(String font, int fontsize, Color color, int borderwidth, Color bordercolor) {
        setUpFontGenerator(font);

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

        BitmapFont generatedFont = generator.generateFont(parameter); // BitmapFont from TTF

        generator.dispose(); // prevent memory leaks

        return generatedFont;
    }

    private void createTitleAssets() {
        centerSpriteX(new Graphic(render, 0, 365, 0f, 1f, 1, "graphics/unisimlogo.png")); // create + center
    }

    private void createPauseAssets() {
        centerSpriteX(new Graphic(render, 0, 500, 0f, 1f, 20, "graphics/unisimlogopixel.png")); // create + center
    }

    private void inputs() {
        // mouse pos
        mouseX = Gdx.input.getX();
        mouseY = screeninfo.height - Gdx.input.getY(); // match sprite and text pos

        // Fullscreen Toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            map.placeBuilding(map.getIndexFromTileCoords(3, 4), 20, 30);

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
                menuSelectionInputs();
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    selectMenuOption(menuSelection);
                }

                for (int i = 0; i < optionRects.size(); i++) {
                    if (optionRects.get(i).contains(mouseX, mouseY + 30)) {
                        menuSelection = i;
                        break;
                    }
                }

                if (Gdx.input.justTouched()) {
                    System.err.println(mouseX + ", " + mouseY);

                    // FIXME: fix this
                    for (int i = 0; i < optionRects.size(); i++) {
                        if (optionRects.get(i).contains(mouseX, mouseY + 30)) {
                            menuSelection = i;
                            System.err.println("SELECT" + menuSelection);
                            selectMenuOption(menuSelection);
                            break;
                        }
                    }
                }
                break;
            case PAUSED:
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                    unpauseGame();
                }
                menuSelectionInputs();
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    switch (menuSelection) {
                        case 0:
                            unpauseGame();
                            break;
                        case 1:
                            restartGame();
                            // TODO: add a way to restart the game
                            break;
                        case 2:
                            exitToMainMenu();
                            break;
                        default:
                            break;
                    }
                }
                break;
            case GAMEPLAY:
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                    pauseGame();
                }
                break;
            default:
                break;
        }
    }

    private void menuSelectionInputs() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W))
            menuSelection -= 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S))
            menuSelection += 1;
        menuSelection = (menuSelection + maxMenuOptions) % maxMenuOptions;
    }

    private void selectMenuOption(int menuSelection) {
        switch (menuSelection) {
            case 0:
                startGame();
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                Gdx.app.exit();
                System.exit(-1);
                break;
            default:
                break;
        }
    }

    private void startGame() {
        timeElapsed = 0; // reset on repeat playthroughs
        gameState = State.GAMEPLAY;
        destroySpritesByIDs(new int[] { 1 }); // remove title sprites
    }

    private void pauseGame() {
        gameState = State.PAUSED;
        menuSelection = 0;
        maxMenuOptions = 3;
        createPauseAssets();
    }

    private void unpauseGame() {
        gameState = State.GAMEPLAY;
        destroySpritesByIDs(new int[] { 20 }); // remove pause sprites
    }

    private void restartGame() {
        destroySpritesByIDs(new int[] { 20 }); // remove pause sprites
        initGame();
        startGame();
    }

    private void exitToMainMenu() {
        // we want to reset all relevant variables to the defaults here
        destroySpritesByIDs(new int[] { 20 }); // remove pause sprites
        initGame();
    }

    private void logic() {
        framesElapsed++;
        globalTimeElapsed += Gdx.graphics.getDeltaTime();
        if (gameState == State.TITLE) {

        }
        if (gameState == State.GAMEPLAY) {
            timeElapsed += Gdx.graphics.getDeltaTime();
            timeRemaining = (float) Math.ceil(timeAllowed - timeElapsed);
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

        // DRAWING ORDER -> bottom layer -> top layer ( text is at the end to stay ontop
        // )
        switch (gameState) {
            case TITLE:
                drawCheckerPattern();
                break;
            case GAMEPLAY:
                drawMap();
                drawClockIcon(1170, 690, 12, ((timeElapsed % 60) * -6) + 90);
                break;
            case PAUSED:
                drawCheckerPattern();
                break;
            default:
                break;
        }

        batch.begin();

        // draw sprites first
        for (Sprite sprite : new ArrayList<>(render.getEntities())) {
            sprite.draw(batch, Gdx.graphics.getDeltaTime());
        }

        // text must be rendered as part of the batch
        switch (gameState) {
            case TITLE:
                renderTitle();
                break;
            case GAMEPLAY:
                renderGameText();
                break;
            case PAUSED:
                renderPauseScreen();
                break;
            default:
                break;
        }
        renderDebugText();

        batch.end();
    }

    private void drawCheckerPattern() {
        offset += scrollSpeed * Gdx.graphics.getDeltaTime(); // Moves right
        offset %= tileSize;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // adjust x and y based on offset
        for (int y = -tileSize + (int) offset; y < screeninfo.height + tileSize; y += tileSize) {
            for (int x = -tileSize + (int) offset; x < screeninfo.width + tileSize; x += tileSize) {
                // pattern
                if (((x + tileSize) / tileSize + (y + tileSize) / tileSize) % 2 == 0) {
                    shapeRenderer.setColor(gameState == State.PAUSED ? Color.PURPLE : Color.BLACK);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                } else {
                    shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 1f);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                }
            }
        }
        shapeRenderer.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        for (int i = 0; i < optionRects.size(); i++) {
            shapeRenderer.rect(550, (200 - i * 40), 150, 38);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void drawClockIcon(float x, float y, float radius, float degrees) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(x, y, radius);
        shapeRenderer.end();

        // end point of the clock hand
        float handX = x + radius * 0.7f * (float) Math.cos(Math.toRadians(degrees));
        float handY = y + radius * 0.7f * (float) Math.sin(Math.toRadians(degrees));

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rectLine(x, y, handX, handY, 2);
        shapeRenderer.end();
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
        float centerX = (screeninfo.width - sprite.getWidth()) / 2.0f;
        float centerY = (screeninfo.height - sprite.getHeight()) / 2.0f;
        sprite.setPos(centerX, centerY);
    }

    // Overload for setting either X or Y if necessary
    private void centerSpriteX(Sprite sprite) {
        float centerX = (screeninfo.width - sprite.getWidth()) / 2.0f;
        sprite.setPos(centerX, sprite.rectangle.y); // keep current Y
    }

    private void centerSpriteY(Sprite sprite) {
        float centerY = (screeninfo.height - sprite.getHeight()) / 2.0f;
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
            if (i == menuSelection) {
                menuText = "> " + menuText;
            }
            boldFont.draw(batch, menuText, 550, (225 - i * 40));
        }

        drawRotatedText(boldFont, batch, "By windows007", 655, 380, 9);

        // 640 is 1/2 of 1280 (center of screen)
        drawCenteredText(normalFont, batch, "Use UP and DOWN to select an option", 640, 50);
        drawCenteredText(normalFont, batch, "Press ENTER to select an option", 640, 30);
    }

    private void renderPauseScreen() {
        String menuText = "";
        for (int i = 0; i < pauseOptions.length; i++) {
            menuText = pauseOptions[i];
            if (i == menuSelection) {
                menuText = "> " + menuText;
            }
            boldFont.draw(batch, menuText, 500, (250 - i * 30));
        }

        boldFont.draw(batch, "PAUSED", 640, 360);

        renderTime();

        drawCenteredText(normalFont, batch, "Use UP and DOWN to select an option", 640, 50);
        drawCenteredText(normalFont, batch, "Press ENTER to select an option", 640, 30);
    }

    private void renderGameText() {
        renderTime();
    }

    private void renderTime() {
        drawRightAlignedText(boldFont, batch, timeRemainingReadable, 1270, 700);
        drawRightAlignedText(normalFont, batch, "Time remaining", 1270, 670);
    }

    private void renderDebugText() {
        normalFont.draw(batch, "GAME STATE: " + gameState, 0, 700);
        normalFont.draw(batch, "FRAMES ELAPSED: " + framesElapsed, 0, 670);
        normalFont.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 0, 640);
        // font.draw(batch, "TIME ELAPSED: " + timeElapsed, 0, 180);

        normalFont.draw(batch, "Mouse pos: " + mouseX + ", " + mouseY, 0, 600);
        normalFont.draw(batch, "menuSelection: " + menuSelection, 0, 580);

        normalFont.draw(batch, "current spriteIDs list: " + render.setOfIDs.toString(), 0, 500);
    }

    public void drawCenteredText(BitmapFont font, SpriteBatch batch, String text, float x, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        float adjustedX = x - layout.width / 2;
        // float adjustedY = y + layout.height / 2;
        font.draw(batch, layout, adjustedX, y);
    }

    public void drawRightAlignedText(BitmapFont font, SpriteBatch batch, String text, float x, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        float adjustedX = x - layout.width;
        font.draw(batch, layout, adjustedX, y);
    }

    public void drawRotatedText(BitmapFont font, SpriteBatch batch, String text, float x, float y, float angleDegrees) {
        batch.end();

        // set affine transformation to rotate text
        Affine2 transform = new Affine2();
        transform.translate(x, y); // Move to the rotation origin
        transform.rotate(angleDegrees); // Apply rotation
        transform.translate(-x, -y); // Move back to original position

        // transform.shear(0, (float)Math.sin(globalTimeElapsed) * 0.008f);
        // apply transformation matrix to batch
        batch.setTransformMatrix(batch.getTransformMatrix().setAsAffine(transform));

        batch.begin();
        font.draw(batch, text, x, y); // draw text with rotation

        // Reset the transformation matrix to avoid affecting other drawings
        batch.end();
        batch.setTransformMatrix(batch.getTransformMatrix().idt());
        batch.begin();
    }

    @Override
    public void render() {
        inputs();
        logic();
        draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // The 'true' flag ensures the camera updates
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        normalFont.dispose();
        boldFont.dispose();
    }
}