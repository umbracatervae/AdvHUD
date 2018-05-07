package com.akshay.BT200Map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Map;

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
    public Wall[] mWalls;
    private ArrayList<Wall> mDWalls;

    public MapDrawable(){
        mBackgroundColor = Color.DKGRAY;
        mStrokeColor = Color.BLUE;
        mStrokeWidth = 10;
        mDegreeRotation = 0;
    }

    public MapDrawable(int demoCode){
        if (demoCode == 1234){
            defaultSetup();
        }
    }

    public MapDrawable(int backgroundColor, int strokeColor, int strokeWidth, Wall[] walls){
        mBackgroundColor = backgroundColor;
        mStrokeColor = strokeColor;
        mStrokeWidth = strokeWidth;
        mWalls = walls;
        mDegreeRotation = 0;
    }

    public void defaultSetup(){
        mBackgroundColor = Color.DKGRAY;
        mStrokeColor = Color.LTGRAY;
        mStrokeWidth = 10;
        mDegreeRotation = 0;

        //Demo Wall Map
        mWalls = new Wall[8];
        Coordinate A = new Coordinate(90,400);
        Coordinate B = new Coordinate(175,400);
        Coordinate C = new Coordinate(90,140);
        Coordinate D = new Coordinate(175,190);
        Coordinate E = new Coordinate(10,140);
        Coordinate F = new Coordinate(270,190);
        Coordinate G = new Coordinate(10,90);
        Coordinate H = new Coordinate(90,90);
        Coordinate I = new Coordinate(90,40);
        Coordinate J = new Coordinate(270,40);
        mWalls[0] = new Wall(A,C);
        mWalls[1] = new Wall(C,E);
        mWalls[2] = new Wall(G,H);
        mWalls[3] = new Wall(H,I);
        mWalls[4] = new Wall(I,J);
        mWalls[5] = new Wall(J,F);
        mWalls[6] = new Wall(F,D);
        mWalls[7] = new Wall(D,B);
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
        double x_trans = c.coordx-(size/2)-moveX;
        double y_trans = c.coordy-(size/2)-moveY;
        double x1 = (Math.cos(-degreeRadians)*x_trans)-(Math.sin(-degreeRadians)*y_trans);
        double y1 = (Math.sin(-degreeRadians)*x_trans)+(Math.cos(-degreeRadians)*y_trans);
        return new Coordinate(x1+(size/2)+moveX,y1+(size/2)+moveY);
    }

    private Coordinate rotateCoord2(Coordinate c, int degrees, int size){
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
            A = rotateCoord2(A, mDegreeRotation, height);
            B = rotateCoord2(B, mDegreeRotation, height);
            C = rotateCoord2(C, mDegreeRotation, height);
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
        mPaint.setColor(mStrokeColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.translate(moveX,moveY);
        if(userLocked){
            canvas.rotate((float)mDegreeRotation,(height/2)-moveX,(width/2)-moveY);
        }
        else{
            canvas.rotate((float)-mDegreeRotation,(height/2)-moveX,(width/2)-moveY);
        }
        canvas.drawColor(mBackgroundColor);
        /*for(int i=0; i < mWalls.length; i++){
            if(!userLocked){
                Coordinate start = new Coordinate(mWalls[i].startX,mWalls[i].startY);
                Coordinate end = new Coordinate(mWalls[i].endX,mWalls[i].endY);
                start = rotateCoord(start,-mDegreeRotation,height);
                end = rotateCoord(end,-mDegreeRotation,height);
                canvas.drawLine((float)start.coordx,(float)start.coordy,(float)end.coordx,(float)end.coordy,mPaint);
            }
            else{
                canvas.drawLine(mWalls[i].startX,mWalls[i].startY,mWalls[i].endX,mWalls[i].endY,mPaint);
            }
        }*/
        for(int i=0;i < mDWalls.size(); i++){
            canvas.drawLine(mDWalls.get(i).startX,mDWalls.get(i).startY,mDWalls.get(i).endX,mDWalls.get(i).endY,mPaint);
        }
        mPaint.setColor(Color.RED);
        Path newPath = constructUser();
        canvas.drawPath(newPath,mPaint);
    }

    public void setWallArray(Wall[] walls){
        mWalls = walls;
    }

    public void setDynamicWallArray(ArrayList<Wall> walls){
        mDWalls = walls;
    }

    public Wall[] getWallArray(){return mWalls;}

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
