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
    private int height = 300;
    private int width = 300;
    public int moveX = 0;
    public int moveY = 0;
    private boolean userLocked = true; //USE ONLY USER USER LOCKED MODE
    public ArrayList<Point> mPoints;
    public ArrayList<Wall> mWalls;
    ArrayList<Coordinate> mPathHistory;

    public MapDrawable(){
        mBackgroundColor = Color.DKGRAY;
        mStrokeColor = Color.BLUE;
        mStrokeWidth = 5;
        mDegreeRotation = 0;
        mPathHistory = new ArrayList<Coordinate>();
    }

    public MapDrawable(int backgroundColor, int strokeColor, int strokeWidth, ArrayList<Point> inPoints){
        mBackgroundColor = backgroundColor;
        mStrokeColor = strokeColor;
        mStrokeWidth = strokeWidth;
        mPoints = inPoints;
        mDegreeRotation = 0;
        mPathHistory = new ArrayList<Coordinate>();
    }

    public void setMapMode(boolean modeFlag){
        if(modeFlag){
            userLocked = true;
        }
        else{
            userLocked = false;
        }
    }

    private Coordinate rotateCoord(Coordinate c, int degrees, int size){
        double degreeRadians = Math.toRadians(degrees);
        double x_trans = c.coordx-(size/2)+moveX;
        double y_trans = c.coordy-(size/2)+moveY;
        double x1 = (Math.cos(-degreeRadians)*x_trans)-(Math.sin(-degreeRadians)*y_trans);
        double y1 = (Math.sin(-degreeRadians)*x_trans)+(Math.cos(-degreeRadians)*y_trans);
        return new Coordinate(x1+(size/2)-moveX,y1+(size/2)-moveY);
    }

    private Path constructUser(){
        Coordinate A = new Coordinate(140,160);
        Coordinate B = new Coordinate(150,140);
        Coordinate C = new Coordinate(160,160);
        if(userLocked) {
            A = new Coordinate(140-moveX,160-moveY);
            B = new Coordinate(150-moveX,140-moveY);
            C = new Coordinate(160-moveX,160-moveY);
            A = rotateCoord(A, mDegreeRotation, height);
            B = rotateCoord(B, mDegreeRotation, height);
            C = rotateCoord(C, mDegreeRotation, height);
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
        pathPaint.setStrokeWidth(4);
        canvas.translate(moveX,moveY);
        if(userLocked){
            canvas.rotate((float)mDegreeRotation,(height/2)-moveX,(width/2)-moveY);
        }
        else{
            canvas.rotate((float)-mDegreeRotation,(height/2)-moveX,(width/2)-moveY);
        }
        canvas.drawColor(mBackgroundColor);
        if(mPoints != null){
            for(int i = 0; i < mPoints.size(); i++){
                Point pt = mPoints.get(i);
                canvas.drawPoint((float)((pt.x + 1.5)*100), (float)(300.0 - ((pt.z + 1.5)*100.0)), mPaint); //x and z being used; why not y?
            }
        }
        if(mPathHistory != null){
            for(int i=0; i < mPathHistory.size(); i++){
                canvas.drawPoint((float)mPathHistory.get(i).coordx,(float)mPathHistory.get(i).coordy,pathPaint);
            }
        }
        /*if(mWalls != null){
            for(int i=0; i < mWalls.size(); i++){
                if(mWalls.get(i).isValid()){
                    canvas.drawLine((float)((mWalls.get(i).getEdge1().x+2.5)*60),
                                    (float)(300-((mWalls.get(i).getEdge1().z+2.5)*60)),
                                    (float)((mWalls.get(i).getEdge1().x+2.5)*60),
                                    (float)(300-((mWalls.get(i).getEdge2().z+2.5)*60)),
                                    mPaint);
                }
            }
        }*/
        mPaint.setColor(Color.RED);
        Path newPath = constructUser();
        canvas.drawPath(newPath,mPaint);
    }

    public void setPointArray(ArrayList<Point> points){
        mPoints = points;
    }

    public void appendPathPoint(Coordinate c){
        mPathHistory.add(c);
    }

    public void setWallArray(ArrayList<Wall> walls){mWalls = walls;}

    public ArrayList<Point> getPointArray(){return mPoints;}

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
