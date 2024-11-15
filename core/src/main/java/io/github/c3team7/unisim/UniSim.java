package io.github.c3team7.unisim;

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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.c3team7.unisim.Building.Building;
import io.github.c3team7.unisim.Map.Map;

import java.util.ArrayList;
import java.util.List;

// TO DO
//
// Research AssetManager() - https://libgdx.com/wiki/managing-your-assets
// HAI ! ^_^
// bye

/**
 * The class for the game that handles inputs, logic and drawing.
 */
public class UniSim extends Game {
    /**
     * The width of the screen in pixels
     */
    private final int RESOLUTION_X = 1280;

    /**
     * The height of the screen in pixels
     */
    private final int RESOLUTION_Y = 720;

    /**
     * The time limit of the game in seconds
     */
    private final float timeAllowed = 300;

    // map, sprites and tileset

    /**
     * The class to store and handle the map
     */
    protected Map map;
    protected Render render;

    /**
     * Stores the state of the game
     */
    State gameState;

    /**
     * Stores the x position of the mouse
     */
    float mouseX;

    /**
     * Stores the y position of the mouse
     */
    float mouseY;

    /**
     * Counts the number of accommodation buildings placed
     */
    int accommodationBuildingCount;

    /**
     * Counts the number of cafeteria buildings placed
     */
    int cafeteriaBuildingCount;

    /**
     * Counts the number of course buildings placed
     */
    int courseBuildingCount;

    /**
     * Counts the number of recreational buildings placed
     */
    int recreationalBuildingCount;

    /**
     * The camera for the game
     */
    private OrthographicCamera camera;

    /**
     * The viewport for the game
     */
    private Viewport viewport;

    /**
     * Allows shapes to be drawn to the screen
     */
    private ShapeRenderer shapeRenderer;

    /**
     * Allows textures to be drawn to the screen
     */
    private SpriteBatch batch;

    /**
     * Generates fonts
     */
    private FreeTypeFontGenerator generator;

    /**
     * Allows different font options to be selected
     */
    private FreeTypeFontParameter parameter;

    /**
     * The different font sizes
     */
    private BitmapFont normalFont, smallerFont, smallFont, mediumFont, boldFont, extraBoldFont;

    /**
     * The time elapsed since the start of a new game in seconds
     */
    private float timeElapsed;

    /**
     * A string where the time remaining is stored as (x:xx)
     */
    private String timeRemainingReadable;

    /**
     * Stores the tileset in a 2D array
     */
    private TextureRegion[][] tiles;
    // buildings

    /**
     * List of the buildings placed on the map
     */
    private List<Building> buildings;

    /**
     * Presets of buildings that can be placed
     */
    private List<Building> buildingPresets;

    /**
     * The names of the building preset names
     */
    private final String[] buildingPresetNames = { // should probably be moved to a .txt file
        "Accommodation",
        "Cafeteria",
        "Course",
        "Recreational",
    };

    /**
     * The index of the selected building in building presets
     */
    private int selectedBuildingIndex = -1;

    /**
     * Whether the building menu is open
     */
    private Boolean buildingMenuOpen = true;

    /**
     * Whether the "can't place building" text should be drawn
     */
    private boolean showCantPlaceBuilding = false;

    /**
     * Whether the instructions are currently showing
     */
    private Boolean currentlyShowingInstructions = false;

    /**
     * A list of the title screen menu options
     */
    private final String[] menuOptions = { // should probably be moved to a .txt file
        "Start Game",
        "Instructions",
        "Options",
        "Exit to Desktop"
    };

    /**
     * A list to show what each option on the title screen does
     */
    private final String[] menuOptionsExplanations = { // should probably be moved to a .txt file
        "Start a new simulation!",
        "View how to play",
        "Enable fullscreen, etc...",
        "Close the game"
    };

    /**
     * A list of the pause menu options
     */
    private final String[] pauseOptions = { // should probably be moved to a .txt file
        "Continue",
        "Restart",
        "Quit Game"
    };

    /**
     * A list of the game over options
     */
    private final String[] gameOverOptions = { // should probably be moved to a .txt file
        "Restart",
        "Quit Game"
    };

    /**
     * Stores the rectanges of the menu options for mouse selection
     */
    private final ArrayList<Rectangle> optionRects = new ArrayList<>();

    /**
     * The index of the currently selected menu option
     */
    private int menuSelection = 0;

    /**
     * The number of menu options for a given meny
     */
    private int maxMenuOptions = 4;

    /**
     * The starting menu x co-ordinate
     */
    private final int menuOptionInitx = 510;

    /**
     * The starting menu y co-ordinate
     */
    private final int menuOptionInity = 225;
    private float offset = 0f; // Horizontal offset for scrolling
    private final boolean showDebugText = false;


    /**
     * Loads assets and sets up the camera and rendering classes
     */
    @Override
    public void create() {
        // setup camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, RESOLUTION_X, RESOLUTION_Y);
        viewport = new FitViewport(RESOLUTION_X, RESOLUTION_Y, camera);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        batch.enableBlending();

        // setup tiles
        Texture tileset = new Texture(Gdx.files.internal("graphics/tiles/tileset.png"));
        tiles = TextureRegion.split(tileset, 20, 20);

        // setup fonts
        normalFont = createFont("fonts/Montserrat-Regular.ttf", 20, Color.WHITE, 1, Color.BLACK);

        smallerFont = createFont("fonts/Montserrat-Regular.ttf", 15, Color.WHITE, 1, Color.BLACK);
        smallFont = createFont("fonts/Montserrat-Medium.ttf", 18, Color.WHITE, 1, Color.BLACK);
        mediumFont = createFont("fonts/Montserrat-Medium.ttf", 32, Color.WHITE, 1, Color.BLACK);
        boldFont = createFont("fonts/Montserrat-Bold.ttf", 32, Color.WHITE, 1, Color.BLACK);
        extraBoldFont = createFont("fonts/Montserrat-Black.ttf", 48, Color.WHITE, 1, Color.BLACK);

        initialiseGame();
    }

    /**
     * Initialises the game to start in the title screen
     */
    private void initialiseGame() {
        gameState = State.TITLE;
        menuSelection = 0;
        maxMenuOptions = 4;

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
            "graphics/buildings/1.png");
        new Graphic(render, -1000, -1000, 0f, 1f, 111,
            "graphics/buildings/2.png");
        new Graphic(render, -1000, -1000, 0f, 1f, 112,
            "graphics/buildings/3.png");
        new Graphic(render, -1000, -1000, 0f, 1f, 113,
            "graphics/buildings/4.png");

        // get rects for each menu option to select with mouse
        optionRects.clear();
        GlyphLayout layout = new GlyphLayout();
        for (int i = 0; i < menuOptions.length; i++) {
            layout.setText(boldFont, menuOptions[i]);

            optionRects.add(new Rectangle(menuOptionInitx - 15, (menuOptionInity - 32 - i * 40), 300, 32));
        }
    }

    /**
     * Starts the game and resets counters and the map
     */
    private void startGame() {
        gameState = State.GAMEPLAY;
        destroySpritesByIDs(new int[]{1}); // remove title sprites

        timeElapsed = 0;

        map = new Map("map.txt"); // generate the map
        buildings = new ArrayList<>(10);
        buildingPresets = new ArrayList<>();

        // initialise building presets
        Building building = new Building(0, 4, 8);
        building.setAccommodationBuilding();
        buildingPresets.add(building);

        building = new Building(1, 4, 3);
        building.setCafeteriaBuilding();
        buildingPresets.add(building);

        building = new Building(2, 2, 3);
        building.setCourseBuilding();
        buildingPresets.add(building);

        building = new Building(3, 3, 3);
        building.setRecreationalBuilding();
        buildingPresets.add(building);

        accommodationBuildingCount = 0;
        cafeteriaBuildingCount = 0;
        courseBuildingCount = 0;
        recreationalBuildingCount = 0;

        // buildings menu
        selectedBuildingIndex = -1;
        buildingMenuOpen = true;
        showCantPlaceBuilding = false;
    }

    private void setUpFontGenerator(String font) {
        generator = new FreeTypeFontGenerator(Gdx.files.internal(font));
        parameter = new FreeTypeFontParameter();
    }

    private BitmapFont createFont(String font, int fontsize, Color color, int borderwidth, Color bordercolor) {
        setUpFontGenerator(font);

        parameter.size = fontsize;
        parameter.color = color;
        parameter.borderWidth = borderwidth;
        parameter.borderColor = bordercolor;

        parameter.minFilter = TextureFilter.Linear;
        parameter.magFilter = TextureFilter.Linear;

        BitmapFont generatedFont = generator.generateFont(parameter); // BitmapFont from TTF
        generator.dispose();

        return generatedFont;
    }

    private void createTitleAssets() {
        centerSpriteX(new Graphic(render, 0, 365, 0f, 1f, 1, "graphics/unisimlogo.png")); // create + center
    }

    private void createPauseAssets() {
        centerSpriteX(new Graphic(render, 0, 500, 0f, 1f, 20, "graphics/unisimlogopixel.png")); // create + center
    }

    /**
     * Handles inputs according to the game state using
     * {@code switch} statements
     */
    private void inputs() {
        mouseX = Gdx.input.getX();
        mouseY = Gdx.input.getY();

        mouseX = mouseX * (RESOLUTION_X / (float) Gdx.graphics.getWidth());
        mouseY = mouseY * (RESOLUTION_Y / (float) Gdx.graphics.getHeight());
        mouseY = RESOLUTION_Y - mouseY; // 0, 0 mouse = top left - 0, 0 screen = bottom left

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen();
        }

        switch (gameState) {
            case TITLE, GAMEOVER:
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

                    // Check if the left mouse button is pressed
                    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                        boolean canPlaceBuilding = map.canPlaceBuilding(
                            map.getIndexFromTileCoords(mouseTileX, mouseTileY), building.getWidth(), building.getHeight()
                        );
                        if (canPlaceBuilding) {
                            placeBuilding(building, mouseTileX, mouseTileY);
                            buildingMenuOpen = true;
                            selectedBuildingIndex = -1;
                        }
                        else {
                            showCantPlaceBuilding = true;
                            Timer timer = new Timer();
                            timer.scheduleTask(new Timer.Task() {
                                @Override
                                public void run() {
                                    showCantPlaceBuilding = false;
                                }
                            }, 1f);
                        }
                    }
                }
                else {
                    if (buildingMenuOpen && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                        if (mouseY <= 150) {
                            int maxX = buildingPresets.size() * 150;
                            if (mouseX < maxX) {
                                selectedBuildingIndex = (int) Math.floor(mouseX / 150);  // Select building based on mouse position
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Toggles fullscreen
     */
    private void toggleFullscreen() {
        boolean fullScreen = Gdx.graphics.isFullscreen();
        Monitor currMonitor = Gdx.graphics.getMonitor();
        if (fullScreen) {
            Gdx.graphics.setWindowedMode(1280, 720);
        } else {
            DisplayMode displayMode = Gdx.graphics.getDisplayMode(currMonitor);
            Gdx.graphics.setFullscreenMode(displayMode);
        }
    }

    /**
     * Changes menu selection according to inputs
     */
    private void menuSelectionInputs() {
        // Keyboard
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            menuSelection -= 1;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            menuSelection += 1;
        }
        menuSelection = (menuSelection + maxMenuOptions) % maxMenuOptions;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (currentlyShowingInstructions) {
                render.removeSpriteByID(300);
                currentlyShowingInstructions = false;
            } else {
                selectMenuOption(menuSelection);
            }
        }

        // Mouse
        if (!currentlyShowingInstructions) {
            for (int i = 0; i < optionRects.size(); i++) {
                if (!(optionRects.get(i).contains(mouseX, mouseY))) {
                    continue;
                }
                menuSelection = i;
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    selectMenuOption(menuSelection);
                }
            }
        }
    }

    /**
     * Selects the menu option by game state
     * @param menuSelection The index of the selected menu
     *                      option
     */
    private void selectMenuOption(int menuSelection) {
        switch (gameState){
            case TITLE:
                switch (menuSelection) {
                    case 0:
                        startGame();
                        break;
                    case 1:
                        new Graphic(render, 0, 0, 0f, 1f, 300,
                            "graphics/instructions.jpg");
                        currentlyShowingInstructions = true;
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
                break;

            case PAUSED:
                switch (menuSelection) {
                    case 0:
                        unpauseGame();
                        break;
                    case 1:
                        destroySpritesByIDs(new int[]{20}); // remove pause sprites
                        restartGame();
                        break;
                    case 2:
                        exitToMainMenu();
                        break;
                    default:
                        break;
                }
                break;

            case GAMEOVER:
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
                break;

            default:
                break;
        }
    }


    /**
     * Pauses the game
     */
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

    /**
     * Unpauses the game
     */
    private void unpauseGame() {
        gameState = State.GAMEPLAY;
        destroySpritesByIDs(new int[]{20}); // remove pause sprites
    }

    /**
     * Restarts the game
     */
    private void restartGame() {
        initialiseGame();
        startGame();
    }

    /**
     * Exits to main menu
     */
    private void exitToMainMenu() {
        destroySpritesByIDs(new int[]{20}); // remove pause sprites
        initialiseGame();
    }

    /**
     * Sets up game over menu options
     */
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

    /**
     * Handles logic according to the game state
     */
    private void logic() {
        if (gameState == State.GAMEPLAY) {
            timeElapsed += Gdx.graphics.getDeltaTime();
            float timeRemaining = (float) Math.ceil(timeAllowed - timeElapsed);

            if (timeRemaining < 0) {
                timeRemainingReadable = "0:00";
                gameState = State.GAMEOVER;
                gameOverMenuOptions();
            }
            timeRemainingReadable = convertTimeToReadable(timeRemaining);
            buildingMenuOpen = (selectedBuildingIndex == -1);
        }
    }

    /**
     * Places a building from a building preset at the
     * given tile co-ordinates if the placement is valid
     * @param building The base building
     * @param x The x co-ordinate of the building in tiles
     * @param y The y co-ordinate of the building in tiles
     */
    private void placeBuilding(Building building, int x, int y) {
        int index = map.getIndexFromTileCoords(x, y);
        if (!map.placeBuilding(index, building.getWidth(), building.getHeight())) {
            return;
        }

        Building newBuilding = new Building(building, index);
        buildings.add(newBuilding);
    }

    /**
     * Handles drawing according to the game state using
     * {@code switch} statements
     */
    private void draw() {
        // BG colour and set background
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);


        switch (gameState) {
            case TITLE:
                drawCheckerPattern();
                drawTransparentMenuBoxes();
                break;
            case PAUSED:
                drawMap();
                drawCheckerPattern();
                drawTransparentMenuBoxes();
                drawClockIcon(1255, 688, 12, ((timeElapsed % 1) * 360) + 90);
                break;
            default:
                break;
        }

        batch.begin();
        switch (gameState) {
            case TITLE:
                drawTitleScreen();
                break;

            case GAMEPLAY:
                drawMapTiles();
                batch.end();
                if (buildingMenuOpen) {
                    drawBuildingTransparentBox();
                } else if (selectedBuildingIndex != -1) {
                    drawPlacerBox();
                }

                drawClockIcon(1255, 688, 12, ((timeElapsed % 1) * 360) + 90);
                batch.begin();
                drawGameText();

                if (buildingMenuOpen) {
                    drawBuildingSelectionText();
                    hideSpritesByID(new int[]{101, 102});
                    // render building icons that go above text
                    for (int i = 0; i < buildingPresetNames.length; i++) {
                        render.getSpriteByID(110 + i).setPos(25 + (i * 150), 45);
                    }
                } else {
                    render.getSpriteByID(101).setPos(mouseX - 15, mouseY - 25);
                    render.getSpriteByID(102).setPos(mouseX + 12, mouseY - 25);

                    hideSpritesByID(new int[]{110, 111, 112, 113});
                }
                break;

                case PAUSED:
                    hideGameSprites();
                    drawPauseScreen();
                    break;

                case GAMEOVER:
                    hideGameSprites();
                    drawMapTiles();
                    batch.end();

                    drawTransparentMenuBoxes();
                    drawClockIcon(1255, 688, 12, 0);;
                    drawGameOverText();

                default:
                    break;
        }

        if (showDebugText) {
            drawDebugText();
        }

        for (Sprite sprite : new ArrayList<>(render.getEntities())) {
            sprite.draw(batch, Gdx.graphics.getDeltaTime());
        }
        batch.end();
    }

    /**
     * Hides a list of sprites given their id's
     * @param spriteIDs The ids of the sprites to be hidden
     */
    public void hideSpritesByID(int[] spriteIDs) {
        for (int id : spriteIDs) {
            if (render.setOfIDs.contains(id)) {
                render.getSpriteByID(id).moveOffScreen();
            }
        }
    }

    /**
     * Hides all game sprites
     */
    private void hideGameSprites() {
        hideSpritesByID(new int[]{101, 102, 110, 111, 112, 113});
    }

    /**
     * Draws the building placement box to show where a
     * building will be placed and show which tiles are
     * valid and invalid.
     */
    private void drawPlacerBox() {
        // needed for transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Building building = buildingPresets.get(selectedBuildingIndex);

        int[] mouseTileCoords = convertMouseCoordsToTileCoords(mouseX, mouseY);
        int index = map.getIndexFromTileCoords(mouseTileCoords[0], mouseTileCoords[1]);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int rowStart = index, row = 0; row < building.getHeight(); row++, rowStart += map.WIDTH) {
            for (int column = 0; column < building.getWidth(); column++) {
                if (map.isIndexOutOfBounds(rowStart + column)) {
                    continue;
                }
                if (map.get(rowStart + column) != 1) {
                    shapeRenderer.setColor(1f, 0, 0, 0.6f); // draw filled
                }
                else {
                    shapeRenderer.setColor(0, 1f, 0, 0.6f); // draw filled
                }

                int tileX;
                int tileY;
                int[] tileCoords = map.getTileCoordsFromIndex(rowStart + column);
                tileX = tileCoords[0];
                tileY = tileCoords[1];

                shapeRenderer.rect(tileX * map.TILE_SIZE, tileY * map.TILE_SIZE, map.TILE_SIZE, map.TILE_SIZE);
            }
        }
        shapeRenderer.end();

        shapeRenderer.setColor(1, 1, 1, 1); // draw outline
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(
            (int) (mouseX / map.TILE_SIZE) * map.TILE_SIZE,
            (int) (mouseY / map.TILE_SIZE) * map.TILE_SIZE,
            building.getWidth() * map.TILE_SIZE,
            building.getHeight() * map.TILE_SIZE);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawBuildingTransparentBox() {
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

    /**
     * Draw transparent menu boxes for the title screen
     */
    private void drawTransparentMenuBoxes() {
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
            }
            else {
                shapeRenderer.setColor(0, 0, 0, 0.8f);
            }
            shapeRenderer.rect(menuOptionInitx - 15, (menuOptionInity - 30 - i * 40), 300, 36);
        }
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Draws a scrolling checker pattern
     */
    private void drawCheckerPattern() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Speed of scrolling in pixels per second
        float scrollSpeed = 50f;
        offset += scrollSpeed * Gdx.graphics.getDeltaTime(); // Moves right
        // tile scrolling effect
        int tileSize = 50;
        offset %= tileSize;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // adjust x and y based on offset
        for (int y = -tileSize + (int) offset; y < RESOLUTION_Y + tileSize; y += tileSize) {
            for (int x = -tileSize + (int) offset; x < RESOLUTION_X + tileSize; x += tileSize) {
                if (((x + tileSize) / tileSize + (y + tileSize) / tileSize) % 2 == 0) {
                    shapeRenderer.setColor(0f, 0f, 0f, gameState == State.PAUSED ? 0.25f : 1f);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                }
                else {
                    shapeRenderer.setColor(0.8f, 0.2f, 0.2f, gameState == State.PAUSED ? 0.25f : 1f);
                    shapeRenderer.rect(x, y, tileSize, tileSize);
                }
            }
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Draws a clock at a given location
     * @param x The x position of the clock centre in
     *          pixels
     * @param y The y position of the clock center in
     *          pixels
     * @param radius The radius of the clock in pixels
     * @param degrees The amount of rotation of the clock
     *               hand in degrees
     */
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

    /**
     * Draws the map tiles
     */
    private void drawMapTiles() {
        for (int y = 0; y < map.HEIGHT; y++) {
            for (int x = 0; x < map.WIDTH; x++) {
                if (map.getFromTileCoords(x, y) != 3) {
                    batch.draw(getTileFromUID(map.getFromTileCoords(x, y)),
                        x * map.TILE_SIZE,
                        y * map.TILE_SIZE,
                        map.TILE_SIZE,
                        map.TILE_SIZE);
                    continue;
                }

                int buildingStartTileX;
                int buildingStartTileY;
                int[] buildingStartTileCoords = map.getTileCoordsFromIndex(getBuildingStartIndex(map.getIndexFromTileCoords(x, y)));

                int buildingPresetIndex = getIndexOfBuildingPreset(map.getIndexFromTileCoords(x, y));
                buildingStartTileX = buildingStartTileCoords[0];
                buildingStartTileY = buildingStartTileCoords[1];

                switch (buildingPresetIndex) {
                    case 0:
                        batch.draw(tiles[1][((x * 9301 + 49297 - y) % 233280) % 2], x * map.TILE_SIZE, y * map.TILE_SIZE, map.TILE_SIZE, map.TILE_SIZE);
                        break;

                    case 1:
                        batch.draw(tiles[7 - (y - buildingStartTileY)][x - buildingStartTileX], x * map.TILE_SIZE, y * map.TILE_SIZE, map.TILE_SIZE, map.TILE_SIZE);
                        break;

                    case 2:
                        batch.draw(tiles[4 - (y - buildingStartTileY)][x - buildingStartTileX], x * map.TILE_SIZE, y * map.TILE_SIZE, map.TILE_SIZE, map.TILE_SIZE);
                        break;

                    case 3:
                        batch.draw(tiles[4 - (y - buildingStartTileY)][2 + (x - buildingStartTileX)], x * map.TILE_SIZE, y * map.TILE_SIZE, map.TILE_SIZE, map.TILE_SIZE);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Gets building preset index a building was based off
     * of given a tile index of the building
     * @param buildingTileIndex The queried building
     * @return The preset index that belongs to the building
     */
    private int getIndexOfBuildingPreset(int buildingTileIndex) {
        for (Building building : buildings) {
            int buildingStartTileX;
            int buildingStartTileY;
            int[] buildingStartTileCoords = map.getTileCoordsFromIndex(building.getIndex());
            buildingStartTileX = buildingStartTileCoords[0];
            buildingStartTileY = buildingStartTileCoords[1];

            int buildingTileX;
            int buildingTileY;
            int[] buildingTileCoords = map.getTileCoordsFromIndex(buildingTileIndex);
            buildingTileX = buildingTileCoords[0];
            buildingTileY = buildingTileCoords[1];


            int xMax = buildingStartTileX + building.getWidth() - 1;
            int yMax = buildingStartTileY + building.getHeight() - 1;

            if (buildingTileX >= buildingStartTileX && buildingTileX <= xMax && buildingTileY >= buildingStartTileY && buildingTileY <= yMax) {
                return building.getPresetIndex();
            }
        }
        throw new RuntimeException("No Building Found");
    }

    /**
     * Gets start index of  a building was based off
     * of given a tile index of the building
     * @param buildingTileIndex The queried building
     * @return The index of the building in the map
     */
    private int getBuildingStartIndex(int buildingTileIndex) {
        for (Building building : buildings) {
            int buildingStartTileX;
            int buildingStartTileY;
            int[] buildingStartTileCoords = map.getTileCoordsFromIndex(building.getIndex());
            buildingStartTileX = buildingStartTileCoords[0];
            buildingStartTileY = buildingStartTileCoords[1];

            int buildingTileX;
            int buildingTileY;
            int[] buildingTileCoords = map.getTileCoordsFromIndex(buildingTileIndex);
            buildingTileX = buildingTileCoords[0];
            buildingTileY = buildingTileCoords[1];

            int xMax = buildingStartTileX + building.getWidth() - 1;
            int yMax = buildingStartTileY + building.getHeight() - 1;

            if (buildingTileX >= buildingStartTileX && buildingTileX <= xMax && buildingTileY >= buildingStartTileY && buildingTileY <= yMax) {
                return building.getIndex();
            }
        }
        throw new RuntimeException("No Building Found");
    }

    /**
     * Formats a time
     * @param seconds The unformatted time in seconds
     * @return A formatted time
     */
    private String convertTimeToReadable(float seconds) {
        int minutes = (int) seconds / 60;
        int tmpSeconds = (int) seconds % 60;

        return String.format("%d:%02d", minutes, tmpSeconds);
    }

    /**
     * Returns the texture region of tile given a uid
     * @param UID The uid of the tile
     * @return The corresponding texture region of the uid
     */
    private TextureRegion getTileFromUID(int UID) {
        return switch (UID) {
            case 0 -> tiles[0][4]; // unknown texture
            case 1 -> tiles[0][0]; // grass
            case 2 -> tiles[0][1]; // water
            case 3 -> tiles[0][2]; // building
            default -> tiles[0][3]; // ? texture
        };
    }

    /**
     * Draw the map
     */
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

    /**
     * Gets the colour of a tile based off of a uid
     * @param UID The uid of the tile
     * @return The color of the corresponding tile
     */
    private Color getColourFromUID(int UID) {
        return switch (UID) {
            case 0 -> Color.PURPLE;
            case 1 -> Color.GREEN;
            case 2 -> Color.BLUE;
            case 3 -> Color.RED;
            default -> Color.BROWN;
        };
    }

    /**
     * Converts mouse co-ordinates to tile co-ordinates
     * @param mouseX The x position of the mouse in pixels
     * @param mouseY The y position of the mouse in pixels
     * @return [mouseTileX, mouseTileY]
     */
    private int[] convertMouseCoordsToTileCoords(float mouseX, float mouseY) {
        return new int[]{(int) mouseX / map.TILE_SIZE, (int) mouseY / map.TILE_SIZE};
    }

    // Overload for setting either X or Y if necessary
    private void centerSpriteX(Sprite sprite) {
        float centerX = (RESOLUTION_X - sprite.getWidth()) / 2.0f;
        sprite.setPos(centerX, sprite.rectangle.y); // keep current Y
    }

    /**
     * Destroys an array of sprites by ids
     * @param spriteIDs The array of sprite ids to be
     *                  destroyed
     */
    public void destroySpritesByIDs(int[] spriteIDs) {
        for (int id : spriteIDs) {
            render.removeSpriteByID(id);
        }
    }

    /**
     * Draws the title screen
     */
    private void drawTitleScreen() {
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

    /**
     * Draws the pause screen
     */
    private void drawPauseScreen() {
        drawTime();

        drawCenteredText(extraBoldFont, batch, "Paused", 640, 475);

        String menuText = "";
        for (int i = 0; i < pauseOptions.length; i++) {
            menuText = pauseOptions[i];
            mediumFont.draw(batch, menuText, menuOptionInitx, (menuOptionInity - i * 40));
        }
        drawCenteredText(normalFont, batch, "Use UP or DOWN to select an option", 640, 50);
        drawCenteredText(normalFont, batch, "Press ENTER to select", 640, 30);
    }

    /**
     * Draws the text for the game
     */
    private void drawGameText() {
        drawTime();

        accommodationBuildingCount = 0;
        cafeteriaBuildingCount = 0;
        courseBuildingCount = 0;
        recreationalBuildingCount = 0;

        for (Building building : buildings) {
            if (building.isAccomodationBuilding())
                accommodationBuildingCount++;
            if (building.isCafeteriaBuilding())
                cafeteriaBuildingCount++;
            if (building.isCourseBuilding())
                courseBuildingCount++;
            if (building.isRecreationalBuilding())
                recreationalBuildingCount++;
        }

        drawRightAlignedText(boldFont, batch, String.valueOf(buildings.size()), 1270, 630);
        drawRightAlignedText(smallFont, batch, "Buildings placed", 1270, 600);
        drawRightAlignedText(smallerFont, batch, recreationalBuildingCount + " accommodation", 1270, 580);
        drawRightAlignedText(smallerFont, batch, cafeteriaBuildingCount + " cafeteria", 1270, 560);
        drawRightAlignedText(smallerFont, batch, courseBuildingCount + " course", 1270, 540);
        drawRightAlignedText(smallerFont, batch, recreationalBuildingCount + " recreational", 1270, 520);

        drawRightAlignedText(boldFont, batch, "50%", 1270, 480);
        drawRightAlignedText(smallFont, batch, "Satisfaction", 1270, 450);

        if (selectedBuildingIndex != -1) {
            drawCenteredText(smallerFont, batch, buildingPresetNames[selectedBuildingIndex], mouseX, mouseY - 30);
        }
        if (showCantPlaceBuilding) {
            smallFont.draw(batch, "Can't place here!", mouseX + 15, mouseY + 15);
        }

    }

    /**
     * Draws the building selection text
     */
    private void drawBuildingSelectionText() {
        String menuText = "";
        for (int i = 0; i < buildingPresetNames.length; i++) {
            menuText = buildingPresetNames[i];
            drawCenteredText(smallerFont, batch, menuText, (i * 150) + 75, 40);
        }
    }

    /**
     * Draws the remaining time
     */
    private void drawTime() {
        drawRightAlignedText(boldFont, batch, timeRemainingReadable, 1240, 700);
        drawRightAlignedText(smallFont, batch, "Time remaining", 1270, 670);
    }

    /**
     * Draws the game over text
     */
    private void drawGameOverText() {
        drawRightAlignedText(boldFont, batch, timeRemainingReadable, 1240, 700);
        drawRightAlignedText(smallFont, batch, "Time's up!", 1270, 670);

        drawCenteredText(extraBoldFont, batch, "Game over!", 640, 600);

        drawCenteredText(boldFont, batch, "Stats", 640, 460);
        drawCenteredText(smallFont, batch, "Buildings placed: " + buildings.size(), 640, 420);
        drawCenteredText(smallFont, batch, accommodationBuildingCount + " Accomodation Buildings Placed", 640, 400);
        drawCenteredText(smallFont, batch, cafeteriaBuildingCount + " Cafeteria Buildings Placed", 640, 380);
        drawCenteredText(smallFont, batch, courseBuildingCount + " Course Buildings Placed", 640, 360);
        drawCenteredText(smallFont, batch, recreationalBuildingCount + " Recreational Buildings Placed", 640, 340);

        drawCenteredText(smallFont, batch, "50% Satisfaction Score", 640, 290);

        String menuText = "";
        for (int i = 0; i < gameOverOptions.length; i++) {
            menuText = gameOverOptions[i];
            mediumFont.draw(batch, menuText, menuOptionInitx, (menuOptionInity - i * 40));
        }
        drawCenteredText(normalFont, batch, "Use UP or DOWN to select an option", 640, 50);
        drawCenteredText(normalFont, batch, "Press ENTER to select", 640, 30);
    }

    /**
     * Draw the debug text
     */
    private void drawDebugText() {
        smallFont.draw(batch, "GAME STATE: " + gameState, 0, 700);
        smallFont.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 0, 640);

        smallFont.draw(batch, "Mouse pos: " + mouseX + ", " + mouseY, 0, 600);
        smallFont.draw(batch, "menuSelection: " + menuSelection, 0, 580);
        smallFont.draw(batch, "current spriteIDs list: " + render.setOfIDs.toString(), 0, 500);
        smallFont.draw(batch, "current res: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight(), 0, 480);
        smallFont.draw(batch, "current selected building: " + selectedBuildingIndex, 0, 450);
    }

    public void drawCenteredText(BitmapFont font, SpriteBatch batch, String text, float x, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        float adjustedX = x - layout.width / 2;
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

    /**
     * Main update loop to handle input, logic and drawing
     */
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

    /**
     * Represents the different states the game can be in
     * and helps control game follow and handle events based
     * on the current game state
     */
    enum State {
        /**
         * The initial state before the game starts showing
         * the title screen
         */
        TITLE,

        /**
         * The state for active game play
         */
        GAMEPLAY,

        /**
         * The state for if the game is paused
         */
        PAUSED,

        /**
         * The state for if the game is over
         */
        GAMEOVER
    }
}
