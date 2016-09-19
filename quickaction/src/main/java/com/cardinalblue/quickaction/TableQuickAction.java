package com.cardinalblue.quickaction;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jimytc on 10/1/14.
 */
public class TableQuickAction extends QuickAction {
    private final boolean mIsBlack;
    private ArrayList<TableRow> mActionRows = new ArrayList<>();
    private int mItemLayoutId;

    /**
     * Constructor allowing orientation override
     *
     * @param context Context
     * @param view
     * @param isBlack
     */
    public TableQuickAction(Context context, View view, boolean isBlack) {
        super(context, view);
        mIsBlack = isBlack;
    }

    @Override
    public void initialRootView() {
        if (mIsBlack) {
            mPopup = mInflater.inflate(R.layout.popup_table_context_menu_black, null);
            mItemLayoutId = R.layout.popup_table_context_menu_item_black;
        } else {
            mPopup = mInflater.inflate(R.layout.popup_table_context_menu, null);
            mItemLayoutId = R.layout.popup_table_context_menu_item;
        }
        mTrack = (ViewGroup) mPopup.findViewById(R.id.table);

        mArrowBottom = (ImageView) mPopup.findViewById(R.id.arrow_bottom);
        mArrowTop = (ImageView) mPopup.findViewById(R.id.arrow_top);

        mScroller = (ScrollView) mPopup.findViewById(R.id.scroller);

        // This was previously defined on show() method, moved here to prevent
        // force close that occured
        // when tapping fastly on a view to show quickaction dialog.
        // Thanx to zammbi (github.com/zammbi)
        mPopup.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(mPopup);
    }

    @Override
    public void addActionItem(ActionItem action) {
        actionItems.add(action);

        // Get rowIndex from action for its preferred row. If not, always put to the first row
        int rowIndex = (action instanceof TableActionItem) ? ((TableActionItem) action).getRowIndex() : 0;

        TableRow rowItem;
        boolean isNewRow = false;
        if (rowIndex >= mActionRows.size()) {
            rowItem = (TableRow) mInflater.inflate(R.layout.popup_table_context_menu_row, mTrack, false);
            mActionRows.add(rowItem);
            isNewRow = true;
        } else {
            rowItem = mActionRows.get(rowIndex);
        }

        String title = action.getTitle();
        Drawable icon = action.getIcon();

        View container = mInflater.inflate(mItemLayoutId, rowItem, false);

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
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int pos = getItemPositionById(actionId);
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(TableQuickAction.this, pos, actionId, img, text);
                }
                if (!getActionItem(pos).isSticky()) {
                    mDidAction = true;
                    dismiss(0);
                }
            }
        });
        container.setFocusable(true);
        container.setClickable(true);

        rowItem.addView(container);
        if (isNewRow) {
            mTrack.addView(rowItem);
        }
    }
}
