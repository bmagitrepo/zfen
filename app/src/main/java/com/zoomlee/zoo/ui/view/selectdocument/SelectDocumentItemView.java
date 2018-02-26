package com.zoomlee.zoo.ui.view.selectdocument;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.ui.view.DocumentIconView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectDocumentItemView extends LinearLayout {

    @BindView(R.id.text_document_name)
    TextView textDocumentName;

    @BindView(R.id.document_icon)
    DocumentIconView documentIconView;

    public SelectDocumentItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (isInEditMode()) {
            return;
        }

        ButterKnife.bind(this);
    }

    public void bind(Document document) {
        documentIconView.setDocument(document);
        textDocumentName.setText(document.getName().toUpperCase());
    }
}
