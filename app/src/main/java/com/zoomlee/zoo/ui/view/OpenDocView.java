package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zoomlee.zoo.R;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

public class OpenDocView extends LinearLayout {

    @BindView(R.id.text_file_name)
    TextView textFileName;

    @Inject
    Presenter presenter;

    public OpenDocView(Context context, AttributeSet attrs) {
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

    /**
     * Sets file name.
     * @param name to set
     */
    public void setFileName(String name) {
        textFileName.setText(name);
    }

    @OnClick(R.id.button_cancel)
    @SuppressWarnings("unused")
    void onCancelClicked() {
        presenter.cancel();
    }

    @OnClick(R.id.button_create_new)
    @SuppressWarnings("unused")
    void onCreateNewClicked() {
        presenter.createNewDocument();
    }

    @OnClick(R.id.button_add_to_existing)
    @SuppressWarnings("unused")
    void onAddToExistingClicked() {
        presenter.addToExistingDocument();
    }

    public interface Presenter {

        void cancel();

        void addToExistingDocument();

        void createNewDocument();
    }
}
