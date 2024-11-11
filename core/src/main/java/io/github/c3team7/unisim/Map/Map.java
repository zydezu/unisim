package io.github.c3team7.unisim.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * This class represents a tiled map and can export and
 * import from a .txt file and allows rectangular buildings
 * to be validated and placed along with various helper
 * methods to convert co-ordinates to an index and
 * vice-versa.
 */
public class Map {
    /**
     * The tile size of the map in pixels
     */
    public final int TILE_SIZE = 20;

    /**
     * The width of the map in tiles
     */
    public final int WIDTH = 1280 / TILE_SIZE;

    /**
     * The height of the map in tiles
     */
    public final int HEIGHT = 720 / TILE_SIZE;

    /**
     * An array of uid's (unique identifiers) for tiles
     * (e.g. 1=grass, 2=water, 3=building)
     */
    private int[] map;

    /**
     * Constructs a Map from a .txt file.
     * @param path The path to the .txt file
     */
    public Map(String path) {
        try {
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

    /**
     * Gets the uid at a given index
     * @param index The queried index
     * @return The uid at the given index
     */
    public int get(int index) {
        return map[index];
    }

    /**
     * Gets the uid at the given tile co-ordinates
     * @param x The x tile co-ordinate of the uid
     * @param y The y tile co-ordinate of the uid
     * @return The uid at the given tile co-ordinates
     */
    public int getFromTileCoords(int x, int y) {
        return map[getIndexFromTileCoords(x, y)];
    }

    /**
     * Converts tile co-ordinates to an index
     * @param x The x co-ordinate
     * @param y The y co-ordinate
     * @return The equivalent index to tile co-ordinates
     */
    public int getIndexFromTileCoords(int x, int y) {
        if (y < 0 || y > HEIGHT) {
            throw new IllegalArgumentException(String.format("Invalid Y-coordinate (%1$s)", y));
        }

        if (x < 0 || x > WIDTH) {
            throw new IllegalArgumentException(String.format("Invalid X-coordinate (%1$s)", x));
        }
        return y * WIDTH + x;
    }

    /**
     *  Converts an index into tile co-ordinates
     * @param index The index
     * @return The equivalent tile co-ordinates to the index
     */
    public int[] getTileCoordsFromIndex(int index) {
        return new int[] { index % WIDTH, index / WIDTH };
    }


    /**
     * Checks if the building can be placed at the given location
     * @param index  The index of the building's bottom
     *               left corner in the {@link #map}
     * @param width  The width of the building in tiles
     * @param height The height of the building in tiles
     * @return {@code true} if the building was successfully placed and
     *         {@code false} if the building cannot be placed.
     */
    public boolean canPlaceBuilding(int index, int width, int height){
        int startRow = index / WIDTH;
        int startCol = index % WIDTH;

        if (startCol + width > WIDTH || startRow + height > HEIGHT) {
            return false; // building will go out of bounds
        }

        for (int rowStart = index, row = 0; row < height; row++, rowStart += WIDTH) {
            for (int column = 0; column < width; column++) {
                if (map[rowStart + column] != 1) {
                    return false;
                }
            }
        }
        return true;

    }

    /**
     *
     * @param index  The index of the building's bottom
     *              left corner in the {@link #map}
     * @param width  The width of the building in tiles
     * @param height The height of the building in tiles
     * @return {@code true} if the building was successfully placed and
     *         {@code false} if the building cannot be placed
     */
    public boolean placeBuilding(int index, int width, int height) {
        if (!canPlaceBuilding(index, width, height)){
            return false;
        }

        for (int rowStart = index, row = 0; row < height; row++, rowStart += WIDTH) {
            for (int column = 0; column < width; column++) {
                map[rowStart + column] = 3;
            }
        }
        return true;
    }

    /**
     * Checks if a given index is out of bounds
     * @param index The index
     * @return {@code true} if the index is out of bounds,
     * {@code false} otherwise
     */
    public boolean isIndexOutOfBounds(int index){
        return (index < 0 || index >= map.length);
    }

    /**
     * Exports the current map to {@code save/export.txt}
     */
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
