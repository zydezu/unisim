package io.github.c3team7.unisim;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;

public class Map {
    public final int TILE_SIZE = 20;
    public final int WIDTH = 1280 / TILE_SIZE;
    public final int HEIGHT = 720 / TILE_SIZE;
    private int[] map; // Each integer is a uid for a tile (e.g. 1=grass, 2=water, etc)

    // public int getIndex(int x, int y) {
    // // do ur array stuff here
    // return 0;
    // }

    public Map() {
        map = new int[WIDTH * HEIGHT];

        Arrays.fill(map, 1);
        exportMap();
    }

    public Map(String path) {
        try {
            // Use Gdx.files.internal() for loading from the assets directory
            String fileContent = Gdx.files.internal(path).readString();

            map = new int[WIDTH * HEIGHT];
            int index = 0;

            for (char c : fileContent.toCharArray()) {
                if (c == '\n') {
                    continue; // Skip newlines
                }
                map[index] = c - '0'; // Convert char to integer ('0' -> 0, '1' -> 1, etc.)
                index++;
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println(map);
    }

    public int get(int index) {
        if (index < 0 || index >= map.length) {
            throw new IndexOutOfBoundsException();
        }

        return map[index];
    }

    public int getFromTileCoords(int x, int y) {
        return map[getIndexFromTileCoords(x, y)];
    }

    public int getIndexFromTileCoords(int x, int y) {
        if (y < 0 || y > HEIGHT) {
            throw new IllegalArgumentException(String.format("Invalid Y-coordinate (%1$s)", y));
        }

        if (x < 0 || x > WIDTH) {
            throw new IllegalArgumentException(String.format("Invalid X-coordinate (%1$s)", x));
        }
        return y * WIDTH + x;
    }

    public int[] getTileCoordsFromIndex(int index) {
        return new int[] { index % WIDTH, index / WIDTH };
    }

    /**
     *
     * @param index  The index of the building's bottom left corner in the
     *               {@link #map}
     * @param width  The width of the building in tiles
     * @param height The height of the building in tiles
     * @return true if the building was successfully placed
     */
    public boolean placeBuilding(int index, int width, int height) {
        for (int rowStart = index, row = 0; row < height; row++, rowStart += WIDTH) {
            for (int column = 0; column < width; column++) {
                map[rowStart + column] = 3;
            }
        }
        return true;
    }

    private void exportMap() {
        try {
            FileWriter writer = new FileWriter("export.txt"); // Changed file path

            for (int i = 0; i < map.length; i++) {
                if (((i + 1) % (WIDTH) == 0)) {
                    writer.write(map[i] + "\n");
                } else {
                    writer.write(map[i] + "");
                }

            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
