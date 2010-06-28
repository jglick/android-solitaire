package com.myopenid.typrase.solitaire;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class Solitaire extends Activity {
    public @Override void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new View(this) {
            float x, y;
            Paint paint = new Paint();
            {
                paint.setColor(0xFFFF0080);
                setFocusable(true);
            }
            public @Override boolean onTouchEvent(MotionEvent me) {
                x = me.getX();
                y = me.getY();
                Log.i("test", "touch @" + x  + "," + y);
                invalidate();
                return true;
            }
            public @Override boolean onTrackballEvent(MotionEvent event) {
                float xi = event.getX();
                x += xi;
                float yi = event.getY();
                y += yi;
                Log.i("test", "trackball +" + xi + "," + yi + " action=" + event.getAction());
                invalidate();
                return true;
            }
            protected @Override void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                canvas.drawText("here", x, y, paint);
            }
        });
    }
}
