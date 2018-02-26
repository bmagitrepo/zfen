package com.zoomlee.zoo.scopes.opendocument;

import com.zoomlee.zoo.scopes.ActivityScope;
import com.zoomlee.zoo.ui.view.selectdocument.SelectDocumentView;

import dagger.Module;
import dagger.Provides;

@Module
public class SelectDocumentModule {

    private final SelectDocumentView.Presenter presenter;

    public SelectDocumentModule(SelectDocumentView.Presenter presenter) {
        this.presenter = presenter;
    }

    @ActivityScope
    @Provides
    SelectDocumentView.Presenter providePresenter() {
        return presenter;
    }
}
