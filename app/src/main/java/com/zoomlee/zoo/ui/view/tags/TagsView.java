package com.zoomlee.zoo.ui.view.tags;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.TagsDocDaoHelper;
import com.zoomlee.zoo.ui.adapters.BindableArrayAdapter;
import com.zoomlee.zoo.ui.view.ComplexDividerListView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * Author vbevans94.
 */
public class TagsView extends RelativeLayout {

    @BindView(android.R.id.list)
    ComplexDividerListView listTags;

    @Inject
    Presenter presenter;

    private final TagsAdapter adapter;

    public TagsView(Context context) {
        this(context, null);
    }

    public TagsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.include_documents_by_categories, this);

        adapter = new TagsAdapter(context);

        ButterKnife.bind(this);

        listTags.setThickPadding(getResources().getDimension(R.dimen.horizontal_margin));
        listTags.setEmptyView(findViewById(R.id.noDataLayout));
        listTags.setAdapter(adapter);
    }

    public void setTags(List<TagsDocDaoHelper.TagsDocAlerts> tags) {
        tags = new ArrayList<>(tags);
        if (!tags.isEmpty()) {
            TagsDocDaoHelper.TagsDocAlerts noTag = null;
            for (TagsDocDaoHelper.TagsDocAlerts tag : tags) {
                if (tag.getId() == TagsDocDaoHelper.TagsDocAlerts.NO_TAG_ID) {
                    noTag = tag;
                    break;
                }
            }
            if (noTag != null) {
                // set "no tag" name for no_tag tag
                noTag.setName(getContext().getString(R.string.title_no_tag));

                if (tags.size() > 1) {
                    // modify list only in case when there is no_tag and others
                    List<TagsDocDaoHelper.TagsDocAlerts> newTags = new ArrayList<>();
                    tags.remove(noTag);
                    newTags.addAll(tags);
                    newTags.add(new TagsDocDaoHelper.TagsDocAlerts());
                    newTags.add(noTag);
                    tags = newTags;
                }
            }
        }

        adapter.replaceWith(tags);
    }

    @OnClick(R.id.addNewBtn)
    @SuppressWarnings("unused")
    void onNewDocumentClicked() {
        presenter.createDocument();
    }

    @OnItemClick(android.R.id.list)
    @SuppressWarnings("unused")
    void onTagClicked(int position) {
        presenter.selectTag(adapter.getItem(position));
    }

    public interface Presenter {

        void createDocument();

        void selectTag(TagsDocDaoHelper.TagsDocAlerts tag);
    }

    private static class TagsAdapter extends BindableArrayAdapter<TagsDocDaoHelper.TagsDocAlerts> {

        public TagsAdapter(Context context) {
            super(context, R.layout.item_tag_with_docs);
        }

        @Override
        public void bindView(TagsDocDaoHelper.TagsDocAlerts item, int position, View view) {
            TagItemView tagItemView = (TagItemView) view;
            tagItemView.bind(item);
        }
    }
}
