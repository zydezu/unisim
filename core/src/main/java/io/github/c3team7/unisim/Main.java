package io.github.c3team7.unisim;

import java.util.ArrayList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.c3team7.unisim.Map.Map;

public class Main extends Game {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    int framesElapsed = 0;
    float timeElapsed = 0;
    float timeRemaining = 0;
    private final float timeAllowed = 300;

    protected Map map;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        font = new BitmapFont(Gdx.files.internal("default.fnt"));

        map = new Map("map.txt");
        map.placeBuilding(map.getIndexFromTileCoords(1, 3), 3, 2);
    }

    private void input(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(1280, 720);
                return;
            }

            Monitor currMonitor = Gdx.graphics.getMonitor();
            DisplayMode displayMode = Gdx.graphics.getDisplayMode(currMonitor);
            Gdx.graphics.setFullscreenMode(displayMode);
        }
    }

    private void logic(){
        framesElapsed++;
        timeElapsed = timeElapsed + Gdx.graphics.getDeltaTime();
        timeRemaining = timeAllowed - timeElapsed;
    }

    private void draw(){
        ScreenUtils.clear(Color.BLACK);

        drawMap();
        drawHUD();
    }

    private void drawMap(){
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < map.HEIGHT; y = y + 1) {
            for (int x = 0; x < map.WIDTH; x = x + 1) {
                shapeRenderer.setColor(getColourFromUID(map.getFromTileCoords(x, y)));
                shapeRenderer.rect(x * map.TILE_SIZE, y * map.TILE_SIZE, map.TILE_SIZE, map.TILE_SIZE);
            }
        }
        shapeRenderer.end();
    }

    private void drawHUD(){
        batch.begin();
        font.getData().setScale(1);
        font.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 0, font.getLineHeight());
        font.draw(batch, "FRAMES ELAPSED: " + framesElapsed, 0, 150);
        font.draw(batch, "TIME ELAPSED: " + timeElapsed, 0, 180);
        font.draw(batch, "TIME REMAINING: " + timeRemaining, 0, 210);
        batch.end();
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

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
