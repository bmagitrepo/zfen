package com.zoomlee.zoo.scopes.opendocument;

import com.zoomlee.zoo.scopes.ActivityScope;
import com.zoomlee.zoo.ui.view.selectperson.SelectPersonView;

import dagger.Component;

@ActivityScope
@Component(modules = SelectPersonModule.class)
public interface SelectPersonComponent {

    void injectView(SelectPersonView view);
}
