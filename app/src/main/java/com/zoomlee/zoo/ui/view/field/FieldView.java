package com.zoomlee.zoo.ui.view.field;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Field;
import com.zoomlee.zoo.ui.CaseNumberDialog;
import com.zoomlee.zoo.utils.TimeUtil;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 24.03.15.
 */
public class FieldView extends LinearLayout {

    private TextView nameTv;
    private TextView valueTv;

    public FieldView(Context context) {
        this(context, null);
    }

    public FieldView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.field_view, this);

        nameTv = (TextView) findViewById(R.id.fieldNameTv);
        valueTv = (TextView) findViewById(R.id.fieldValueTv);
    }

    /**
     * Sets field by {@link Field} instance. Shows alert indication if field is expired.
     *
     * @param field to set
     */
    public void setField(Field field) {
        long notifyOn = field.getLongNotifyOn();
        long currentTime = TimeUtil.getServerEndDayTimestamp();
        if (notifyOn == -1 || notifyOn < currentTime) {
            int valueColor = getResources().getColor(notifyOn == -1
                    ? R.color.text_gray : R.color.dark_orange);
            valueTv.setTextColor(valueColor);
            valueTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            valueTv.setTextColor(getResources().getColor(R.color.text_gray));
            valueTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.alert_clock, 0);
        }

        nameTv.setText(field.getName().toLowerCase());
        valueTv.setText(field.getFormattedValue());
    }

    /**
     * Sets field by its name and value pair.
     *
     * @param name  of the field
     * @param value of the field
     */
    public void setField(String name, String value) {
        nameTv.setText(name);
        valueTv.setText(value);
    }

    /**
     * Sets whether value should be single line or not.
     *
     * @param isValueSingleLine to set
     */
    public void setSingleLine(boolean isValueSingleLine) {
        valueTv.setSingleLine(isValueSingleLine);
    }

    public void needToHighlight() {
        valueTv.setTextColor(getResources().getColor(R.color.green_zoomlee));
        valueTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CaseNumberDialog dialog = new CaseNumberDialog(getContext(), valueTv.getText().toString());
                dialog.show();
            }
        });
    }
}
