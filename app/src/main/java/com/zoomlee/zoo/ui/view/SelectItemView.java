package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.R;

import butterknife.ButterKnife;
import butterknife.BindView;

public class SelectItemView extends FrameLayout {

    @BindView(R.id.image_item)
    protected ImageView imageItem;

    @BindView(R.id.text_item_name)
    protected TextView textItemName;

    public SelectItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.include_item_select, this);

        setClipToPadding(false);
        setClipChildren(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (isInEditMode()) {
            return;
        }

        ButterKnife.bind(this);
    }
}
