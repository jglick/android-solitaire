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
            Paint paint;
            Model model = new Model();
            int gridWidth, gridHeight;
            float textWidth, textHeight;
            protected @Override void onAttachedToWindow() {
                super.onAttachedToWindow();
                setFocusable(true);
                paint = new Paint();
                paint.setColor(0xFFFFFFFF);
                float[] widths = new float[Model.RANKS.length];
                paint.getTextWidths(Model.RANKS, 0, Model.RANKS.length, widths);
                textHeight = paint.getTextSize();
                textWidth = 0;
                for (float w : widths) {
                    textWidth = Math.max(w, textWidth);
                }
            }
            protected @Override void onSizeChanged(int width, int height, int oldw, int oldh) {
                super.onSizeChanged(width, height, oldw, oldh);
                gridWidth = (int) (width / textWidth);
                gridHeight = (int) (height / textHeight);
                Log.v("solitaire", "grid: " + gridWidth + "x" + gridHeight + "; " + model);
            }
            private int[] model2Grid(int x, int y) {
                return new int[] {gridWidth / 2 + x - y, x + y};
            }
            private int[] grid2Model(int x, int y) {
                int mx2 = x + y - gridWidth / 2;
                if (mx2 % 2 == 1) {
                    return null;
                }
                int gx = mx2 / 2;
                int gy = gridWidth / 2 - x + y;
                if (gx < 0 || gy < 0 || gx >= model.matrixBound() || gy >= model.matrixBound()) {
                    return null;
                }
                return new int[] {gx, gy};
            }
            private void drawChar(Canvas canvas, char c, int gx, int gy) {
                canvas.drawText(new String(new char[] {c}), textWidth * gx, textHeight * gy, paint);
            }
            protected @Override void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                // A.0.9
                // .1.1.
                // 2.2.2
                //y.....x
                // .....
                drawChar(canvas, Model.RANKS[model.flipped()], 0, 0);
                int[] size = model.matrixSize();
                for (int x = 0; x < size[0]; x++) {
                    for (int y = 0; y < size[1]; y++) {
                        byte card = model.read(x, y);
                        if (card > 0) {
                            int[] xy = model2Grid(x, y);
                            if (xy[0] < gridWidth && xy[1] < gridHeight) {
                                drawChar(canvas, Model.RANKS[card], xy[0], xy[1]);
                            }
                        }
                    }
                }
                int remaining = model.deckRemaining();
                String remS = remaining == 0 ? "END" : String.valueOf(remaining);
                for (int x = 0; x < remS.length(); x++) {
                    drawChar(canvas, remS.charAt(x), gridWidth - remS.length() + x, 0);
                }
            }
            public @Override boolean onTouchEvent(MotionEvent me) {
                int gx = (int) (me.getX() / textWidth);
                int gy = (int) (me.getY() / textHeight);
                if (gx == 0 && gy == 0) {
                    model.flip();
                    Log.v("solitaire", "flip => " + Model.RANKS[model.flipped()]);
                    invalidate();
                } else {
                    int[] xy = grid2Model(gx, gy);
                    if (xy != null) {
                        int mx = xy[0];
                        int my = xy[1];
                        if (model.canAcquire(mx, my)) {
                            Log.v("solitaire", "acquire " + Model.RANKS[model.read(mx, my)] + " @" + mx + "," + my);
                            model.acquire(mx, my);
                            invalidate();
                        } else {
                            Log.v("solitaire", "cannot acquire " + Model.RANKS[model.read(mx, my)] + " @" + mx + "," + my);
                            // XXX see if we can move it
                        }
                    }
                }
                return true;
            }
        });
    }
}
