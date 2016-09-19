package com.cardinalblue.quickaction;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cardinalblue.quickaction.views.CustomRelativeLayout;

/**
 * QuickAction dialog, shows action list as icon and text like the one in
 * Gallery3D app. Currently supports vertical and horizontal layout.
 *
 * @author Lorensius W. L. T lorenz@londatiga.net
 *         Contributors: - Kevin Peck kevinwpeck@gmail.com
 *         Contributors: - Prada Hsiung bear.prada@gmail.com
 */
public class QuickAction implements OnDismissListener {
    protected Context mContext;
    protected PopupWindow mWindow;
    protected View mRootView;
    protected WindowManager mWindowManager;

    private final View mMainView;

    protected final LayoutInflater mInflater;

    protected View mPopup;
    protected ImageView mArrowTop;
    protected ImageView mArrowBottom;
    protected ImageView mShowedArrow;
    protected ViewGroup mTrack;
    protected ScrollView mScroller;
    protected OnActionItemClickListener mItemClickListener;
    private PopupWindow.OnDismissListener mDismissListener;

    protected final List<ActionItem> actionItems = new ArrayList<>();

    protected boolean mDidAction;

    private int mAnimStyle;
    private int mPopupWidth = 0;
    private int mPopupHeight = 0;

    private final Rect mEdge;

    public static final int ANIM_GROW_FROM_LEFT = 1;
    public static final int ANIM_GROW_FROM_RIGHT = 2;
    public static final int ANIM_GROW_FROM_CENTER = 3;

    private static final int SCREEN_PADDING = 10;

    /**
     * Constructor allowing orientation override
     *
     * @param context Context
     * @param view View
     */
    public QuickAction(Context context, View view) {
        mContext = context;
        mWindow = new PopupWindow(context);
        mWindow.setBackgroundDrawable(null);
        mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setOutsideTouchable(true);
        mWindow.setTouchable(true);
        mWindow.setFocusable(false);
        mWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Rect r = new Rect();
                v.getDrawingRect(r);
                if (r.contains((int)event.getX(), (int)event.getY())) {
                    if (mRootView != null) {
                        mRootView.dispatchTouchEvent(event);
                    }
                    return true;
                }
                return false;
            }
        });

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        mMainView = view;

        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initialRootView();

        final int screenWidth = mMainView.getWidth();
        final int screenHeight = mMainView.getHeight();
        mEdge = new Rect(SCREEN_PADDING, SCREEN_PADDING,
            screenWidth - SCREEN_PADDING,
            screenHeight - SCREEN_PADDING);
    }

    /**
     * Get action item at an index
     *
     * @param index Index of item (position from callback)
     *
     * @return Action Item at the position
     */
    public ActionItem getActionItem(int index) {
        return actionItems.get(index);
    }

    /**
     * Set root view.
     */
    public void initialRootView() {
        mPopup = mInflater.inflate(R.layout.popup_vertical, null);
        mTrack = (ViewGroup) mPopup.findViewById(R.id.tracks);

        mArrowBottom = (ImageView) mPopup.findViewById(R.id.arrow_bottom);
        mArrowTop = (ImageView) mPopup.findViewById(R.id.arrow_top);

        mScroller = (ScrollView) mPopup.findViewById(R.id.scroller);

        // This was previously defined on show() method, moved here to prevent
        // force close that occured
        // when tapping fastly on a view to show quickaction dialog.
        // Thanx to zammbi (github.com/zammbi)
        mPopup.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        setContentView(mPopup);
    }

    /**
     * Set listener for action item clicked.
     *
     * @param listener Listener
     */
    public void setOnActionItemClickListener(OnActionItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * Add action item
     *
     * @param action {@link ActionItem}
     */
    public void addActionItem(ActionItem action) {
        addActionItem(action, R.layout.action_item_vertical);
    }

    public void addActionItem(ActionItem action, int layoutId) {
        actionItems.add(action);

        String title = action.getTitle();
        Drawable icon = action.getIcon();

        View container = mInflater.inflate(layoutId, null);

        final ImageView img = (ImageView) container.findViewById(R.id.iv_icon);
        final TextView text = (TextView) container.findViewById(R.id.tv_title);

        if (icon != null) {
            img.setImageDrawable(icon);
        } else {
            img.setVisibility(View.GONE);
        }

        if (title != null) {
            text.setText(title);
        } else {
            text.setVisibility(View.GONE);
        }

        final int actionId = action.getActionId();
        container.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int pos = getItemPositionById(actionId);
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(QuickAction.this, pos, actionId, img, text);
                }
                if (!getActionItem(pos).isSticky()) {
                    mDidAction = true;
                    dismiss(0);
                }
            }
        });
        container.setFocusable(true);
        container.setClickable(true);

        mTrack.addView(container);
    }

    /**
     * Remove action item by action id
     *
     * @param actionId {@link ActionItem}
     */
    public void removeByActionId(final int actionId) {
        int pos = getItemPositionById(actionId);
        if (pos != -1) {
            mTrack.removeViewAt(pos);
        }
    }

    /**
     * Return the position of action item with action id
     *
     * @param actionId actionId
     * @return position of action item or -1 if not found
     */
    protected int getItemPositionById(final int actionId) {
        int pos = -1;
        for (ActionItem action : actionItems) {
            pos++;
            if (actionId == action.getActionId()) {
                return pos;
            }
        }
        return -1;
    }

    public void showAtView(View anchor) {
        int[] location = new int[2];

        anchor.getLocationOnScreen(location);

        Rect anchorRect = new Rect(location[0], location[1], location[0]
                + anchor.getWidth(), location[1] + anchor.getHeight());

        show(anchorRect, anchor);
    }

    public void showAtLocation(float x, float y) {

        // When display the PopupWindow, the position was used in global coordinate system.
        // But the coordX and coordY here is in mMainView coordinate system.
        // Thus, we have to translate to global system.
        Rect mainViewRect = new Rect();
        mMainView.getGlobalVisibleRect(mainViewRect);
        Rect rect = new Rect((int) x + mainViewRect.left,
                             (int) y + mainViewRect.top,
                             (int) x + mainViewRect.left,
                             (int) y + mainViewRect.top);
        show(rect, mMainView);
    }

    /**
     * Show quickaction popup. Popup is automatically positioned, on top or
     * bottom of anchor view. When the space above or below the anchor view is not enough,
     * Pupup is re-positioned to show the entire menu.
     */

    private void show(Rect rect, View parentView) {
        if (mRootView == null) {
            throw new IllegalStateException("setContentView was not called with a view to display.");
        }

        onShow();
        mWindow.setContentView(mRootView);

        // (popupX,popupY): the top-left position in screen of popupWindow
        int popupX;
        int popupY;

        mDidAction = false;

        if (mPopupWidth == 0 || mPopupHeight == 0) {
            mPopup.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            mPopupWidth = mPopup.getMeasuredWidth();
            mPopupHeight = mPopup.getMeasuredHeight();
        }

        // Y-coordinate
        final int spaceTop = rect.top;
        final int spaceBottom = mEdge.bottom - rect.bottom;

        // if top space is larger than bottom space, then Popup should grow upwards.
        final boolean isGrowUpwards = spaceTop > spaceBottom;

        if (mPopupHeight > mEdge.height()) {
            // If the whole Popup is too high to fit in the screen, re-position and resize the Popup.
            mShowedArrow = null;
            popupY = mEdge.top;
            LayoutParams l = mScroller.getLayoutParams();
            l.height = mEdge.height();
        } else if (isGrowUpwards) {
            //  Popup grows upwards, \/ arrow
            if (mPopupHeight > spaceTop) {
                // If the Popup is too high to fit in spaceTop,
                // re-position rootView and don't show the arrow.
                mShowedArrow = null;
                popupY = mEdge.top;
            } else {
                mShowedArrow = mArrowBottom;
                popupY = rect.top - mPopupHeight;
            }
        } else {
            //  Popup grows downwards, /\ arrow
            if (mPopupHeight > spaceBottom) {
                // If the Popup is too high to fit in spaceBottom,
                // re-position rootView and don't show the arrow.
                mShowedArrow = null;
                popupY = mEdge.height() - mPopupHeight;
            } else {
                mShowedArrow = mArrowTop;
                popupY = rect.bottom;
            }
        }

        // X-coordinate
        if ( (rect.centerX() + mPopupWidth/2) > mEdge.right ) {
            // If the right edge goes beyond the screen, re-position the Popup
            popupX = mEdge.right - mPopupWidth;
            mAnimStyle = ANIM_GROW_FROM_RIGHT;
        } else if ((rect.centerX() - mPopupWidth/2) < mEdge.left) {
            // If the left edge goes beyond the screen
            popupX = mEdge.left;
            mAnimStyle = ANIM_GROW_FROM_LEFT;
        } else {
            popupX = rect.centerX() - mPopupWidth/2;
            mAnimStyle  = ANIM_GROW_FROM_CENTER;
        }

        // arrowPosX: the X-coord of the arrow relative to mPopup
        showArrow(rect, popupX);

        setAnimationStyle(isGrowUpwards);

        try {
            mWindow.showAtLocation(parentView, Gravity.NO_GRAVITY, popupX, popupY);
        } catch (Exception e) {
            // catch java.lang.RuntimeException: Failed to register input channel
        }
    }

    /**
     * Set animation style
     *
     * @param isGrowUpwards flag to indicate where the popup should be
     * displayed. Set TRUE if displayed on top of anchor view and vice versa
     */
    private void setAnimationStyle(boolean isGrowUpwards) {

        switch (mAnimStyle) {
        case ANIM_GROW_FROM_LEFT:
            mWindow.setAnimationStyle((isGrowUpwards) ? R.style.Animations_PopUpMenu_Left
                    : R.style.Animations_PopDownMenu_Left);
            break;

        case ANIM_GROW_FROM_RIGHT:
            mWindow.setAnimationStyle((isGrowUpwards) ? R.style.Animations_PopUpMenu_Right
                    : R.style.Animations_PopDownMenu_Right);
            break;

        case ANIM_GROW_FROM_CENTER:
            mWindow.setAnimationStyle((isGrowUpwards) ? R.style.Animations_PopUpMenu_Center
                    : R.style.Animations_PopDownMenu_Center);
            break;
        }
    }

    /**
     * Show arrow
     *
     * @param popupX distance from left screen
     */
    private void showArrow(Rect rect, int popupX) {
        // Even if mShowedArrow is null, we still need to hide top and bottom arrows.
        // Otherwise 1 or both of them will be accidentally showed,
        // because their visibility was set to VISIBLE before.
        mArrowTop.setVisibility(View.INVISIBLE);
        mArrowBottom.setVisibility(View.INVISIBLE);

        if (mShowedArrow == null) {
            return;
        }

        final int arrowWidth = mShowedArrow.getMeasuredWidth();
        int windowPadding = mScroller.getPaddingLeft();
        int arrowLeftMargin = rect.centerX() - popupX - arrowWidth / 2;

        int arrowLeftWithPadding = rect.centerX() - arrowWidth - windowPadding;
        int arrowRightWithPadding = rect.centerX() + arrowWidth + windowPadding;
        if (arrowLeftWithPadding < popupX) {
            arrowLeftMargin = windowPadding + arrowWidth / 2;
        } else if (arrowRightWithPadding > (popupX + mPopupWidth)) {
            arrowLeftMargin = mPopupWidth - (arrowWidth * 3 / 2) - windowPadding;
        }

        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) mShowedArrow
                .getLayoutParams();
        param.leftMargin = arrowLeftMargin;

        mShowedArrow.setVisibility(View.VISIBLE);
    }

    /**
     * Set listener for window dismissed. This listener will only be fired if
     * the quick-action dialog is dismissed by clicking outside the dialog or
     * clicking on sticky item.
     *
     * @param listener Listener
     */
    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mWindow.setOnDismissListener(this);
        mDismissListener = listener;
    }

    @Override
    public void onDismiss() {
        if (!mDidAction && mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    /**
     * Listener for item click
     *
     */
    public interface OnActionItemClickListener {
        void onItemClick(QuickAction source, int pos, int actionId, ImageView img, TextView text);
    }

    /**
     * On show
     */
    private void onShow() {
        if (mRootView == null) {
            return;
        }

        if (mRootView instanceof CustomRelativeLayout) {
            ((CustomRelativeLayout)mRootView).setDispatchKeyEventListener(new CustomRelativeLayout.OnDispatchKeyEventListener() {

                @Override
                public void onDispatchKeyEvent(KeyEvent event) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
                        event.getRepeatCount() == 0 &&
                        mWindow.isShowing()) {
                        dismiss(mWindow);
                    }
                }
            });
        }

        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && mWindow.isShowing()) {
                    dismiss(mWindow);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Set content view.
     *
     * @param root Root view
     */
    public void setContentView(View root) {
        mRootView = root;
        mWindow.setContentView(root);
    }

    public void dismiss(final long delayMillis) {
        if (!mWindow.isShowing()) {
            return;
        }
//        Task.callInBackground(new Callable<Void>() {
//            @Override
//            public Void call() throws Exception {
//                Thread.sleep(delayMillis);
//                return null;
//            }
//        }).continueWith(new Continuation<Void, Void>() {
//            @Override
//            public Void then(Task<Void> task) throws Exception {
                dismiss(mWindow);
//                return null;
//            }
//        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * @return true if this PopupWindow is showing
     */
    public boolean isShowing() {
        return mWindow.isShowing();
    }

    private static void dismiss(PopupWindow popupWindow) {
        // Check if the window is showing before dismiss
        if (popupWindow != null && popupWindow.isShowing()) {
            try {
                popupWindow.dismiss();
            } catch (IllegalArgumentException | NullPointerException | IllegalStateException ignored) {
            }
        }
    }
}
