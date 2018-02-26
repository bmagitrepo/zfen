package com.zoomlee.zoo.ui.view.selectdocument;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.ui.adapters.BindableArrayAdapter;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class SelectDocumentView extends ListView {

    @Inject
    Presenter presenter;

    private final DocumentsAdapter adapter;

    public SelectDocumentView(Context context, AttributeSet attrs) {
        super(context, attrs);

        adapter = new DocumentsAdapter(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (isInEditMode()) {
            return;
        }

        ButterKnife.bind(this);

        setAdapter(adapter);
    }

    public void setDocuments(List<Document> documents) {
        adapter.replaceWith(documents);
    }

    @OnItemClick(R.id.select_document_view)
    @SuppressWarnings("unused")
    void onDocumentClicked(int position) {
        presenter.selectDocument(adapter.getItem(position));
    }

    private static class DocumentsAdapter extends BindableArrayAdapter<Document> {

        public DocumentsAdapter(Context context) {
            super(context, R.layout.item_select_document);
        }

        @Override
        public void bindView(Document item, int position, View view) {
            SelectDocumentItemView itemView = (SelectDocumentItemView) view;
            itemView.bind(item);
        }
    }

    public interface Presenter {

        void selectDocument(Document document);
    }
}
