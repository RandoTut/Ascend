package com.siegeengine.ascend;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Ascend extends Activity {

    //Define the variables
    int noOfHorizontalPixels,noOfVerticalPixels;
    int gridWidth=6,gridHeight,blockSize,centreDistance;
    int level,prevCircleTouched;
    List<Integer> centreListX,centreListY,radii;
    List<Integer> circleFlags;
    boolean levelComplete,restartGame;

    Random  random  =   new Random();

    //Declare the objects for drawing
    Bitmap      blankBitmap;
    Canvas      gameCanvas;
    ImageView   gameView;
    Paint       paint;

    //Initialise the variables for maths calculations
    void intialiseMathsVariables(){
        //Co-ordinates store initialise
        centreListX =   new ArrayList<>(2);
        centreListY =   new ArrayList<>(2);
        radii       =   new ArrayList<>(2);
        circleFlags =   new ArrayList<>(2);

        circleFlags.add(0,0);
        circleFlags.add(1,0);
        circleFlags.add(2,0);

        prevCircleTouched=3;

        levelComplete = false;
    }

    //reset variables for a game restart
    void resetTheMathsVariables(){
        restartGame=false;
        level=0;
    }

    //Initialise the graphics variables
    void initialiseGraphicsVariables(){
        //Get the display size parameters
        Display display         =   getWindowManager().getDefaultDisplay();     //Display object
        Point windowSize        =   new Point();        //Variable to store the X & Y display size

        display.getSize(windowSize);

        noOfHorizontalPixels    =   windowSize.x;
        noOfVerticalPixels      =   windowSize.y;

        Log.d("DBG-Window Size is ",noOfHorizontalPixels+"x"+noOfVerticalPixels);

        //Setup the drawing background and view
        blankBitmap     =   Bitmap.createBitmap(noOfHorizontalPixels,noOfVerticalPixels,Bitmap.Config.ARGB_8888);
        gameCanvas      =   new Canvas(blankBitmap);

        //Make the canvas white
        gameCanvas.drawColor(Color.argb(255,255,255,255));

        //Initialise the paint
        paint           =   new Paint();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       initialiseGraphicsVariables();

        //Set the ImageView as the content view
        gameView        =   new ImageView(this);
        setContentView(gameView);

        //Call the new game method
        level   =   0;
        newLevel();

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);

        if ((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            //check if the level is finished
            if(!restartGame) {
                if (!levelComplete) {
                    detectTouchedCircle((int) motionEvent.getX(), (int) motionEvent.getY());
                } else {
                    newLevel();        //if current level finished start new level
                }
            }
            else {
                resetTheMathsVariables();
                newLevel();
            }

        }
        return true;
    }

    //This is where each game starts
    void newLevel(){

        intialiseMathsVariables();

        Log.d("DBG-","Inside the newGame()");
        //Wipe the bitmap clean with white
        gameCanvas.drawColor(Color.argb(255,255,255,255));

        //Draw the circles
        drawCircles();
        gameView.setImageBitmap(blankBitmap);

    }

    //This is where we draw our circles
    void drawCircles(){
        //Log.d("DBG-","Inside drawCircles()");
        //Initialise the radii
        blockSize   =   noOfHorizontalPixels/gridWidth;
        gridHeight  =   noOfVerticalPixels/blockSize;

        //Set the radius of all three circles
        radii.add(0,blockSize);
        radii.add(1,(int)(blockSize*0.8));
        radii.add(2,(int)(blockSize*0.6));

        //Calculate centre points so as not to overlap
        for(int i = 0;i<3;i++){

            //get new coordinates - and calculate the distance to the previous one
            centreListX.add(i, (random.nextInt(gridWidth-1)+1)*blockSize);
            centreListY.add(i, (random.nextInt(gridHeight-1)+1)*blockSize);

            if(i==1) {
                //Distance between new coordinate and previous must be higher than the sum of radius
                do {
                    centreListX.add(i, (random.nextInt(gridWidth - 1) + 1) * blockSize);
                    centreListY.add(i, (random.nextInt(gridHeight - 1) + 1) * blockSize);
                    //Log.d("DBG- while loop 1", "CD:" + centreDistance);
                } while (doTheyOverlap(i - 1, i));
            }

            if(i==2){
                do {
                    do {
                        centreListX.add(i, (random.nextInt(gridWidth - 1) + 1) * blockSize);
                        centreListY.add(i, (random.nextInt(gridHeight - 1) + 1) * blockSize);
                        //Log.d("DBG- while loop 2", "CD:" + centreDistance);
                    }while ( doTheyOverlap(i-1,i));
                } while ( doTheyOverlap(i-2,i));
            }
        }

        //Draw circles with calculated centre coordinates
        for (int i=0;i<3;i++){
            //Get a random colour, Draw the circle
            paint.setColor(Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            gameCanvas.drawCircle(centreListX.get(i), centreListY.get(i), radii.get(i), paint);
        }
    }

    //calculate if two circles overlap
    boolean doTheyOverlap(int p, int q){
        centreDistance
                = (int) Math.sqrt((centreListX.get(p)-centreListX.get(q))*(centreListX.get(p)-centreListX.get(q))
                +
                (centreListY.get(p)-centreListY.get(q))*(centreListY.get(p)-centreListY.get(q)));

        //Log.d("DBG- overlap? CD"," "+centreDistance);
        //Log.d("DBG- overlap? +Rs"," "+(radii.get(p)+radii.get(q)));

        return centreDistance < (radii.get(p) + radii.get(q));
    }

    //Identify if a circle is touched
    void detectTouchedCircle(int x, int y){
        //Check if the touched point is inside a circle, (x-a)^2+(y-b)^2=r^2
        for (int i=0;i<3;i++){
            //Log.d("DBG-"," "+i);
            //Log.d("DBG-"," "+((x-centreListX.get(i))*(x-centreListX.get(i))+(y-centreListY.get(i))*(y-centreListY.get(i))));
           // Log.d("DBG-"," "+(radii.get(i)*radii.get(i)));
            if((x-centreListX.get(i))*(x-centreListX.get(i))+(y-centreListY.get(i))*(y-centreListY.get(i))
                <=(radii.get(i)*radii.get(i))){
                colourTheCircleTouched(i);
            }
        }
    }

    /*Give colour to the circle once it is selected, and
if all three are selected start a new game*/
    void colourTheCircleTouched(int circleTouched){
        paint.setColor(Color.BLACK);
        gameCanvas.drawCircle(centreListX.get(circleTouched),centreListY.get(circleTouched),radii.get(circleTouched),paint);
        gameView.setImageBitmap(blankBitmap);

        //Flag up for the circle so we can detect later if all had been touched
        circleFlags.set(circleTouched,1);
        //Log.d("DBG-","F0 "+circleFlags.get(0)+" F1 "+ circleFlags.get(1)+" F2 "+circleFlags.get(2)+"  No:"+circleTouched);
        Log.d("DBG-","Prev circle :"+prevCircleTouched+" Cir touched : "+circleTouched);

        //Check the current touched circle is bigger than the previously touched one
        if(circleTouched<prevCircleTouched){
            //check if all circles are selected
            if ( (circleFlags.get(0)+ circleFlags.get(1)+ circleFlags.get(2))==3){
                level++;
                levelComplete =true;
                updateScore();
            }
            else {
                levelComplete =false;
            }
            prevCircleTouched=circleTouched;
            restartGame=false;
        }
        else {
            restartGame=true;
            endOfGame();
        }
    }

    //Update the game score
    void updateScore(){
        gameCanvas.drawColor(Color.argb(130,255,250,250));
        paint.setTextSize(50);
        paint.setColor(Color.GREEN);
        gameCanvas.drawText("Level Complete",50,50,paint);
        gameCanvas.drawText("SCORE : "+level,(noOfHorizontalPixels/2),(noOfVerticalPixels-50),paint);
        gameCanvas.drawText("[Touch anywhere for next level]",50,(noOfVerticalPixels-50),paint);
    }

    //Game finishes
    void endOfGame(){
        gameCanvas.drawColor(Color.argb(130,255,250,250));
        paint.setTextSize(50);
        paint.setColor(Color.RED);
        gameCanvas.drawText("FAILED",50,50,paint);
        gameCanvas.drawText("SCORE : "+level,(noOfHorizontalPixels/2),(noOfVerticalPixels-50),paint);
        gameCanvas.drawText("[Touch anywhere to restart]",50,(noOfVerticalPixels-50),paint);
    }

}
