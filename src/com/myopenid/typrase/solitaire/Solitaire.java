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
                int my2 = gridWidth / 2 - x + y;
                if (mx2 % 2 != 0 || my2 % 2 != 0) {
                    return null;
                }
                int mx = mx2 >>> 1;
                int my = my2 >>> 1;
                if (mx < 0 || my < 0 || mx >= model.matrixBound() || my >= model.matrixBound()) {
                    return null;
                }
                return new int[] {mx, my};
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
                drawChar(canvas, Model.RANKS[model.flipped()], 0, 5);
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
                    drawChar(canvas, remS.charAt(x), gridWidth - remS.length() + x, 5);
                }
//                Paint red = new Paint();
//                red.setColor(0xFFFF0000);
//                canvas.drawLine(cursorX - 5, cursorY - 5, cursorX + 5, cursorY + 5, red);
//                canvas.drawLine(cursorX - 5, cursorY + 5, cursorX + 5, cursorY - 5, red);
            }
            public @Override boolean onTouchEvent(MotionEvent me) {
                if (me.getAction() != MotionEvent.ACTION_DOWN) {
                    return false;
                }
                int gx = (int) (me.getX() / textWidth);
                int gy = (int) (me.getY() / textHeight) + 1;
                Log.v("solitaire", "touched at grid " + gx + "," + gy);
                if (gx == 0 && gy == 5) {
                    if (model.deckRemaining() == 0) {
                        model = new Model();
                    } else {
                        model.flip();
                        Log.v("solitaire", "flip => " + Model.RANKS[model.flipped()]);
                    }
                } else {
                    int[] xy = grid2Model(gx, gy);
                    if (xy != null) {
                        int mx = xy[0];
                        int my = xy[1];
                        if (model.canAcquire(mx, my)) {
                            Log.v("solitaire", "acquire " + Model.RANKS[model.read(mx, my)] + " @" + mx + "," + my);
                            model.acquire(mx, my);
                        } else {
                            Log.v("solitaire", "cannot acquire " + Model.RANKS[model.read(mx, my)] + " @" + mx + "," + my);
                            // XXX see if we can move it
                        }
                    }
                }
                invalidate();
                return true;
            }
        });
    }
}
