package io.github.c3team7.unisim.Building;

public class Building {
    private int index;
    private int width;
    private int height;


    private AccommodationBuilding accommodationBuilding;
    private CafeteriaBuilding cafeteriaBuilding;
    private CourseBuilding courseBuilding;
    private RecreationalBuilding recreationalBuilding;


    public Building(int width, int height){
        this.index = -1;
        this.width = width;
        this.height = height;
    }

    public Building(int index, int width, int height){
        this.index = index;
        this.width = width;
        this.height = height;
    }

    public boolean exists(){
        return index != -1;
    }

    public boolean isAccomodationBuilding(){
        return accommodationBuilding != null;
    }

    public boolean isCafeteriaBuilding(){
        return cafeteriaBuilding != null;
    }

    public boolean isCourseBuilding(){
        return courseBuilding != null;
    }

    public boolean isRecreationalBuilding(){
        return recreationalBuilding != null;
    }

    public void setAccommodationBuilding(){
        accommodationBuilding = new AccommodationBuilding();
    }

    public void setCafeteriaBuilding(){
        cafeteriaBuilding = new CafeteriaBuilding();
    }

    public void setCourseBuilding(){
        courseBuilding = new CourseBuilding();
    }

    public void setRecreationalBuilding(){
        recreationalBuilding = new RecreationalBuilding();
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

}
