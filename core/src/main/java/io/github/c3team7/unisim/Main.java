package io.github.c3team7.unisim;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
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

public class Main extends Game {
    private final float timeAllowed = 300;
    protected Map map;
    ScreenInfo screeninfo; // debug
    State gameState = State.TITLE;
    //timer
    int framesElapsed = 0;
    float timeElapsed = 0;
    float timeRemaining = 0;
    String timeRemainingReadable = "";
    //mouse position
    int mouseX = 0;
    int mouseY = 0;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private List<Building> buildings;

    private List<Building> buildingPresets;

    @Override
    public void create() {
        screeninfo = new ScreenInfo(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
            Gdx.graphics.getDisplayMode().refreshRate);
//        System.out.println(screeninfo);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();


        font = new BitmapFont(Gdx.files.internal("default.fnt"));

        map = new Map("map.txt"); // generate the map
        buildings = new ArrayList<>(10);
        buildingPresets = new ArrayList<>();

        Building building = new Building(4, 8);
        building.setAccommodationBuilding();

        buildingPresets.add(building);
    }


    @Override
    public void render() {
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
                    // remove title sprites
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

                for (int i = Input.Keys.NUM_0; i <= Input.Keys.NUM_9; i++){
                    if (Gdx.input.isKeyJustPressed(i)){
                        int[] mouseTileCoords = convertMouseCoordsToTileCoords(mouseX, mouseY);
                        Building newBuilding = buildingPresets.get(i - Input.Keys.NUM_0);
                        placeBuilding(mouseTileCoords[0], mouseTileCoords[1], newBuilding.getWidth(), newBuilding.getHeight());
                    }
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

    // boched attempt at keeping 16/9 resizing window
//    @Override
//	public void resize(int width, int height) {
//        viewport.update(width, height, true); // The 'true' flag ensures the camera updates
//	}

    private void logic() {
        framesElapsed++;
        if (gameState == State.GAMEPLAY) {
            timeElapsed = timeElapsed + Gdx.graphics.getDeltaTime();
            timeRemaining = timeAllowed - timeElapsed;
            timeRemainingReadable = formatTime(timeRemaining);
        }
    }

    private boolean placeBuilding(int index, int width, int height) {
        if (!map.placeBuilding(index, width, height)) {
            return false;
        }
        Building building = new Building(index, width, height);
        buildings.add(building);
        return true;
    }

    private boolean placeBuilding(int x, int y, int width, int height) {
        return placeBuilding(map.getIndexFromTileCoords(x, y), width, height);
    }

    private void draw() {
        ScreenUtils.clear(0, 0, 0, 0);
//        viewport.apply();
//        batch.setProjectionMatrix(viewport.getCamera().combined);

        drawBackground();
        batch.begin();
        drawForeground();
        drawDebugText();
        batch.end();
    }

    private void drawBackground() {
        switch (gameState) {
            case TITLE -> drawTitleSplashScreen();
            case GAMEPLAY -> drawMap();
        }
    }

    private void drawTitleSplashScreen() {
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

    private void drawMap() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < map.HEIGHT; y++) {
            for (int x = 0; x < map.WIDTH; x++) {
                shapeRenderer.setColor(getColourFromUID(map.getFromTileCoords(x, y)));
                shapeRenderer.rect(x * map.TILE_SIZE, y * map.TILE_SIZE, map.TILE_SIZE, map.TILE_SIZE);
            }
        }
        shapeRenderer.end();
    }

    private void drawForeground() {
        switch (gameState) {
            case GAMEPLAY -> drawText();
            case TITLE -> drawTitle();
        }
    }

    private void drawText() {
        font.getData().setScale(1);
        font.draw(batch, "NUMBER OF BUILDINGS PLACED: " + buildings.size(), 0, 700);

        int numberOfAccomodationBuildings = 0;
        int numberOfCafeteriaBuildings = 0;
        int numberOfCourseBuildings = 0;
        int numberOfRecreationalBuildings = 0;

        for (Building building : buildings) {
            if (building.isAccomodationBuilding()) {
                numberOfAccomodationBuildings++;
            }

            if (building.isCafeteriaBuilding()) {
                numberOfCafeteriaBuildings++;
            }

            if (building.isCourseBuilding()) {
                numberOfCourseBuildings++;
            }

            if (building.isRecreationalBuilding()) {
                numberOfRecreationalBuildings++;
            }
        }

        font.draw(batch, "NUMBER OF ACCOMMODATION BUILDINGS PLACED: " + numberOfAccomodationBuildings,
            0, 680);
        font.draw(batch, "NUMBER OF CAFETERIA BUILDINGS PLACED: " + numberOfCafeteriaBuildings,
            0, 660);
        font.draw(batch, "NUMBER OF COURSE BUILDINGS PLACED: " + numberOfCourseBuildings,
            0, 640);
        font.draw(batch, "NUMBER OF RECREATIONAL BUILDINGS PLACED: " + numberOfRecreationalBuildings,
            0, 620);
    }

    private void drawDebugText() {
        font.getData().setScale(1);
        font.draw(batch, "GAME STATE: " + gameState, 0, 500);
        font.draw(batch, "FRAMES ELAPSED: " + framesElapsed, 0, 480);
        font.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 0, 460);
        // font.draw(batch, "TIME ELAPSED: " + timeElapsed, 0, 180);

        font.draw(batch, "TIME REMAINING: " + timeRemainingReadable, 0, font.getLineHeight());
    }

    private void drawTitle() {
        font.getData().setScale(2);
        font.draw(batch, "START GAME", 500, 200);
        font.draw(batch, "Press ENTER to start", 500, 150);
    }

    private void drawPauseScreen() {
        font.getData().setScale(3);
        font.draw(batch, "PAUSED", 640, 360);
    }

    private int[] convertMouseCoordsToTileCoords(int mouseX, int mouseY){
        mouseY = screeninfo.height - mouseY;

        return new int[]{(int) mouseX / map.TILE_SIZE, (int) mouseY / map.TILE_SIZE};
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

    private String formatTime(float seconds) {
        int minutes = (int) (seconds / 60);
        int tmpSeconds = (int) (seconds % 60);

        return String.format("%d:%02d", minutes, tmpSeconds);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    //game state
    enum State {
        TITLE,
        GAMEPLAY,
        PAUSED,
        GAMEOVER
    }

    class ScreenInfo {
        int width;
        int height;
        int refresh;
        public ScreenInfo(int width, int height, int refresh) {
            this.width = width;
            this.height = height;
            this.refresh = refresh;
        }

        public String toString() {
            return String.format("Resolution: %1$s x %2$s @ %3$s Hz", width, height, refresh); // string formatting
        }
    }
}
