package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Form;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.utils.UiUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FormItemView extends FrameLayout {
    private static final SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy");

    public static final int ANIMATION_DURATION = 250;

    @BindView(R.id.detailsLayout)
    View detailsLayout;
    @BindView(R.id.actionsLayout)
    View actionsLayout;
    @BindViews({R.id.editFormLayout, R.id.printLayout})
    List<View> buttons;
    @BindView(R.id.formIcon)
    ImageView formIcon;
    @BindView(R.id.formNameTv)
    TextView formNameTv;
    @BindView(R.id.personIconIv)
    ImageView ivPersonIcon;
    @BindView(R.id.dateTv)
    ZMTextView dateTv;

    private FormItemListener listener;
    private Form form;

    public FormItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this);

        ButterKnife.apply(buttons, new SetClickable(false));
    }

    public void bind(Form form, boolean allPersons, User user, List<Person> personsList) {
        if (isOpened()) {
            closeActions(true);
        }

        this.form = form;
        formNameTv.setText(form.getName().toUpperCase());
        formIcon.setColorFilter(form.getColor(getContext()));
        dateTv.setText(format.format(new Date(form.getUpdateTime() * 1000L)));
        viewPersonIcon(allPersons, user, personsList);
    }

    public Form getForm() {
        return form;
    }

    @OnClick(R.id.editFormLayout)
    @SuppressWarnings("unused")
    void onEditClicked() {
        if (listener != null) {
            listener.onEdit(form);

            closeActions(true);
        }
    }

    @OnClick(R.id.printLayout)
    @SuppressWarnings("unused")
    void onPrintClicked() {
        if (listener != null) {
            listener.onPrint(form);

            closeActions(true);
        }
    }

    public void openActions(FormItemListener listener) {
        openActions(false, listener);
    }

    public void openActions(boolean immediate, FormItemListener listener) {
        this.listener = listener;

        ButterKnife.apply(buttons, new SetClickable(true));
        detailsLayout.animate().x(-actionsLayout.getMeasuredWidth()).setDuration(immediate ? 0 : ANIMATION_DURATION).start();
    }

    public void closeActions(boolean immediate) {
        listener = null;

        ButterKnife.apply(buttons, new SetClickable(false));
        detailsLayout.animate().x(0).setDuration(immediate ? 0 : ANIMATION_DURATION).start();
    }

    public boolean isOpened() {
        return listener != null;
    }

    private void viewPersonIcon(boolean allPersons, User user, List<Person> personsList) {
        if (allPersons) {
            Person person = null;
            if (form.getLocalPersonId() == Person.ME_ID)
                person = user;
            else {
                for (Person personItem : personsList)
                    if (personItem.getId() == form.getLocalPersonId()) {
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
            ivPersonIcon.setVisibility(INVISIBLE);
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

    public interface FormItemListener {

        void onPrint(Form form);

        void onEdit(Form form);
    }
}
