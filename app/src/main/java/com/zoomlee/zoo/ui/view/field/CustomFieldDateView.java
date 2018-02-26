package com.zoomlee.zoo.ui.view.field;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Field;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 4/6/15
 */
public class CustomFieldDateView extends FieldDateEditView {

    public CustomFieldDateView(Context context) {
        this(context, null);
    }

    public CustomFieldDateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomFieldDateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, false);

        inflate(context, R.layout.view_custom_field_date, this);

        titleView = (TextView) findViewById(R.id.titleTv);
        valueView = (TextView) findViewById(R.id.valueTv);
        deleteBtn = (ImageView) findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteField(CustomFieldDateView.this);
            }
        });
        valueView.setOnClickListener(this);
    }

    @Override
    public Field getField() {
        Field field = super.getField();
        String title = titleView.getText().toString();
        if (TextUtils.isEmpty(title))
            title = Field.CUSTOM_FIELD_NAME;
        field.setName(title);
        return field;
    }
}
