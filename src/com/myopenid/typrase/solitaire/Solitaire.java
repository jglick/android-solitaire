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
            Paint regularPaint, hotPaint, cursorPaint;
            Model model;
            int gridWidth, gridHeight;
            float textWidth, textHeight;
            int hotX, hotY;
            float cursorX, cursorY;
            private void init() {
                model = new Model();
                hotX = hotY = -1;
            }
            protected @Override void onAttachedToWindow() {
                super.onAttachedToWindow();
                setFocusable(true);
                regularPaint = new Paint();
                regularPaint.setColor(0xFFFFFFFF);
                regularPaint.setTextSize(15);
                regularPaint.setAntiAlias(true);
                hotPaint = new Paint();
                hotPaint.setColor(0xFFFF0000);
                hotPaint.setTextSize(regularPaint.getTextSize());
                hotPaint.setAntiAlias(true);
                cursorPaint = new Paint();
                cursorPaint.setColor(0xFF00FF00);
                cursorPaint.setAntiAlias(true);
                float[] widths = new float[Model.RANKS.length];
                regularPaint.getTextWidths(Model.RANKS, 0, Model.RANKS.length, widths);
                textHeight = regularPaint.getTextSize();
                textWidth = 0;
                for (float w : widths) {
                    textWidth = Math.max(w, textWidth);
                }
                init();
            }
            protected @Override void onSizeChanged(int width, int height, int oldw, int oldh) {
                super.onSizeChanged(width, height, oldw, oldh);
                gridWidth = (int) (width / textWidth);
                gridHeight = (int) (height / textHeight);
                cursorX = width / 2;
                cursorY = height / 2;
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
            private void drawChar(Canvas canvas, char c, int gx, int gy, Paint paint) {
                if (c == 'X') {
                    canvas.drawText("10", textWidth * gx - textWidth / 2, textHeight * gy, paint);
                } else {
                    canvas.drawText(new String(new char[] {c}), textWidth * gx + (c == 'J' ? textWidth * .2f : 0), textHeight * gy, paint);
                }
            }
            protected @Override void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                // A.0.9
                // .1.1.
                // 2.2.2
                //y.....x
                // .....
                int[] cursorXY = snapCursor();
                // Center offsets determined by trial and error acc. to typical font and size:
                canvas.drawCircle(textWidth * (cursorXY[0] + .45f), textHeight * (cursorXY[1] - .3f), (textWidth + textHeight) / 2, cursorPaint);
                drawChar(canvas, Model.RANKS[model.flipped()], 1, 3, regularPaint);
                int[] size = model.matrixSize();
                for (int x = 0; x < size[0]; x++) {
                    for (int y = 0; y < size[1]; y++) {
                        byte card = model.read(x, y);
                        if (card > 0) {
                            int[] xy = model2Grid(x, y);
                            if (xy[0] < gridWidth && xy[1] < gridHeight) {
                                drawChar(canvas, Model.RANKS[card], xy[0], xy[1], x == hotX && y == hotY ? hotPaint : regularPaint);
                            }
                        }
                    }
                }
                int remaining = model.deckRemaining();
                String remS = remaining == 0 ? "END" : String.valueOf(remaining);
                for (int x = 0; x < remS.length(); x++) {
                    drawChar(canvas, remS.charAt(x), gridWidth - remS.length() + x - 1, 3, regularPaint);
                }
                drawChar(canvas, 'L', 1, 1, regularPaint);
                drawChar(canvas, 'o', 2, 1, regularPaint);
                drawChar(canvas, 'v', 3, 1, regularPaint);
                drawChar(canvas, 'e', 4, 1, regularPaint);
                drawChar(canvas, ',', 5, 1, regularPaint);
                drawChar(canvas, 'J', gridWidth - 6, 1, regularPaint);
                drawChar(canvas, 'e', gridWidth - 5, 1, regularPaint);
                drawChar(canvas, 's', gridWidth - 4, 1, regularPaint);
                drawChar(canvas, 's', gridWidth - 3, 1, regularPaint);
                drawChar(canvas, 'e', gridWidth - 2, 1, regularPaint);
            }
            public @Override boolean onTouchEvent(MotionEvent me) {
                if (me.getAction() != MotionEvent.ACTION_DOWN) {
                    return false;
                }
                int gx = (int) (me.getX() / textWidth);
                int gy = (int) (me.getY() / textHeight) + 1;
                Log.v("solitaire", "touched at grid " + gx + "," + gy);
                return true;
            }
            public @Override boolean onTrackballEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.v("solitaire", "trackball click @" + cursorX + "," + cursorY);
                    int[] xy = snapCursor();
                    handle(xy[0], xy[1]);
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    Log.v("solitaire", "trackball move " + event.getX() + "," + event.getY());
                    cursorX += event.getX() * textWidth;
                    cursorY += event.getY() * textHeight;
                    invalidate();
                }
                return true;
            }
            private int[] snapCursor() {
                int freeX = (int) (cursorX / textWidth);
                int freeY = (int) (cursorY / textHeight);
                return new int[] {freeX, freeY};
            }
            private void handle(int gx, int gy) {
                if (gx == 1 && gy == 3) {
                    if (model.deckRemaining() == 0) {
                        init();
                    } else {
                        model.flip();
                        Log.v("solitaire", "flip => " + Model.RANKS[model.flipped()]);
                    }
                } else {
                    int[] xy = grid2Model(gx, gy);
                    if (xy != null) {
                        int mx = xy[0];
                        int my = xy[1];
                        if (mx == hotX && my == hotY) {
                            hotX = -1;
                            hotY = -1;
                        } else if (model.canAcquire(mx, my)) {
                            Log.v("solitaire", "acquire " + Model.RANKS[model.read(mx, my)] + " @" + mx + "," + my);
                            model.acquire(mx, my);
                        } else if (hotX == -1 && model.present(mx, my) && model.free(mx, my)) {
                            Log.v("solitaire", "hold " + Model.RANKS[model.read(mx, my)] + " @" + mx + "," + my);
                            hotX = mx;
                            hotY = my;
                        } else if (hotX != -1 && model.canMove(hotX, hotY, mx, my)) {
                            Log.v("solitaire", "move " + hotX + "," + hotY + " -> " + mx + "," + my);
                            model.move(hotX, hotY, mx, my);
                            hotX = -1;
                            hotY = -1;
                        } else {
                            Log.v("solitaire", "cannot do anything at " + mx + "," + my);
                        }
                    }
                }
                invalidate();
            }
        });
    }
}
