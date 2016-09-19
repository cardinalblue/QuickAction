package com.cardinalblue.quickaction.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class CustomRelativeLayout extends LinearLayout {

    public CustomRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRelativeLayout(Context context) {
        super(context);
    }

    private OnDispatchKeyEventListener mOnDispatchKeyEventListener;



    public void setDispatchKeyEventListener(OnDispatchKeyEventListener listener) {
        mOnDispatchKeyEventListener = listener;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mOnDispatchKeyEventListener != null) {
            mOnDispatchKeyEventListener.onDispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }   

    public interface OnDispatchKeyEventListener {
        void onDispatchKeyEvent(KeyEvent event);
    }
}
