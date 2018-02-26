package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.adapters.IncitationsAdapter;
import com.zoomlee.zoo.ui.adapters.NoDivider;

public class ComplexDividerListView extends DynamicListView {

    private final float thickWidth;
    private final float thickHeight;
    private float thickPadding;
    private final float thinHeight;
    private final Paint paint;

    public ComplexDividerListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ComplexDividerListView, 0, 0);
        int color = array.getColor(R.styleable.ComplexDividerListView_dividerColor, Color.BLACK);
        thickWidth = array.getDimension(R.styleable.ComplexDividerListView_thickWidth, 1);
        thickHeight = array.getDimension(R.styleable.ComplexDividerListView_thickHeight, 1);
        thickPadding = array.getDimension(R.styleable.ComplexDividerListView_thickPadding, 1);
        thinHeight = array.getDimension(R.styleable.ComplexDividerListView_thinHeight, 1);
        array.recycle();

        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        setDivider(null);
    }

    public void setThickPadding(float thickPadding) {
        this.thickPadding = thickPadding;

        invalidate();
    }

    @Override
    protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);

        boolean allowDivider = true;
        ListAdapter adapter = getAdapter();

        // unwrap adapter
        if (adapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        if (adapter instanceof IncitationsAdapter) {
            adapter = ((IncitationsAdapter) adapter).getWrapped();
        }

        int position = getPositionForView(child);
        NoDivider noDivider = null;
        if (adapter instanceof NoDivider) {
            noDivider = (NoDivider) adapter;
            if (position >= 0 && position < getAdapter().getCount() && noDivider.noDivider(position)) {
                allowDivider = false;
            }
        }
        if (allowDivider) {
            drawDivider(canvas, child.getRight(), child.getTop());
        }

        if (position == getAdapter().getCount() - 1) {
            // for last item draw divider below
            if (noDivider == null || !noDivider.noFooterLine()) {
                drawDivider(canvas, child.getRight(), child.getBottom());
            }
        }

        return result;
    }

    private void drawDivider(@NonNull Canvas canvas, int right, int top) {
        if (top > 0) {
            canvas.drawRect(thickPadding, top, thickPadding + thickWidth, top + thickHeight, paint);
            canvas.drawRect(thickPadding + thickWidth, top, right, top + thinHeight, paint);
        }
    }
}
