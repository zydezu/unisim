package io.github.c3team7.unisim.Building;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

import io.github.c3team7.unisim.Render;
import io.github.c3team7.unisim.Sprite;

public class Building {
    private int presetIndex;
    private int index;
    private int width;
    private int height;

    private AccommodationBuilding accommodationBuilding;
    private CafeteriaBuilding cafeteriaBuilding;
    private CourseBuilding courseBuilding;
    private RecreationalBuilding recreationalBuilding;

    public Building(int presetIndex, int width, int height) {
        this.presetIndex = presetIndex;
        this.index = -1;
        this.width = width;
        this.height = height;
    }

    public Building(int presetIndex, int index, int width, int height) {
        this.presetIndex = presetIndex;
        this.index = index;
        this.width = width;
        this.height = height;
    }

    public Building(Building building, int presetIndex, int index){
        if (building.exists()){
            throw new IllegalArgumentException("Building index should be -1");
        }
        this.presetIndex = presetIndex;
        this.index = index;
        this.width = building.getWidth();
        this.height = building.getHeight();

        if (building.isAccomodationBuilding()){
            setAccommodationBuilding();
        }

        if (building.isCafeteriaBuilding()){
            setCafeteriaBuilding();
        }

        if (building.isCourseBuilding()){
            setCourseBuilding();
        }

        if (building.isRecreationalBuilding()){
            setRecreationalBuilding();
        }
    }

    public boolean exists() {
        return index != -1;
    }

    public boolean isAccomodationBuilding() {
        return accommodationBuilding != null;
    }

    public boolean isCafeteriaBuilding() {
        return cafeteriaBuilding != null;
    }

    public boolean isCourseBuilding() {
        return courseBuilding != null;
    }

    public boolean isRecreationalBuilding() {
        return recreationalBuilding != null;
    }

    public void setAccommodationBuilding() {
        accommodationBuilding = new AccommodationBuilding();
    }

    public void setCafeteriaBuilding() {
        cafeteriaBuilding = new CafeteriaBuilding();
    }

    public void setCourseBuilding() {
        courseBuilding = new CourseBuilding();
    }

    public void setRecreationalBuilding() {
        recreationalBuilding = new RecreationalBuilding();
    }


    public int getPresetIndex(){
        return presetIndex;
    }
    public int getIndex(){
        return index;
    }
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
