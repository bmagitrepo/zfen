package com.zoomlee.zoo.scopes.opendocument;

import com.zoomlee.zoo.scopes.ActivityScope;
import com.zoomlee.zoo.ui.view.selectcategory.SelectCategoryView;

import dagger.Module;
import dagger.Provides;

@Module
public class SelectCategoryModule {

    private final SelectCategoryView.Presenter presenter;

    public SelectCategoryModule(SelectCategoryView.Presenter presenter) {
        this.presenter = presenter;
    }

    @ActivityScope
    @Provides
    SelectCategoryView.Presenter providePresenter() {
        return presenter;
    }
}
