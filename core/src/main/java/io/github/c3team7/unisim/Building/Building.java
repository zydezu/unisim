package io.github.c3team7.unisim.Building;

public class Building {
    private int index;
    private int width;
    private int height;


    public Building(int width, int height){
        this.index = -1;
        this.width = width;
        this.height = height;
    }

    public boolean exists(){
        return index != -1;
    }
}
