package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Field;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.ui.view.field.FieldsTableView;
import com.zoomlee.zoo.utils.TimeUtil;
import com.zoomlee.zoo.utils.UiUtil;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

/**
 * Author vbevans94.
 */
public class DocumentItemView extends FrameLayout {

    private static final int MAX_VISIBLE_FIELDS = 6;
    public static final int ANIMATION_DURATION = 250;

    @BindView(R.id.layout_details)
    View layoutDetails;

    @BindView(R.id.layout_actions)
    View layoutActions;

    @BindViews({R.id.button_manage_tags, R.id.button_delete})
    List<View> buttons;

    @BindViews({R.id.text_manage_tags, R.id.text_delete})
    List<View> texts;

    @BindView(R.id.alertIv)
    View alertIv;

    @BindView(R.id.document_icon)
    DocumentIconView iconView;

    @BindView(R.id.docNameTv)
    TextView tvDocName;

    @BindView(R.id.personIconIv)
    ImageView ivPersonIcon;

    @BindView(R.id.fieldsTable)
    FieldsTableView fieldsTableView;

    private DocumentItemListener listener;
    private Document document;

    public DocumentItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this);

        ButterKnife.apply(buttons, new SetClickable(false));
    }

    public void bind(Document document, boolean allPersons, User user, List<Person> personsList) {
        if (isOpened()) {
            closeActions(true);
        }

        this.document = document;

        tvDocName.setText(document.getName().toUpperCase());
        iconView.setDocument(document);
        viewPersonIcon(allPersons, user, personsList);
        fieldsTableView.setFields(document.getVisibleFields(), MAX_VISIBLE_FIELDS, true, false);

        ButterKnife.apply(texts, new SetVisible(!document.getVisibleFields().isEmpty()));

        boolean alert = false;
        long currentTime = TimeUtil.getServerEndDayTimestamp();
        for (Field field : document.getVisibleFields()) {
            long notifyOn = field.getLongNotifyOn();
            if (notifyOn != -1 && notifyOn < currentTime) {
                alert = true;
                break;
            }
        }
        alertIv.setVisibility(alert ? View.VISIBLE : View.GONE);
    }

    public Document getDocument() {
        return document;
    }

    @OnClick(R.id.button_manage_tags)
    @SuppressWarnings("unused")
    void onManageTagsClicked() {
        if (listener != null) {
            listener.onManageTags(document);

            closeActions(true);
        }
    }

    @OnClick(R.id.button_delete)
    @SuppressWarnings("unused")
    void onDeleteClicked() {
        if (listener != null) {
            listener.onItemDelete(document);

            closeActions(true);
        }
    }

    public void openActions(DocumentItemListener listener) {
        openActions(false, listener);
    }

    public void openActions(boolean immediate, DocumentItemListener listener) {
        this.listener = listener;

        ButterKnife.apply(buttons, new SetClickable(true));
        layoutDetails.animate().x(-layoutActions.getMeasuredWidth()).setDuration(immediate ? 0 : ANIMATION_DURATION).start();
    }

    public void closeActions(boolean immediate) {
        listener = null;

        ButterKnife.apply(buttons, new SetClickable(false));
        layoutDetails.animate().x(0).setDuration(immediate ? 0 : ANIMATION_DURATION).start();
    }

    public boolean isOpened() {
        return listener != null;
    }

    private void viewPersonIcon(boolean allPersons, User user, List<Person> personsList) {
        if (allPersons) {
            Person person = null;
            if (document.getLocalPersonId() == Person.ME_ID)
                person = user;
            else {
                for (Person personItem : personsList)
                    if (personItem.getId() == document.getLocalPersonId()) {
                        person = personItem;
                        break;
                    }
            }

            if (person == null) {
                ivPersonIcon.setImageResource(R.drawable.stub_person_green);
            } else {
                UiUtil.loadPersonIcon(person, ivPersonIcon, false);
            }
        } else {
            ivPersonIcon.setVisibility(GONE);
        }
    }

    private static class SetVisible implements ButterKnife.Action<View> {

        private final boolean isVisible;

        private SetVisible(boolean isVisible) {
            this.isVisible = isVisible;
        }

        @Override
        public void apply(View view, int index) {
            view.setVisibility(isVisible ? VISIBLE : GONE);
        }
    }

    private static class SetClickable implements ButterKnife.Action<View> {

        private final boolean isClickable;

        private SetClickable(boolean isClickable) {
            this.isClickable = isClickable;
        }

        @Override
        public void apply(View view, int index) {
            view.setClickable(isClickable);
        }
    }

    public interface DocumentItemListener {

        void onItemDelete(Document document);

        void onManageTags(Document document);
    }
}
