package org.renpy.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageButton;

public class JoystickButton extends AppCompatImageButton implements View.OnTouchListener {
    private final static float CLICK_DRAG_TOLERANCE = 20;
    private float downRawX, downRawY, dX, dY;

    public JoystickButton(Context context){
        super(context);
        init();
    }

    public JoystickButton(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        init();
    }

    public JoystickButton(Context context, AttributeSet attributeSet, int defStyleAttr){
        super(context,attributeSet,defStyleAttr);
        init();
    }

    private void init(){
      setOnTouchListener(this);
    }



    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)view.getLayoutParams();

        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN){
            downRawX = motionEvent.getRawX();
            downRawY = motionEvent.getRawY();
            dX = view.getX() - downRawX;
            dY = view.getY() -downRawY;
            return true;
        } else if (action == MotionEvent.ACTION_MOVE){
            int viewWidth = view.getWidth();
            int viewHeight = view.getHeight();

            View viewParent = (View)view.getParent();
            int parentWidth = viewParent.getWidth();
            int parentHeight = viewParent.getHeight();

            float newX = motionEvent.getRawX() + dX;
            newX = Math.max(layoutParams.leftMargin,newX);
            newX = Math.min(parentWidth - viewWidth - layoutParams.rightMargin, newX);

            float newY = motionEvent.getRawY() - dY;
            newY = Math.max(layoutParams.topMargin, newY);
            newY = Math.min(parentHeight - viewHeight - layoutParams.bottomMargin,newY);
            view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start();
            view.bringToFront();
            view.invalidate();
            return true;
        } else if (action == MotionEvent.ACTION_UP){
            float upRawX = motionEvent.getRawX();
            float upRawY = motionEvent.getRawY();

            float upDX = upRawX - downRawX;
            float upDY = upRawY - downRawY;

            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE){
                return performClick();
            } else {
                return true;
            }
        } else {
            return super.onTouchEvent(motionEvent);
        }
    }
}
