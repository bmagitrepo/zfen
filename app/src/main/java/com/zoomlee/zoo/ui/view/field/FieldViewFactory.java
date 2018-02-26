package com.zoomlee.zoo.ui.view.field;

import android.content.Context;

import com.zoomlee.zoo.net.model.Field;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 4/6/15
 */
public class FieldViewFactory {

    public static FieldEditView makeFieldView(Context ctx, Field field) {
        if (field.isCustom()) {
            if (field.getType() == Field.DATE_TYPE)
                return new CustomFieldDateView(ctx);
            else if (field.getType() == Field.TEXT_TYPE)
                return new CustomFieldTextView(ctx);
        } else {
            if (field.getType() == Field.DATE_TYPE)
                return new FieldDateEditView(ctx);
            else if (field.getType() == Field.TEXT_TYPE)
                return new FieldTextEditView(ctx);
        }

        throw new IllegalStateException("Unexpected Field Type ID");
    }
}
