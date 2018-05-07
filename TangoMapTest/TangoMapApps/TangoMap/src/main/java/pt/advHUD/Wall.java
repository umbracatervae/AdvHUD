package pt.advHUD;

/**
 * Created by Akshay on 2/15/2017.
 */
public class Wall {
    public int startX;
    public int startY;
    public int endX;
    public int endY;

    public Wall(){
        startX = 0;
        startY = 0;
        endX = 0;
        endY= 0;
    }
    public Wall(Coordinate start, Coordinate end){
        startX = (int) start.getCoordinates()[0];
        startY = (int) start.getCoordinates()[1];
        endX = (int) end.getCoordinates()[0];
        endY = (int) end.getCoordinates()[1];
    }
    public Wall(int sx, int sy, int ex, int ey){
        startX = sx;
        startY = sy;
        endX = ex;
        endY = ey;
    }
    public double[] getStart(){
        double a[] = {startX,startY};
        return a;
    }
    public double[] getEnd(){
        double a[] = {endX,endY};
        return a;
    }
    public void setCoordinates(int sx, int sy, int ex, int ey){
        startX= sx;
        startY= sy;
        endX = ex;
        endY = ey;
    }
}
