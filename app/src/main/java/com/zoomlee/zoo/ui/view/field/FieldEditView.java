package com.zoomlee.zoo.ui.view.field;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zoomlee.zoo.net.model.Field;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 2/18/15
 */
public abstract class FieldEditView extends LinearLayout {

    protected Field field;
    protected TextView valueView;
    protected TextView titleView;
    protected ImageView deleteBtn;
    protected OnDeleteFieldListener listener;

    public interface OnDeleteFieldListener {
        void onDeleteField(FieldEditView view);
    }

    protected FieldEditView(Context context) {
        this(context, null);
    }

    protected FieldEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    protected FieldEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public abstract void setValue(String value);
    public abstract String getValue();

    public void setField(Field newField) {
        this.field = newField;
        setTitle(field.getName());
        setValue(field.getFormattedValue());
    }

    public Field getField() {
        String value = valueView.getText().toString();
        field.setValue(value);
        return field;
    }


    public void setOnDeleteFieldListener(OnDeleteFieldListener newListener) {
        this.listener = newListener;
    }

}
