package com.example.jlee.touchselector;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Two ways to launch the multitouchview:
        // setContentView(R.layout.multi_touch_activity);
        setContentView(new MultitouchView(this));
    }
}
