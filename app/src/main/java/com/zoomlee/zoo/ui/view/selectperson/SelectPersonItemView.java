package com.zoomlee.zoo.ui.view.selectperson;

import android.content.Context;
import android.util.AttributeSet;

import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.ui.view.SelectItemView;
import com.zoomlee.zoo.utils.UiUtil;

public class SelectPersonItemView extends SelectItemView {

    public SelectPersonItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bind(Person person) {
        textItemName.setText(person.getName().toUpperCase());
        UiUtil.loadPersonIcon(person, imageItem, false);
    }
}
