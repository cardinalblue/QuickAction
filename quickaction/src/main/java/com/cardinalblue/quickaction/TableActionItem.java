package com.cardinalblue.quickaction;

import android.graphics.drawable.Drawable;

/**
 * Created by jimytc on 12/17/14.
 */
public class TableActionItem extends ActionItem {
    private int rowIndex = 0;

    public TableActionItem(int actionId, String title, Drawable icon, int rowIndex) {
        super(actionId, title, icon);
        this.rowIndex = rowIndex;
    }

    /**
     * Constructor
     *
     * @param actionId  Action id for case statements
     * @param title     Title
     * @param icon      Icon to use
     */
    public TableActionItem(int actionId, String title, Drawable icon) {
        this(actionId, title, icon, 0);
    }

    /**
     * Constructor
     */
    public TableActionItem() {
        this(-1, null, null);
    }

    /**
     * Set row index of this Action Item
     * @param rowIndex rowIndex
     */
    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getRowIndex() {
        return this.rowIndex;
    }
}