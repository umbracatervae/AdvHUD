package hud.advancedhud_moverio;

import java.nio.DoubleBuffer;

/**
 * Created by Akshay on 2/15/2017.
 */
public class Wall {
    public Double startX;
    public Double startY;
    public Double endX;
    public Double endY;

    public Wall(){
        startX = 0.0;
        startY = 0.0;
        endX = 0.0;
        endY= 0.0;
    }

    public Wall(Coordinate start, Coordinate end){
        startX = start.getCoordinates()[0];
        startY = start.getCoordinates()[1];
        endX = end.getCoordinates()[0];
        endY = end.getCoordinates()[1];
    }
    public Wall(Double sx, Double sy, Double ex, Double ey){
        startX = sx;
        startY = sy;
        endX = ex;
        endY = ey;
    }
    @Override
    public String toString(){
        return "(" + startX + "," + startY + ") (" + endX + "," + endY + ")";
    }
    public Double[] getStart(){
        Double a[] = {startX,startY};
        return a;
    }
    public Double[] getEnd(){
        Double a[] = {endX,endY};
        return a;
    }
    public Double[] getDoubleArray(){
        Double a[] = {startX,startY,endX,endY};
        return a;
    }
    public void setCoordinates(Double sx, Double sy, Double ex, Double ey){
        startX= sx;
        startY= sy;
        endX = ex;
        endY = ey;
    }
}
