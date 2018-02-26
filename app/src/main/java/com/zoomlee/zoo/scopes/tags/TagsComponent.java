package com.zoomlee.zoo.scopes.tags;

import com.zoomlee.zoo.scopes.ActivityScope;
import com.zoomlee.zoo.ui.view.tags.TagsView;

import dagger.Component;

@ActivityScope
@Component(modules = TagsModule.class)
public interface TagsComponent {

    void injectView(TagsView view);
}
