package io.github.c3team7.unisim;

import java.util.ArrayList;
import java.util.List;

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
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.c3team7.unisim.Building.Building;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

// TO DO
//
// Research AssetManager() - https://libgdx.com/wiki/managing-your-assets
// HAI ! ^_^
// bye

public class Main extends Game {
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;

    // fonts
    private FreeTypeFontGenerator generator;
    private FreeTypeFontParameter parameter;
    private BitmapFont normalFont, smallerFont, smallFont, mediumFont, boldFont, extraBoldFont;

    private final int RESOLUTIONX = 1280;
    private final int RESOLUTIONY = 720;
    private final int TILE_SIZE = 20;

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
    private float timeElapsed = 0;
    private float timeRemaining = 0;
    private String timeRemainingReadable = "";
    private final float timeAllowed = 1;

    // mouse position
    float mouseX = 0;
    float mouseY = 0;

    // map, sprites and tileset
    protected Map map;
    protected Render render;
    private Texture tileset;
    private TextureRegion[][] tiles;

    // buildings
    private List<Building> buildings;
    private List<Building> buildingPresets;
    private String[] buildingPresetNames = { // should probably be moved to a .txt file
            "Accomodation\nBuilding #1",
            "Cafeteria\nBuilding #2",
            "Course\nBuilding #3",
            "Recreational\nBuilding #3",
    };
    // ArrayList<String> buildingPresetNames = new ArrayList<>();
    private int selectedBuildingIndex = -1;

    // building menu
    private Boolean buildingMenuOpen = true;
    private boolean showCanPlaceBuilding = false;
    int accomodationcount = 0;
    int cafeteriacount = 0;
    int coursecount = 0;
    int recreationalcount = 0;

    // menu options
    private String[] menuOptions = { // should probably be moved to a .txt file
            "Start Game",
            "Instructions",
            "Options",
            "Exit to Desktop"
    };
    private String[] menuOptionsExplanations = { // should probably be moved to a .txt file
            "Start a new simulation!",
            "View how to play",
            "Enable fullscreen, etc...",
            "Close the game"
    };
    private String[] pauseOptions = { // should probably be moved to a .txt file
            "Continue",
            "Restart",
            "Quit Game"
    };
    private String[] gameOverOptions = { // should probably be moved to a .txt file
            "Restart",
            "Quit Game"
    };
    private ArrayList<Rectangle> optionRects = new ArrayList<>();
    private int menuSelection = 0;
    private int maxMenuOptions = 4;
    private int menuOptionInitx = 510;
    private int menuOptionInity = 225;

    // tile scrolling effect
    private int tileSize = 50;
    private float scrollSpeed = 50f; // Speed of scrolling in pixels per second
    private float offset = 0f; // Horizontal offset for scrolling

    private Boolean showDebugText = false;

    @Override
    public void create() {
        // setting up camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, RESOLUTIONX, RESOLUTIONY);
        viewport = new FitViewport(RESOLUTIONX, RESOLUTIONY, camera);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        batch.enableBlending();

        // setup tiles
        tileset = new Texture(Gdx.files.internal("graphics/tiles/tileset.png"));
        tiles = TextureRegion.split(tileset, 20, 20);

        // setup fonts
        normalFont = createFont("fonts/Montserrat-Regular.ttf", 20, Color.WHITE, 1, Color.BLACK);

        smallerFont = createFont("fonts/Montserrat-Regular.ttf", 15, Color.WHITE, 1, Color.BLACK);
        smallFont = createFont("fonts/Montserrat-Medium.ttf", 18, Color.WHITE, 1, Color.BLACK);
        mediumFont = createFont("fonts/Montserrat-Medium.ttf", 32, Color.WHITE, 1, Color.BLACK);
        boldFont = createFont("fonts/Montserrat-Bold.ttf", 32, Color.WHITE, 1, Color.BLACK);
        extraBoldFont = createFont("fonts/Montserrat-Black.ttf", 48, Color.WHITE, 1, Color.BLACK);

        initGame();
    }

    private void initGame() {
        gameState = State.TITLE;
        menuSelection = 0;
        maxMenuOptions = 4;

        timeElapsed = 0;

        // CREATE MAP THEN CREATE ENTITIES FOR MAP
        map = new Map("map.txt"); // generate the map

        // init lists
        buildings = new ArrayList<>(10);
        buildingPresets = new ArrayList<>();

        // add building to presets
        Building building = new Building(4, 8);
        building.setAccommodationBuilding();
        buildingPresets.add(building);

        building = new Building(10, 8);
        building.setCafeteriaBuilding();
        buildingPresets.add(building);

        building = new Building(5, 5);
        building.setCourseBuilding();
        buildingPresets.add(building);

        building = new Building(2, 4);
        building.setRecreationalBuilding();
        buildingPresets.add(building);

        // buildings stuff
        selectedBuildingIndex = -1;
        buildingMenuOpen = true;
        showCanPlaceBuilding = false;

        // actual rendering
        render = new Render();

        // store all sprites entities
        createTitleAssets();

        // create tick and cross to be used in game
        new Graphic(render, -50, -50, 0f, 1f, 101,
                "graphics/mouse/tick.png");
        new Graphic(render, -50, -50, 0f, 1f, 102,
                "graphics/mouse/cross.png");

        // create building icons
        new Graphic(render, -1000, -1000, 0f, 1f, 110,
                "graphics/buildings/test.jpg");
        new Graphic(render, -1000, -1000, 0f, 1f, 111,
                "graphics/buildings/test.jpg");
        new Graphic(render, -1000, -1000, 0f, 1f, 112,
                "graphics/buildings/test.jpg");
        new Graphic(render, -1000, -1000, 0f, 1f, 113,
                "graphics/buildings/test.jpg");

        // get rects for each menu option to select with mouse
        optionRects.clear();
        GlyphLayout layout = new GlyphLayout();
        for (int i = 0; i < menuOptions.length; i++) {
            layout.setText(boldFont, menuOptions[i]);

            optionRects.add(new Rectangle(menuOptionInitx - 15, (menuOptionInity - 32 - i * 40), 300, 32));
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
        mouseY = Gdx.input.getY(); // match sprite and text pos
        mouseX = mouseX * (RESOLUTIONX / (float) Gdx.graphics.getWidth());
        mouseY = mouseY * (RESOLUTIONY / (float) Gdx.graphics.getHeight());
        mouseY = RESOLUTIONY - mouseY;

        // Fullscreen Toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen();
        }

        // TODO: remove debug toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2))
            showDebugText = !showDebugText;
        // Manage player inputs here
        switch (gameState) {
            case TITLE:
                // title screen menu selection
                menuSelectionInputs();
                break;
            case PAUSED:
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                    unpauseGame();
                }
                menuSelectionInputs();
                break;
            case GAMEPLAY:
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                    pauseGame();
                }

                if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                    buildingMenuOpen = true;
                    selectedBuildingIndex = -1;
                }

                if (selectedBuildingIndex != -1) {
                    Building building = buildingPresets.get(selectedBuildingIndex);
                    int[] mouseTileCoords = convertMouseCoordsToTileCoords(mouseX, mouseY);
                    int mouseTileX = mouseTileCoords[0];
                    int mouseTileY = mouseTileCoords[1];

                    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                        boolean canPlaceBuilding = map.canPlaceBuilding(
                                map.getIndexFromTileCoords(mouseTileX, mouseTileY), building.getWidth(),
                                building.getHeight());
                        if (canPlaceBuilding) {
                            placeBuilding(building, mouseTileCoords[0], mouseTileCoords[1]);
                            buildingMenuOpen = true;
                            selectedBuildingIndex = -1;
                        } else {
                            showCanPlaceBuilding = true;
                            Timer timer = new Timer();
                            timer.scheduleTask(new Timer.Task() {
                                @Override
                                public void run() {
                                    showCanPlaceBuilding = false;
                                }
                            }, 1f);
                        }

                    }
                } else {
                    if (buildingMenuOpen) {
                        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                            if (mouseY <= 150) {
                                if (mouseX < buildingPresets.size() * 150) {
                                    selectedBuildingIndex = (int) Math.floor(mouseX / 150);
                                }
                            }
                        }
                    }
                }

                break;
            case GAMEOVER:
                menuSelectionInputs();
            default:
                break;
        }
    }

    private int[] convertMouseCoordsToTileCoords(float mouseX, float mouseY) {
        return new int[] { (int) mouseX / map.TILE_SIZE, (int) mouseY / map.TILE_SIZE };
    }

    private void toggleFullscreen() {
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

    private void menuSelectionInputs() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W))
            menuSelection -= 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S))
            menuSelection += 1;
        menuSelection = (menuSelection + maxMenuOptions) % maxMenuOptions;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            selectMenuOption(menuSelection);
        }

        // mouse
        for (int i = 0; i < optionRects.size(); i++) {
            if (optionRects.get(i).contains(mouseX, mouseY)) {
                menuSelection = i;
                break;
            }
        }
        if (Gdx.input.justTouched()) {
            for (int i = 0; i < optionRects.size(); i++) {
                if (optionRects.get(i).contains(mouseX, mouseY)) {
                    menuSelection = i;
                    selectMenuOption(menuSelection);
                    break;
                }
            }
        }
    }

    private void selectMenuOption(int menuSelection) {
        if (gameState == State.TITLE) {
            switch (menuSelection) {
                case 0:
                    startGame();
                    break;
                case 1:
                    break;
                case 2:
                    toggleFullscreen();
                    break;
                case 3:
                    Gdx.app.exit();
                    System.exit(-1);
                    break;
                default:
                    break;
            }
        } else if (gameState == State.PAUSED) {
            switch (menuSelection) {
                case 0:
                    unpauseGame();
                    break;
                case 1:
                    restartGame();
                    break;
                case 2:
                    exitToMainMenu();
                    break;
                default:
                    break;
            }
        } else if (gameState == State.GAMEOVER) {
            switch (menuSelection) {
                case 0:
                    restartGame();
                    break;
                case 1:
                    exitToMainMenu();
                    break;
                default:
                    break;
            }
        }
    }

    private void startGame() {
        timeElapsed = 0; // reset on repeat playthroughs
        gameState = State.GAMEPLAY;
        destroySpritesByIDs(new int[] { 1 }); // remove title sprites
    }

    private void pauseGame() {
        optionRects.clear();
        GlyphLayout layout = new GlyphLayout();
        for (int i = 0; i < pauseOptions.length; i++) {
            layout.setText(boldFont, pauseOptions[i]);

            optionRects.add(new Rectangle(menuOptionInitx - 15, (menuOptionInity - 32 - i * 40), 300, 32));
        }

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

    private void gameOverMenuOptions() {
        optionRects.clear();
        GlyphLayout layout = new GlyphLayout();
        for (int i = 0; i < gameOverOptions.length; i++) {
            layout.setText(boldFont, gameOverOptions[i]);

            optionRects.add(new Rectangle(menuOptionInitx - 15, (menuOptionInity - 32 - i * 40), 300, 32));
        }
        menuSelection = 0;
        maxMenuOptions = 2;
    }

    private void logic() {
        framesElapsed++;
        globalTimeElapsed += Gdx.graphics.getDeltaTime();
        if (gameState == State.GAMEPLAY) {
            timeElapsed += Gdx.graphics.getDeltaTime();
            timeRemaining = (float) Math.ceil(timeAllowed - timeElapsed);
            if (timeRemaining < 0) {
                timeRemainingReadable = "0:00";
                gameState = State.GAMEOVER;
                gameOverMenuOptions();
            } else {
                timeRemainingReadable = convertTimeToReadable(timeRemaining);
            }
            buildingMenuOpen = (selectedBuildingIndex == -1); // hide menu to place building
        }
    }

    private boolean placeBuilding(Building building, int x, int y) {
        int index = map.getIndexFromTileCoords(x, y);
        if (!map.placeBuilding(index, building.getWidth(), building.getHeight())) {
            return false;
        }

        Building newBuilding = new Building(building, index);
        buildings.add(newBuilding);
        return true;
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
                drawTransMenuBoxes();
                break;
            case GAMEPLAY:
                // drawMap();
                break;
            case PAUSED:
                drawMap();
                drawCheckerPattern();
                drawTransMenuBoxes();
                drawClockIcon(1255, 688, 12, ((timeElapsed % 1) * 360) + 90);
                break;
            case GAMEOVER:
                break;
            default:
                break;
        }

        batch.begin();

        // text must be rendered as part of the batch
        switch (gameState) {
            case TITLE:
                renderTitle();
                break;
            case GAMEPLAY:
                drawMapTiles();
                batch.end();
                if (buildingMenuOpen) {
                    drawBuildingTransBox();
                } else {
                    // draw button to open ?
                }
                drawClockIcon(1255, 688, 12, ((timeElapsed % 1) * 360) + 90);
                batch.begin();
                renderGameText();
                if (buildingMenuOpen) {
                    renderBuildingSelectionText();
                    hideSpritesByID(new int[] { 101, 102 });
                    // render building icons that go above text
                    for (int i = 0; i < buildingPresetNames.length; i++) {
                        render.getSpriteByID(110 + i).setPos(25 + (i * 150), 45);
                    }
                } else {
                    render.getSpriteByID(101).setPos(mouseX - 15, mouseY - 25);
                    render.getSpriteByID(102).setPos(mouseX + 12, mouseY - 25);

                    hideSpritesByID(new int[] { 110, 111, 112, 113 });
                }

                break;
            case PAUSED:
                // move tick and cross offscreen
                hideGameSprites();
                renderPauseScreen();
                break;
            case GAMEOVER:
                hideGameSprites();
                drawMapTiles();
                batch.end();
                drawTransMenuBoxes();
                drawClockIcon(1255, 688, 12, ((timeElapsed % 1) * 360) + 90);
                batch.begin();
                renderGameOverText();
            default:
                break;
        }
        if (showDebugText) {
            renderDebugText();
        }

        // draw sprites first
        for (Sprite sprite : new ArrayList<>(render.getEntities())) {
            sprite.draw(batch, Gdx.graphics.getDeltaTime());
        }

        batch.end();
    }

    public void hideSpritesByID(int[] spriteIDs) {
        for (int id : spriteIDs) {
            if (render.setOfIDs.contains(id)) {
                render.getSpriteByID(id).moveOffScreen();
            }
        }
    }

    private void hideGameSprites() {
        hideSpritesByID(new int[] { 101, 102, 110, 111, 112, 113 });
    }

    private void drawBuildingTransBox() {
        // needed for transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setColor(0, 0, 0, 0.6f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(0, 0, 1280, 150);

        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);

        for (int i = 0; i < buildingPresetNames.length; i++) {
            shapeRenderer.rect((i * 150), 0, 150, 150);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

    }

    private void drawTransMenuBoxes() {
        // needed for transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (gameState == State.PAUSED || gameState == State.GAMEOVER) {
            shapeRenderer.setColor(0, 0, 0, 0.6f);
            shapeRenderer.rect(0, 0, 1280, 720);
        }

        for (int i = 0; i < optionRects.size(); i++) {
            if (i == menuSelection) {
                shapeRenderer.setColor(1f, 1f, 1f, 0.8f);
            } else {
                shapeRenderer.setColor(0, 0, 0, 0.8f);
            }
            shapeRenderer.rect(menuOptionInitx - 15, (menuOptionInity - 30 - i * 40), 300, 36);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawCheckerPattern() {
        // needed for transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        offset += scrollSpeed * Gdx.graphics.getDeltaTime(); // Moves right
        offset %= tileSize;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // adjust x and y based on offset
        for (int y = -tileSize + (int) offset; y < RESOLUTIONY + tileSize; y += tileSize) {
            for (int x = -tileSize + (int) offset; x < RESOLUTIONX + tileSize; x += tileSize) {
                if (((x + tileSize) / tileSize + (y + tileSize) / tileSize) % 2 == 0) {
                    shapeRenderer.setColor(0f, 0f, 0f, gameState == State.PAUSED ? 0.25f : 1f);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                } else {
                    shapeRenderer.setColor(0.8f, 0.2f, 0.2f, gameState == State.PAUSED ? 0.25f : 1f);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                }
            }
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

    private void drawMapTiles() {
        for (int y = 0; y < map.HEIGHT; y++) {
            for (int x = 0; x < map.WIDTH; x++) {

                batch.draw(getTileFromUID(map.getFromTileCoords(x, y)), x * map.TILE_SIZE, y * map.TILE_SIZE, TILE_SIZE,
                        TILE_SIZE);

                // batch.draw(tiles[0][(int) ((Math.sin(globalTimeElapsed * x*x + y*y) + 1) *
                // 2.5)], x * map.TILE_SIZE, y * map.TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private TextureRegion getTileFromUID(int UID) {
        return switch (UID) {
            case 0 -> tiles[0][4]; // unknown texture
            case 1 -> tiles[0][0]; // grass
            case 2 -> tiles[0][1]; // water
            case 3 -> tiles[0][2]; // building
            default -> tiles[0][3]; // ? texture
        };
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

    private Color getColourFromUID(int UID) {
        return switch (UID) {
            case 0 -> Color.PURPLE;
            case 1 -> Color.GREEN;
            case 2 -> Color.BLUE;
            case 3 -> Color.RED;
            default -> Color.BROWN;
        };
    }

    // Centers a sprite based on the screen dimensions
    private void centerSprite(Sprite sprite) {
        float centerX = (RESOLUTIONX - sprite.getWidth()) / 2.0f;
        float centerY = (RESOLUTIONY - sprite.getHeight()) / 2.0f;
        sprite.setPos(centerX, centerY);
    }

    // Overload for setting either X or Y if necessary
    private void centerSpriteX(Sprite sprite) {
        float centerX = (RESOLUTIONX - sprite.getWidth()) / 2.0f;
        sprite.setPos(centerX, sprite.rectangle.y); // keep current Y
    }

    private void centerSpriteY(Sprite sprite) {
        float centerY = (RESOLUTIONY - sprite.getHeight()) / 2.0f;
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
            mediumFont.draw(batch, menuText, menuOptionInitx, (menuOptionInity - i * 40));
        }

        extraBoldFont.draw(batch, "Main Menu", 20, 700);
        smallFont.draw(batch, "Welcome to unisim!", 20, 660);
        drawRotatedText(boldFont, batch, "By windows007", 655, 380, 9);

        // 640 is 1/2 of 1280 (center of screen)
        drawCenteredText(normalFont, batch, menuOptionsExplanations[menuSelection], 640, 270);

        drawCenteredText(normalFont, batch, "Use UP or DOWN to select an option", 640, 50);
        drawCenteredText(normalFont, batch, "Press ENTER to select", 640, 30);
    }

    private void renderPauseScreen() {
        renderTime();

        drawCenteredText(extraBoldFont, batch, "Paused", 640, 475);

        String menuText = "";
        for (int i = 0; i < pauseOptions.length; i++) {
            menuText = pauseOptions[i];
            mediumFont.draw(batch, menuText, menuOptionInitx, (menuOptionInity - i * 40));
        }
        drawCenteredText(normalFont, batch, "Use UP or DOWN to select an option", 640, 50);
        drawCenteredText(normalFont, batch, "Press ENTER to select", 640, 30);
    }

    private void renderGameText() {
        renderTime();

        accomodationcount = 0;
        cafeteriacount = 0;
        coursecount = 0;
        recreationalcount = 0;

        for (Building building : buildings) {
            if (building.isAccomodationBuilding())
                accomodationcount++;
            if (building.isCafeteriaBuilding())
                cafeteriacount++;
            if (building.isCourseBuilding())
                coursecount++;
            if (building.isRecreationalBuilding())
                recreationalcount++;
        }

        drawRightAlignedText(boldFont, batch, String.valueOf(buildings.size()), 1270, 630);
        drawRightAlignedText(smallFont, batch, "Buildings placed", 1270, 600);
        drawRightAlignedText(smallFont, batch, accomodationcount + " accommodation", 1270, 580);
        drawRightAlignedText(smallFont, batch, cafeteriacount + " cafeteria", 1270, 560);
        drawRightAlignedText(smallFont, batch, coursecount + " course", 1270, 540);
        drawRightAlignedText(smallFont, batch, recreationalcount + " recreational", 1270, 520);

        drawRightAlignedText(boldFont, batch, "50%", 1270, 480);
        drawRightAlignedText(smallFont, batch, "Satisfaction rating", 1270, 450);

        if (showCanPlaceBuilding) {
            smallFont.draw(batch, "Can't place here!", mouseX + 15, mouseY + 15);
        }

    }

    private void renderBuildingSelectionText() {
        String menuText = "";
        for (int i = 0; i < buildingPresetNames.length; i++) {
            menuText = buildingPresetNames[i];
            drawCenteredText(smallerFont, batch, menuText, (i * 150) + 75, 40);
        }
    }

    private void renderTime() {
        drawRightAlignedText(boldFont, batch, timeRemainingReadable, 1240, 700);
        drawRightAlignedText(smallFont, batch, "Time remaining", 1270, 670);
    }

    private void renderGameOverText() {
        drawRightAlignedText(boldFont, batch, timeRemainingReadable, 1240, 700);
        drawRightAlignedText(smallFont, batch, "Time's up!", 1270, 670);

        drawCenteredText(extraBoldFont, batch, "Game over!", 640, 600);

        drawCenteredText(boldFont, batch, "Stats", 640, 460);
        drawCenteredText(smallFont, batch, "Buildings placed: " + String.valueOf(buildings.size()), 640, 420);
        drawCenteredText(smallFont, batch, accomodationcount + " accommodation", 640, 400);
        drawCenteredText(smallFont, batch, cafeteriacount + " cafeteria", 640, 380);
        drawCenteredText(smallFont, batch, coursecount + " course", 640, 360);
        drawCenteredText(smallFont, batch, recreationalcount + " recreational", 640, 340);

        drawCenteredText(smallFont, batch, "50% satisfaction", 640, 290);

        String menuText = "";
        for (int i = 0; i < gameOverOptions.length; i++) {
            menuText = gameOverOptions[i];
            mediumFont.draw(batch, menuText, menuOptionInitx, (menuOptionInity - i * 40));
        }
        drawCenteredText(normalFont, batch, "Use UP or DOWN to select an option", 640, 50);
        drawCenteredText(normalFont, batch, "Press ENTER to select", 640, 30);
    }

    private void renderDebugText() {
        smallFont.draw(batch, "GAME STATE: " + gameState, 0, 700);
        smallFont.draw(batch, "FRAMES ELAPSED: " + framesElapsed, 0, 670);
        smallFont.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 0, 640);
        // font.draw(batch, "TIME ELAPSED: " + timeElapsed, 0, 180);

        smallFont.draw(batch, "Mouse pos: " + mouseX + ", " + mouseY, 0, 600);
        smallFont.draw(batch, "menuSelection: " + menuSelection, 0, 580);
        smallFont.draw(batch, "current spriteIDs list: " + render.setOfIDs.toString(), 0, 500);
        smallFont.draw(batch, "current res: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight(), 0, 480);
        smallFont.draw(batch, "current selected building: " + selectedBuildingIndex, 0, 450);
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

        Affine2 transform = new Affine2(); // affine transformation to rotate text
        transform.translate(x, y); // move to the rotation origin
        transform.rotate(angleDegrees); // apply rotation
        transform.translate(-x, -y); // move back to original position

        // apply transformation matrix to batch
        batch.setTransformMatrix(batch.getTransformMatrix().setAsAffine(transform));

        batch.begin();
        font.draw(batch, text, x, y); // draw text with rotation
        batch.end(); // reset transformation matrix
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
