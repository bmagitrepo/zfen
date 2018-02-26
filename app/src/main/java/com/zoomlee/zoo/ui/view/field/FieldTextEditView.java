package com.zoomlee.zoo.ui.view.field;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.zoomlee.zoo.R;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 2/18/15
 */
public class FieldTextEditView extends FieldEditView {

    private boolean hideTitle;

    public FieldTextEditView(Context context) {
        this(context, null);
    }

    public FieldTextEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FieldTextEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, true);
    }

    protected FieldTextEditView(Context context, AttributeSet attrs, int defStyleAttr, boolean initUi) {
        super(context, attrs, defStyleAttr);
        if (initUi) {
            initUi(attrs);
        }
    }

    private void initUi(AttributeSet attrs) {
        setOrientation(VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.horizontal_margin);
        setPadding(padding, 0, padding, 0);

        inflate(getContext(), R.layout.view_field_text, this);

        titleView = (TextView) findViewById(R.id.titleTv);
        valueView = (TextView) findViewById(R.id.valueEt);
        valueView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateTitleVisibility();
            }
        });

        if (attrs == null) return;
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.FieldEditView, 0, 0);
        try {
            hideTitle = array.getBoolean(R.styleable.FieldEditView_hide_title, false);
            String title = array.getString(R.styleable.FieldEditView_title);
            boolean titleSingleLine = array.getBoolean(R.styleable.FieldEditView_title_single_line, true);

            titleView.setSingleLine(titleSingleLine);
            titleView.setText(title);
            if (hideTitle) valueView.setHint(title);
            updateTitleVisibility();
        } finally {
            array.recycle();
        }
    }

    private void updateTitleVisibility() {
        boolean hide = valueView.getText().length() == 0 && hideTitle;
        titleView.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void setValue(String value) {
        valueView.setText(value);
    }

    @Override
    public String getValue() {
        return valueView.getText().toString();
    }

    public boolean isHideTitle() {
        return hideTitle;
    }

    public void setHideTitle(boolean hideTitle) {
        this.hideTitle = hideTitle;
        if (hideTitle) valueView.setHint(titleView.getText());
        updateTitleVisibility();
    }
}
