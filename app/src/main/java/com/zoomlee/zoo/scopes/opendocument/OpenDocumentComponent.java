package com.zoomlee.zoo.scopes.opendocument;

import com.zoomlee.zoo.scopes.ActivityScope;
import com.zoomlee.zoo.ui.view.OpenDocView;

import dagger.Component;

@ActivityScope
@Component(modules = OpenDocumentModule.class)
public interface OpenDocumentComponent {

    void injectView(OpenDocView view);
}
