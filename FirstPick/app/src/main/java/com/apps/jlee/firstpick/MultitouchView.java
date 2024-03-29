package com.apps.jlee.firstpick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//http://www.vogella.com/tutorials/AndroidTouch/article.html
public class MultitouchView extends View
{
    private SparseArray<PointF> mActivePointers;
    private ArrayList<Integer> list;
    private Map<Integer,Integer> colorMap;
    private Paint mPaint, textPaint;
    private CountDownTimer cTimer;
    private RandomColors randomColors;

    private static final int CIRCLE_SIZE = 250;
    private static final long startTime = 800;
    final Random mRandom = new Random(System.currentTimeMillis());
    private String text = "Place Fingers Here";
    private boolean isReset = true;

    public MultitouchView(Context context)
    {
        super(context);
        initView();
    }

    /*Intialize data structures and instantiate objects*/
    private void initView()
    {
        mActivePointers = new SparseArray<PointF>();
        list = new ArrayList<Integer>(10);
        colorMap = new HashMap<Integer,Integer>();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(125);
        textPaint.setColor(Color.WHITE);

        randomColors = new RandomColors();
    }

    /*
    ACTION_DOWN is for the first finger that touches the screen. This starts the gesture. The pointer data for this finger is always at index 0 in the MotionEvent.
    ACTION_POINTER_DOWN is for extra fingers that enter the screen beyond the first. The pointer data for this finger is at the index returned by getActionIndex().
    ACTION_POINTER_UP is sent when a finger leaves the screen but at least one finger is still touching it. The last data sample about the finger that went up is at the index returned by getActionIndex().
    ACTION_UP is sent when the last finger leaves the screen. The last data sample about the finger that went up is at index 0. This ends the gesture.
    ACTION_CANCEL means the entire gesture was aborted for some reason. This ends the gesture.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // get pointer index of the touch
        int pointerIndex = event.getActionIndex();
        // get pointer ID of the touch
        int pointerId = event.getPointerId(pointerIndex);
        //return an action such as ACTION_DOWN,ACTION_POINTER_DOWN and others.
        int maskedAction = MotionEventCompat.getActionMasked(event);

        switch (maskedAction)
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            {
                // A new finger has touched screen, add it to the list of pointers
                PointF f = new PointF();
                f.x = event.getX(pointerIndex);
                f.y = event.getY(pointerIndex);

                mActivePointers.put(pointerId, f);
                list.add(pointerId);
                colorMap.put(pointerId,-1);

                cancelTimer();

                break;
            }
            case MotionEvent.ACTION_MOVE:
            {   // Loops through all the pointers that were moved
                for (int size = event.getPointerCount(), i = 0; i < size; i++)
                {
                    PointF point = mActivePointers.get(event.getPointerId(i));
                    if (point != null)
                    {
                        point.x = event.getX(i);
                        point.y = event.getY(i);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            {
                removeFinger(pointerId);
                //Log.v("Lakers","ACTION_POINTER_UP Called");
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            {
//                removeFinger(pointerId);
//                Log.v("Lakers","ACTION_CANCEL Called");
//                break;
            }
            default :
                return super.onTouchEvent(event);
        }
        //calls the onDraw method so the canvas will redraw everytime this is called.
        invalidate();

        if(mActivePointers.size() > 1 && isReset)
            startTimer();

        return true;
    }
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        // draw all pointers
        for (int size = mActivePointers.size(), i = 0; i < size; i++)
        {
            PointF point = mActivePointers.valueAt(i);
            int pointerID_key = mActivePointers.keyAt(i);

            if(colorMap.get(pointerID_key) == -1)
            {
                //colorMap.put(pointerID_key,randomColors.getColor());
                colorMap.put(pointerID_key,((int)(Math.random()*16777215)) | (0xFF << 24));
                //colorMap.put(pointerID_key,generateRandomColor());
            }

            mPaint.setColor(colorMap.get(pointerID_key));
            canvas.drawCircle(point.x, point.y, CIRCLE_SIZE, mPaint);
        }
        //canvas.drawText("Total pointers: " + mActivePointers.size(), 10, 50, textPaint);

        /*Draws the Place Fingers Here text on screen*/
        if(mActivePointers.size() == 0)
        {
            Rect r = new Rect();
            canvas.getClipBounds(r);
            int cHeight = r.height();
            int cWidth = r.width();

            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.getTextBounds(text, 0, text.length(), r);
            float x = cWidth / 2f - r.width() / 2f - r.left;
            float y = cHeight / 2f + r.height() / 2f - r.bottom;
            canvas.drawText(text, x, y, textPaint);
        }
    }

    public void startTimer()
    {
        //First arg is the number of seconds to start counting down from
        //Second arg is how times onTick() method is called. Since we dont use it, it doesnt matter what we put.
        cTimer = new CountDownTimer(startTime, 800)
        {
            public void onTick(long millisUntilFinished){}
            public void onFinish()
            {
                Random rand = new Random();
                while (mActivePointers.size() > 1)
                {
                    int index = rand.nextInt(mActivePointers.size());
                    mActivePointers.remove(list.remove(index));
                    invalidate();
                }
            }
        };
        cTimer.start();
        isReset = false;
    }

    //cancel timer
    public void cancelTimer()
    {
        if (cTimer != null)
        {
            cTimer.cancel();
            isReset = true;
        }
    }

    public void removeFinger(int pointerId)
    {
        mActivePointers.remove(pointerId);
        if(list.contains(pointerId))
            list.remove(list.indexOf(pointerId));
        colorMap.remove(pointerId);
        cancelTimer();
    }

    public int generateRandomColor() {
        // This is the base color which will be mixed with the generated one
        final int baseColor = Color.WHITE;

        final int baseRed = Color.red(baseColor);
        final int baseGreen = Color.green(baseColor);
        final int baseBlue = Color.blue(baseColor);

        final int red = (baseRed + mRandom.nextInt(256)) / 2;
        final int green = (baseGreen + mRandom.nextInt(256)) / 2;
        final int blue = (baseBlue + mRandom.nextInt(256)) / 2;

        return Color.rgb(red, green, blue);
    }
}