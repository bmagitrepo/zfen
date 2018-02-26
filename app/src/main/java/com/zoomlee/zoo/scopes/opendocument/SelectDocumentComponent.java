package com.zoomlee.zoo.scopes.opendocument;

import com.zoomlee.zoo.scopes.ActivityScope;
import com.zoomlee.zoo.ui.view.selectdocument.SelectDocumentView;

import dagger.Component;

@ActivityScope
@Component(modules = SelectDocumentModule.class)
public interface SelectDocumentComponent {

    void injectView(SelectDocumentView view);
}
