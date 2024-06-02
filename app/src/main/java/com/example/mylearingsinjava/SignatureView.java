package com.example.mylearingsinjava;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/** Custom View for capturing signatures */
public class SignatureView extends View {

    private Paint paint = new Paint(); // 'Paint' object for drawing signatures
    private List<Path> paths = new ArrayList<>(); // List of paths representing signatures
    private Bitmap bitmap; // 'Bitmap' object to hold the drawn signature
    private Canvas canvas; // 'Canvas' object to draw the signature
    private Path currentPath; // Current path being drawn

    /** constructor for SignatureView.
     * @param context Context of the application
     * @param attrs   Attributes set for the view */
    public SignatureView(Context context, AttributeSet attrs) {

        super(context, attrs);

        // initialize paint attributes
        paint.setAntiAlias(true);
        paint.setColor(0xFF000000); // Black color
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(8f);

    }// end 'SignatureView' constructor

    /** called when the size of the view changes.
     * @param w      New width of the view
     * @param h      New height of the view
     * @param oldw   Old width of the view
     * @param oldh   Old height of the view */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // initialize a new bitmap & canvas with the updated size
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

    }// end 'onSizeChanged' method

    /**
     * Called when the view needs to be drawn.
     *
     * @param canvas Canvas on which the view will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw all paths on the canvas
        for (Path path : paths) {

            canvas.drawPath(path, paint);

        }// end FOR

    }// end 'onDraw' method

    /** handles touch events for drawing the signature
     * @param event - 'MotionEvent' representing the touch event
     * @return - 'True' if the event was handled, 'false' otherwise */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                // start a new path when touch is initiated
                currentPath = new Path();
                currentPath.moveTo(x, y);
                paths.add(currentPath);
                return true;

            case MotionEvent.ACTION_MOVE:
                // draw a line to the current touch position when moving
                currentPath.lineTo(x, y);
                break;

            case MotionEvent.ACTION_UP:
                break;

            default:
                return false;

        }// end SWITCH

        // invalidate the view to trigger onDraw & update the display
        invalidate();
        return true;

    }// end 'onTouchEvent' method

    /** get the signature as a bitmap
     * @return - 'Bitmap' representing the signature */
    public Bitmap getSignatureBitmap() {

        return bitmap;

    }// end 'getSignatureBitmap' method

    /** clears the drawn signature */
    public void clear() {

        // clear the paths list and create a new blank bitmap and canvas
        paths.clear();
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        // invalidate the view to update the display
        invalidate();

    }// end 'clear' method

}// end 'SignatureView' class