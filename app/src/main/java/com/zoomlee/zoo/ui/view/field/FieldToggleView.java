package com.zoomlee.zoo.ui.view.field;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.zoomlee.zoo.R;

public class FieldToggleView extends FieldEditView {

    protected TextView headerTv;
    protected TextView bodyTv;
    protected ToggleButton toggleBtn;

    public FieldToggleView(Context context) {
        this(context, null);
    }

    public FieldToggleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FieldToggleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(getContext(), R.layout.view_field_switch, this);

        headerTv = (TextView) findViewById(R.id.headerTv);
        bodyTv = (TextView) findViewById(R.id.hintTv);
        toggleBtn = (ToggleButton) findViewById(R.id.toggleBtn);

        init(attrs);
    }

    @Override
    public void setValue(String value) {
        if (value != null && (value.toLowerCase().equals("yes") || value.toLowerCase().equals("x")))
            setChecked(true);
    }

    @Override
    public String getValue() {
        return isChecked() ? "Yes" : "";
    }

    private void init(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.FieldToggleView, 0, 0);
        try {
            String header = array.getString(R.styleable.FieldToggleView_header);
            String body = array.getString(R.styleable.FieldToggleView_body);

            headerTv.setText(header);
            bodyTv.setText(body);
            bodyTv.setVisibility(body == null ? GONE : VISIBLE);
        } finally {
            array.recycle();
        }
    }

    public String getHeader() {
        return headerTv.getText().toString();
    }

    public String getBody() {
        return bodyTv.getText().toString();
    }

    public boolean isChecked() {
        return toggleBtn.isChecked();
    }

    public void setHeader(String header) {
        headerTv.setText(header);
    }

    public void setBody(String body) {
        bodyTv.setText(body);
        bodyTv.setVisibility(body == null ? GONE : VISIBLE);
    }

    public void setChecked(boolean checked) {
        toggleBtn.setChecked(checked);
    }

}
