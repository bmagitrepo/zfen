package com.zoomlee.zoo.scopes.opendocument;

import com.zoomlee.zoo.scopes.ActivityScope;
import com.zoomlee.zoo.ui.view.OpenDocView;

import dagger.Module;
import dagger.Provides;

@Module
public class OpenDocumentModule {

    private final OpenDocView.Presenter presenter;

    public OpenDocumentModule(OpenDocView.Presenter presenter) {
        this.presenter = presenter;
    }

    @ActivityScope
    @Provides
    OpenDocView.Presenter providePresenter() {
        return presenter;
    }
}
