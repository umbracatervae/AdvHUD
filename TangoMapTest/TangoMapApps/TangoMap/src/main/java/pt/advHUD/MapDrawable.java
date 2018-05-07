package pt.advHUD;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    //Friendly User Information
    public int fx = 0;
    public int fy = 0;
    public int fdegree = 0;

    private boolean userLocked = true; //USE ONLY USER USER LOCKED MODE
    private boolean multipleMode = false;
    public Wall[] mWalls;
    ArrayList<Coordinate> mPathHistory;

    public MapDrawable(){
        mBackgroundColor = Color.DKGRAY;
        mStrokeColor = Color.BLUE;
        mStrokeWidth = 10;
        mDegreeRotation = 0;
        mPathHistory = new ArrayList<Coordinate>();
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
        mPathHistory = new ArrayList<Coordinate>();
    }

    public void defaultSetup(){
        mBackgroundColor = Color.DKGRAY;
        mStrokeColor = Color.LTGRAY;
        mStrokeWidth = 10;
        mDegreeRotation = 0;
        mPathHistory = new ArrayList<Coordinate>();

        //Demo Wall Map
        mWalls = new Wall[8];
        Coordinate A = new Coordinate(90,400);
        Coordinate B = new Coordinate(175,400);
        Coordinate C = new Coordinate(90,140);
        Coordinate D = new Coordinate(175,190);
        Coordinate E = new Coordinate(-100,140);
        Coordinate F = new Coordinate(270,190);
        Coordinate G = new Coordinate(-100,90);
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
        double x_trans = c.coordx-(size/2)+moveX;
        double y_trans = c.coordy-(size/2)+moveY;
        double x1 = (Math.cos(-degreeRadians)*x_trans)-(Math.sin(-degreeRadians)*y_trans);
        double y1 = (Math.sin(-degreeRadians)*x_trans)+(Math.cos(-degreeRadians)*y_trans);
        return new Coordinate(x1+(size/2)-moveX,y1+(size/2)-moveY);
    }

    private Coordinate rotateCoord_friendly(Coordinate c, int degrees, int size){
        double degreeRadians = Math.toRadians(degrees);
        double x_trans = c.coordx-(size/2)+fx;
        double y_trans = c.coordy-(size/2)+fy;
        double x1 = (Math.cos(-degreeRadians)*x_trans)-(Math.sin(-degreeRadians)*y_trans);
        double y1 = (Math.sin(-degreeRadians)*x_trans)+(Math.cos(-degreeRadians)*y_trans);
        return new Coordinate(x1+(size/2)-fx,y1+(size/2)-fy);
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

    private Path constructFriendlyUser(){
        Coordinate A = new Coordinate(140,160);
        Coordinate B = new Coordinate(150,140);
        Coordinate C = new Coordinate(160,160);
        if(userLocked) {
            A = new Coordinate(140-fx,160-fy);
            B = new Coordinate(150-fx,140-fy);
            C = new Coordinate(160-fx,160-fy);
            A = rotateCoord_friendly(A, fdegree, height);
            B = rotateCoord_friendly(B, fdegree, height);
            C = rotateCoord_friendly(C, fdegree, height);
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
        canvas.rotate((float)mDegreeRotation,(height/2)-moveX,(width/2)-moveY);
        canvas.drawColor(mBackgroundColor);
        for(int i=0; i < mWalls.length; i++){
            canvas.drawLine(mWalls[i].startX,mWalls[i].startY,mWalls[i].endX,mWalls[i].endY,mPaint);
        }
        if(mPathHistory != null){
            for(int i=0; i < mPathHistory.size(); i++){
                canvas.drawPoint((float)mPathHistory.get(i).coordx,(float)mPathHistory.get(i).coordy,pathPaint);
            }
        }
        mPaint.setColor(Color.RED);
        Path newPath = constructUser();
        canvas.drawPath(newPath,mPaint);
        if(multipleMode){
            mPaint.setColor(Color.BLUE);
            Path friendPath = constructFriendlyUser();
            canvas.drawPath(friendPath,mPaint);
        }
    }

    public void setMultipleMode(boolean mode){
        multipleMode = mode;
    }

    public void setWallArray(Wall[] walls){
        mWalls = walls;
    }

    public void appendPathPoint(Coordinate c){
        mPathHistory.add(c);
    }

    public Wall[] getWallArray(){return mWalls;}

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    public void setStrokeColor(int color){
        mStrokeColor = color;
    }

    public void setDegreeRotation(int degreeRotation){mDegreeRotation = degreeRotation;}

    public float[] extractTransOrient(File fData, BufferedReader reader){
        float[] result = new float[7];
        if(fData != null){
            try {
                String line;
                Pattern pt = Pattern.compile("^Position\\:\\s*(\\-?\\d{1}\\.{1}\\d*\\w?\\-?\\d?)\\,\\s*(\\-?\\d{1}\\.{1}\\d*\\w?\\-?\\d?)\\,\\s*(\\-?\\d{1}\\.{1}\\d*\\w?\\-?\\d?)\\.{1}");
                Pattern po = Pattern.compile("Orientation\\:\\s*(\\-?\\d{1}\\.{1}\\d*\\w?\\-?\\d?)\\,\\s*(\\-?\\d{1}\\.{1}\\d*\\w?\\-?\\d?)\\,\\s*(\\-?\\d{1}\\.{1}\\d*\\w?\\-?\\d?)\\,\\s*(\\-?\\d{1}\\.{1}\\d*\\w?\\-?\\d?)$");
                line = reader.readLine();
                if(line == null){
                    reader.close();
                }
                if(line != null){
                    Matcher m1 = pt.matcher(line);
                    Matcher m2 = po.matcher(line);
                    if (m1.find()){
                        result[0] = Float.valueOf(m1.group(1));
                        result[1] = Float.valueOf(m1.group(2));
                        result[2] = Float.valueOf(m1.group(3));
                        StringBuilder stringBuilder1 = new StringBuilder();
                        stringBuilder1.append("XAM: " + String.valueOf(result[0])+", YAM: "+ String.valueOf(result[1])+" , ZAM: "+ String.valueOf(result[2])+ "\n");
                        Log.i(TangoMainActivity.class.getSimpleName(),stringBuilder1.toString());
                    }
                    if (m2.find()){
                        result[3] = Float.valueOf(m2.group(1));
                        result[4] = Float.valueOf(m2.group(2));
                        result[5] = Float.valueOf(m2.group(3));
                        result[6] = Float.valueOf(m2.group(3));
                        //StringBuilder stringBuilder2 = new StringBuilder();
                        //stringBuilder2.append("OR0: " + m2.group(1)+", OR1: "+ m2.group(2)+" , OR2: "+ m2.group(3)+ " , OR3: "+ m2.group(4)+"\n");
                        //Log.i(TangoMainActivity.class.getSimpleName(),stringBuilder2.toString());
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

    public void updateFriendlyInfo(File rFile, BufferedReader rReader){
        //Updates translational and rotational information for friendly arrow
        float[] f = extractTransOrient(rFile,rReader);
        //translation information (x & y)
        fx = (int)(f[0]*-25);
        fy = (int)(f[1]*25);

        float qw = f[3];
        float qx = f[4];
        float qy = f[5];
        float qz = f[6];
        float[] rotMatrix = new float[9];
        float[] euOrient = new float[3];
        //Extract Rotation Matrix
        rotMatrix[0] = 1-2*(qy*qy)-2*(qz*qz);
        rotMatrix[1] = (2*qx*qy)+(2*qz*qw);
        rotMatrix[2] = (2*qx*qz)-(2*qy*qw);
        rotMatrix[3] = (2*qx*qy)-(2*qz*qw);
        rotMatrix[4] = 1-(2*qx*qx)-(2*qz*qz);
        rotMatrix[5] = (2*qy*qz)+(2*qx*qw);
        rotMatrix[6] = (2*qx*qz)+(2*qy*qw);
        rotMatrix[7] = (2*qy*qz)-(2*qx*qw);
        rotMatrix[8] = 1-(2*qx*qx)-(2*qy*qy);
        //Get orientation information
        SensorManager.getOrientation(rotMatrix,euOrient);
        fdegree = (int)(Math.toDegrees(euOrient[2])*-1);
    }

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
