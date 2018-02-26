package com.zoomlee.zoo.scopes.opendocument;

import com.zoomlee.zoo.scopes.ActivityScope;
import com.zoomlee.zoo.ui.view.selectcategory.SelectCategoryView;

import dagger.Component;

@ActivityScope
@Component(modules = SelectCategoryModule.class)
public interface SelectCategoryComponent {

    void injectView(SelectCategoryView view);
}
