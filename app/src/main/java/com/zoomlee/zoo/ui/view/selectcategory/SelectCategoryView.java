package com.zoomlee.zoo.ui.view.selectcategory;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.ui.adapters.BindableArrayAdapter;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnItemClick;

public class SelectCategoryView extends FrameLayout {

    @BindView(R.id.list_items)
    ListView listItems;

    @Inject
    Presenter presenter;

    private final SelectCategoryAdapter adapter;

    public SelectCategoryView(Context context, AttributeSet attrs) {
        super(context, attrs);

        adapter = new SelectCategoryAdapter(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (isInEditMode()) {
            return;
        }

        ButterKnife.bind(this);

        listItems.setEmptyView(findViewById(R.id.text_empty_categories));
        listItems.setAdapter(adapter);
    }

    @OnItemClick(R.id.list_items)
    @SuppressWarnings("unused")
    void onCategoryClicked(int position) {
        presenter.selectCategory(adapter.getItem(position));
    }

    public void setCategories(List<Category> categories) {
        adapter.replaceWith(categories);
    }

    private static class SelectCategoryAdapter extends BindableArrayAdapter<Category> {

        public SelectCategoryAdapter(Context context) {
            super(context, R.layout.item_select_category);
        }

        @Override
        public void bindView(Category item, int position, View view) {
            SelectCategoryItemView itemView = (SelectCategoryItemView) view;
            itemView.bind(item);
        }
    }

    public interface Presenter {

        void selectCategory(Category category);
    }
}
