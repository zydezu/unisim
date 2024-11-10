package io.github.c3team7.unisim;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

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
                    continue; // skip newlines
                }
                if (index >= map.length) {
                    break; // failsafe
                }
                map[index] = c - '0'; // convert char to int
                index++;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
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
     * @return false if the building cannot be placed
     */
    public boolean placeBuilding(int index, int width, int height) {
        int startRow = index / WIDTH;
        int startCol = index % WIDTH;
    
        if (startCol + width > WIDTH || startRow + height > HEIGHT) {
            System.err.println("WARNING! Tried to place a building but it would go out of bounds!");
            return false; // building will go out of bounds
        }
    
        // place building
        for (int rowStart = index, row = 0; row < height; row++, rowStart += WIDTH) {
            for (int column = 0; column < width; column++) {
                map[rowStart + column] = 3;
            }
        }
        return true;
    }    

    private void exportMap() {
        try {
            FileHandle file = Gdx.files.local("save/export.txt"); // libgdx way to write to a file

            StringBuilder mapData = new StringBuilder();
            for (int i = 0; i < map.length; i++) {
                if ((i + 1) % WIDTH == 0) {
                    mapData.append(map[i]).append("\n");
                } else {
                    mapData.append(map[i]);
                }
            }

            file.writeString(mapData.toString(), false); // 'false' overwrites the file if it exists
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
