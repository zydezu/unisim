package io.github.c3team7.unisim.map;

public class Map {
    // 1280, 720 divided by 24 (TILE SIZE)
    private final int WIDTH = 64;
    private final int HEIGHT = 36;

    private final int TILE_SIZE = 24;

    private int[] map; // Each integer is a uid for a tile (e.g. 1=grass, 2=water, etc)

    public Map() {
        map = new int[WIDTH * HEIGHT];
    }

    public int getIndexFromTileCoords(int x, int y) {
        if (y < 0 || y > HEIGHT){
            //TODO
            //Add exception
        }

        if (x < 0 || x > WIDTH){
            //TODO
            //Add exception
        }
        return y * WIDTH + x;
    }

    public int[] getTileCoordsFromIndex(int index){
        return new int[]{index / WIDTH, index % WIDTH};
    }

    //public boolean validPlace(int, Building)


}
