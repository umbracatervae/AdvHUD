package com.projecttango.examples.java.hellomotiontracking;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Created by Akshay on 2/15/2017.
 */
public class LineSeqDrawable extends Drawable {
    private int mBackgroundColor;
    private int mStrokeWidth;
    private int mStrokeColor;
    private int mDegreeRotation;
    public Wall[] mWalls;

    public LineSeqDrawable(){
        mBackgroundColor = Color.DKGRAY;
        mStrokeColor = Color.BLUE;
        mStrokeWidth = 10;
        mDegreeRotation = 0;
    }

    public LineSeqDrawable(int backgroundColor, int strokeColor, int strokeWidth, Wall[] walls){
        mBackgroundColor = backgroundColor;
        mStrokeColor = strokeColor;
        mStrokeWidth = strokeWidth;
        mWalls = walls;
        mDegreeRotation = 0;
    }

    private Path constructUser(){
        double degreeRadians = Math.toRadians(mDegreeRotation);
        double x1 = (Math.cos(-degreeRadians)*-10)-(Math.sin(-degreeRadians)*10);
        double y1 = (Math.sin(-degreeRadians)*-10)+(Math.cos(-degreeRadians)*10);
        double x2 = (Math.cos(-degreeRadians)*0)-(Math.sin(-degreeRadians)*-10);
        double y2 = (Math.sin(-degreeRadians)*0)+(Math.cos(-degreeRadians)*-10);
        double x3 = (Math.cos(-degreeRadians)*10)-(Math.sin(-degreeRadians)*10);
        double y3 = (Math.sin(-degreeRadians)*10)+(Math.cos(-degreeRadians)*10);
        Coordinate A = new Coordinate(x1+150,y1+150);
        Coordinate B = new Coordinate(x2+150,y2+150);
        Coordinate C = new Coordinate(x3+150,y3+150);
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
        canvas.rotate((float)mDegreeRotation,150,150);
        canvas.drawColor(mBackgroundColor);
        for(int i=0; i < mWalls.length; i++){
            canvas.drawLine(mWalls[i].startX,mWalls[i].startY,mWalls[i].endX,mWalls[i].endY,mPaint);
        }
        mPaint.setColor(Color.RED);
        Path newPath = constructUser();
        canvas.drawPath(newPath,mPaint);
    }

    public void setWallArray(Wall[] walls){
        mWalls = walls;
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
        return 300;
    }

    @Override
    public int getIntrinsicWidth() {
        return 300;
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
