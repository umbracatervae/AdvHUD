package pt.advHUD;

/**
 * Created by Akshay on 2/15/2017.
 */
public class Coordinate {
    public double coordx;
    public double coordy;
    public Coordinate(){
        coordx = 0;
        coordy = 0;
    }
    public Coordinate(double x, double y){
        coordx = x;
        coordy = y;
    }
    public double[] getCoordinates(){
        double a[] = {coordx,coordy};
        return a;
    }
    public void setCoordinates(double x, double y){
        coordx = x;
        coordy = y;
    }
}
