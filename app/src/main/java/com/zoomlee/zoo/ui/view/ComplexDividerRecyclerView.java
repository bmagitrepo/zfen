package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.adapters.NoDivider;

public class ComplexDividerRecyclerView extends EmptyRecyclerView {

    private final float thickWidth;
    private final float thickHeight;
    private final float thickPadding;
    private final float thinHeight;
    private final Paint paint;

    public ComplexDividerRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ComplexDividerListView, 0, 0);
        int color = array.getColor(R.styleable.ComplexDividerListView_dividerColor, Color.BLACK);
        thickWidth = array.getDimension(R.styleable.ComplexDividerListView_thickWidth, 1);
        thickHeight = array.getDimension(R.styleable.ComplexDividerListView_thickHeight, 1);
        thickPadding = array.getDimension(R.styleable.ComplexDividerListView_thickPadding, 0);
        thinHeight = array.getDimension(R.styleable.ComplexDividerListView_thinHeight, 1);
        array.recycle();

        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);
        boolean notDrawDivider = false;
        int position = getChildPosition(child);
        if (getAdapter() instanceof NoDivider) {
            if (((NoDivider) getAdapter()).noDivider(position)) {
                notDrawDivider = true;
            }
        }
        if (getAdapter().getItemCount() > 1 && !notDrawDivider) {
            drawDivider(canvas, child.getTop(), child.getRight());

            if (position == getAdapter().getItemCount() - 1) {
                // we need to draw divider only for non scrolling mode
                drawDivider(canvas, child.getBottom(), getRight());
            }
        }

        return result;
    }

    private void drawDivider(Canvas canvas, int top, int right) {
        canvas.drawRect(thickPadding, top, thickPadding + thickWidth, top + thickHeight, paint);
        canvas.drawRect(thickPadding + thickWidth, top, right, top + thinHeight, paint);
    }
}
