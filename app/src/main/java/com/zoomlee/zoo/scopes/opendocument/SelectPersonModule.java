package com.zoomlee.zoo.scopes.opendocument;

import com.zoomlee.zoo.scopes.ActivityScope;
import com.zoomlee.zoo.ui.view.selectperson.SelectPersonView;

import dagger.Module;
import dagger.Provides;

@Module
public class SelectPersonModule {

    private final SelectPersonView.Presenter presenter;

    public SelectPersonModule(SelectPersonView.Presenter presenter) {
        this.presenter = presenter;
    }

    @ActivityScope
    @Provides
    SelectPersonView.Presenter providePresenter() {
        return presenter;
    }
}
