package pt.advHUD;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

/**
 * Created by Akshay on 2/15/2017.
 */

//USE ONLY USER USER LOCKED MODE
public class MapDrawable extends Drawable {
    private int mBackgroundColor;
    private int mStrokeWidth;
    private int mStrokeColor;
    private int mDegreeRotation;
    public static int height = 600;
    public static int width = 600;
    public static double metricRangeTangoX = 10; //this means it will go from -5m to 5m
    public static double metricRangeTangoY = 10;
    public static double zoom = 1;
    public int moveX = 0;
    public int moveY = 0;
    private boolean userLocked = true; //USE ONLY USER USER LOCKED MODE
    public ArrayList<Point> mPoints;
    public ArrayList<Wall2D> mWallList;
    ArrayList<Coordinate> mPathHistory;

    public MapDrawable(){
        mBackgroundColor = Color.DKGRAY;
        mStrokeColor = Color.BLUE;
        mStrokeWidth = 5;
        mDegreeRotation = 0;
        mWallList = new ArrayList<Wall2D>();
        mPathHistory = new ArrayList<Coordinate>();
    }

    public MapDrawable(int backgroundColor, int strokeColor, int strokeWidth, ArrayList<Point> inPoints){
        mBackgroundColor = backgroundColor;
        mStrokeColor = strokeColor;
        mStrokeWidth = strokeWidth;
        mPoints = inPoints;
        mDegreeRotation = 0;
    }

    public void setMapMode(boolean modeFlag){
        if(modeFlag){
            userLocked = true;
        }
        else{
            userLocked = false;
        }
    }

    private Coordinate rotateCoord(Coordinate c, int degrees, int sizex, int sizey){ //used to be just int size
        double degreeRadians = Math.toRadians(degrees);
        double x_trans = c.coordx-(sizex/2)+moveX; //used to be just size
        double y_trans = c.coordy-(sizey/2)+moveY; //used to be just size
        double x1 = (Math.cos(degreeRadians)*x_trans)-(Math.sin(degreeRadians)*y_trans);
        double y1 = (Math.sin(degreeRadians)*x_trans)+(Math.cos(degreeRadians)*y_trans);
        return new Coordinate(x1+(sizex/2)-moveX,y1+(sizey/2)-moveY); //used to be just size
    }

    private Path constructUser(){
        Coordinate A = new Coordinate(140,160);
        Coordinate B = new Coordinate(150,140);
        Coordinate C = new Coordinate(160,160);
        if(userLocked) {
            A = new Coordinate(((-10/zoom)+width/2)-moveX,((10/zoom)+height/2)-moveY); //(140,160)
            B = new Coordinate((width/2)-moveX,((-13/zoom)+height/2)-moveY);  //(150,137)
            C = new Coordinate(((10/zoom)+width/2)-moveX,((10/zoom)+height/2)-moveY);  //(160,160)
            A = rotateCoord(A, -mDegreeRotation, width,height); //just used to be height
            B = rotateCoord(B, -mDegreeRotation, width,height); //just used to be height
            C = rotateCoord(C, -mDegreeRotation, width,height); //just used to be height
        }
        Path newPath = new Path();
        newPath.moveTo((float)A.coordx,(float)A.coordy);
        newPath.lineTo((float)B.coordx,(float)B.coordy);
        newPath.lineTo((float)C.coordx,(float)C.coordy);
        newPath.moveTo((float)B.coordx,(float)B.coordy);
        newPath.lineTo((float)C.coordx,(float)C.coordy);
        newPath.close();
        return newPath;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint mPaint = new Paint();
        Paint pathPaint = new Paint();
        mPaint.setColor(mStrokeColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        pathPaint.setColor(Color.GREEN);
        pathPaint.setStrokeWidth(2);
        canvas.translate(moveX,moveY);

        if(userLocked){
            canvas.rotate((float)mDegreeRotation,(width/2)-moveX,(height/2)-moveY);
        }

        canvas.drawColor(mBackgroundColor);
        mPaint.setColor(Color.YELLOW);
        if(mPoints != null){
            for(int i = 0; i < mPoints.size(); i += 2){
                Point pt = mPoints.get(i);
                canvas.drawPoint((float)((pt.x + metricRangeTangoX/2)*width/metricRangeTangoX), (float)(height - ((pt.z + metricRangeTangoY/2)*height/metricRangeTangoY)), mPaint);
            }
        }
        mPaint.setColor(Color.BLUE);
        if(mWallList != null){
            for(int i=0; i <mWallList.size(); i++){
                Wall2D wl = mWallList.get(i);
                canvas.drawLine((float)((wl.getEdge1().x + metricRangeTangoX/2)*width/metricRangeTangoX),
                        (float)(height - ((wl.getEdge1().z + metricRangeTangoY/2)*height/metricRangeTangoY)),
                        (float)((wl.getEdge2().x + metricRangeTangoX/2)*width/metricRangeTangoX),
                        (float)(height - ((wl.getEdge2().z + metricRangeTangoY/2)*height/metricRangeTangoY)),
                        mPaint);
            }
        }
        if(mPathHistory != null){
            for(int i=0; i < mPathHistory.size(); i++){
                canvas.drawPoint((float)mPathHistory.get(i).coordx,(float)mPathHistory.get(i).coordy,pathPaint);
            }
        }
        mPaint.setColor(Color.RED);
        Path newPath = constructUser();
        canvas.drawPath(newPath,mPaint);
    }

    public void setPointArray(ArrayList<Point> points){
        mPoints = points;
    }

    public void setWallArray(ArrayList<Wall2D> walls){ mWallList = walls;}

    public ArrayList<Point> getPointArray(){return mPoints;}

    public void appendPathPoint(Coordinate c){
        mPathHistory.add(c);
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    public void setStrokeColor(int color){
        mStrokeColor = color;
    }

    public void setDegreeRotation(int degreeRotation){mDegreeRotation = degreeRotation;}

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
