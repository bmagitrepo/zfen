package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 2/16/15
 */
public class ColorPickerView extends LinearLayout {

    private int selectedInd;
    private ArrayList<Integer> colorValues = new ArrayList<>();
    private ArrayList<Color> colors = new ArrayList<>();
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(Color color);
    }

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        setLayoutParams(layoutParams);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    public Color getSelectedColor() {
        return colors.get(selectedInd);
    }


    public void setColorValues(Color[] colors) {
        for (Color color : colors) {
            this.colors.add(color);
            this.colorValues.add(android.graphics.Color.parseColor("#" + color.getHex()));
        }
        updateView();
    }


    public void setColors(List<Color> colors) {
        for (Color color : colors) {
            this.colors.add(color);
            this.colorValues.add(android.graphics.Color.parseColor("#" + color.getHex()));
        }
        updateView();
    }

    public void addColor(Color color) {
        this.colors.add(color);
        this.colorValues.add(android.graphics.Color.parseColor("#" + color.getHex()));

        updateView();
    }

    public void selectColor(int position) {
        ImageView v = (ImageView) findViewWithTag(position);
        ImageView oldView = (ImageView) getChildAt(selectedInd);
        oldView.setImageDrawable(null);
        selectedInd = position;
        v.setImageResource(R.drawable.selected_color);
    }

    public void selectColor(long colorId) {
        int colorsSize = colors.size();
        for (int i = 0; i < colorsSize; i++) {
            if (colorId == colors.get(i).getId())
                selectColor(i);
        }
    }

    public void selectColor(String hex) {
        if (hex == null) {
            return;
        }

        int colorsSize = colors.size();
        for (int i = 0; i < colorsSize; i++) {
            if (colors.get(i).getHex().contains(hex)) {
                selectColor(i);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        updateView();
    }

    private void updateView() {
        if (colors.isEmpty()) {
            return;
        }

        int colorsSize = colorValues.size();

        removeAllViews();

        Resources r = getResources();
        int diameter = r.getDimensionPixelSize(R.dimen.color_item_diameter);


        int margin = (getWidth() - colorsSize * diameter) / (colorsSize - 1) / 2;

        LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(diameter, diameter);
        innerParams.setMargins(margin, 0, margin, 0);

        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(diameter, diameter);
        leftParams.setMargins(0, 0, margin, 0);

        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(diameter, diameter);
        rightParams.setMargins(margin, 0, 0, 0);


        for (int i = 0; i < colorsSize; i++) {

            Drawable drawable = r.getDrawable(R.drawable.color_item);
            drawable.setColorFilter(colorValues.get(i), PorterDuff.Mode.SRC_ATOP);

            ImageView colorItem = new ImageView(getContext());
            colorItem.setLayoutParams(innerParams);
            colorItem.setScaleType(ImageView.ScaleType.CENTER);
            colorItem.setBackgroundDrawable(drawable);
            colorItem.setOnClickListener(colorClickListener);
            colorItem.setTag(i);

            addView(colorItem);
        }

        getChildAt(0).setLayoutParams(leftParams);
        getChildAt(colorsSize - 1).setLayoutParams(rightParams);

        if (selectedInd > -1) {
            ((ImageView) getChildAt(selectedInd)).setImageResource(R.drawable.selected_color);
        }

        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    }

    private OnClickListener colorClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            selectColor(position);
            if (listener != null)
                listener.onColorSelected(colors.get(position));
        }
    };
}
