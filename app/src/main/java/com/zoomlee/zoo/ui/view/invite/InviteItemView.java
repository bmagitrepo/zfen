package com.zoomlee.zoo.ui.view.invite;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.invites.Contact;
import com.zoomlee.zoo.utils.CircleTransform;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.PicassoUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Author vbevans94.
 */
public class InviteItemView extends RelativeLayout {

    @BindView(R.id.text_name)
    TextView textName;

    @BindView(R.id.text_details)
    TextView textDetails;

    @BindView(R.id.text_status)
    TextView textStatus;

    @BindView(R.id.image_contact)
    ImageView imageContact;

    private Contact contact;

    public InviteItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this);
    }

    public void bind(Contact contact) {
        this.contact = contact;

        textName.setText(contact.getName());
        textDetails.setText(contact.getChannelsString());
        textStatus.setText(contact.getStatus().titleResId);
        textStatus.setEnabled(contact.getStatus().inviteEnabled);

        PicassoUtil.getInstance().load(contact.getDisplayPhotoUri())
                .transform(new CircleTransform())
                .placeholder(R.drawable.stub_person_green)
                .into(imageContact);
    }

    @OnClick(R.id.text_status)
    @SuppressWarnings("unused")
    void onInviteClicked() {
        EventBus.getDefault().post(new Events.InviteClicked(contact));
    }
}
