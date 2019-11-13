package com.apps.jlee.firstpick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.CountDownTimer;
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
    SparseArray<PointF> mActivePointers;
    ArrayList<Integer> list;
    Map<Integer,Integer> colorMap;
    Paint mPaint, textPaint;
    CountDownTimer cTimer = null;
    static final int CIRCLE_SIZE = 250;
    int fingers = 0;

    public MultitouchView(Context context)
    {
        super(context);
        initView();
    }

    private void initView()
    {
        mActivePointers = new SparseArray<PointF>();
        list = new ArrayList<Integer>(10);
        colorMap = new HashMap<Integer,Integer>();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(60);
    }

    public boolean onTouchEvent(MotionEvent event)
    {
        // get pointer index of the touch
        int pointerIndex = event.getActionIndex();
        // get pointer ID of the touch
        int pointerId = event.getPointerId(pointerIndex);
        //return an action such as ACTION_DOWN,ACTION_POINTER_DOWN and others.
        int maskedAction = event.getActionMasked();

        switch (maskedAction)
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            {
                // We have a new pointer. Lets add it to the list of pointers
                PointF f = new PointF();
                f.x = event.getX(pointerIndex);
                f.y = event.getY(pointerIndex);

                mActivePointers.put(pointerId, f);
                list.add(pointerId);
                colorMap.put(pointerId,-1);

                break;
            }
            case MotionEvent.ACTION_MOVE:
            { // a pointer was moved
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
            case MotionEvent.ACTION_CANCEL:
            {
                mActivePointers.remove(pointerId);
                if(list.contains(pointerId))
                    list.remove(list.indexOf(pointerId));
                colorMap.remove(pointerId);

                break;
            }
        }
        //calls the onDraw method so the canvas will redraw everytime this is called.
        invalidate();

        if(fingers != mActivePointers.size())
        {
            fingers = mActivePointers.size();
            cancelTimer();
        }
        if(mActivePointers.size() > 1)
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
                colorMap.put(pointerID_key,((int)(Math.random()*16777215)) | (0xFF << 24));

            mPaint.setColor(colorMap.get(pointerID_key));
            canvas.drawCircle(point.x, point.y, CIRCLE_SIZE, mPaint);
        }
        //canvas.drawText("Total pointers: " + mActivePointers.size(), 10, 50, textPaint);
    }
    void startTimer()
    {
        cTimer = new CountDownTimer(1000, 1000)
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
    }
    //cancel timer
    void cancelTimer()
    {
        if (cTimer != null)
            cTimer.cancel();
    }
}