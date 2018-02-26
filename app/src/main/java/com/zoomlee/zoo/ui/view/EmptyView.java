package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zoomlee.zoo.R;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Author vbevans94.
 */
public class EmptyView extends FrameLayout {

    @BindView(R.id.text_title)
    TextView textTitle;

    @BindView(R.id.text_message)
    TextView textMessage;

    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_empty, this);

        ButterKnife.bind(this);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.EmptyView, 0, 0);
        setTitle(array.getString(R.styleable.EmptyView_textTitle));
        setMessage(array.getString(R.styleable.EmptyView_textMessage));
        array.recycle();
    }

    private void setMessage(String message) {
        textMessage.setText(message);
    }

    private void setTitle(String title) {
        textTitle.setText(title);
    }
}
