package com.zoomlee.zoo.ui.fragments.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.invites.Contact;
import com.zoomlee.zoo.ui.view.BottomSheet;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;


public class InviteFragment extends DialogFragment {

    public final static String TAG = InviteFragment.class.getName();
    private static final String ARG_CONTACT = "arg_contact";

    @BindView(R.id.list_channels)
    ListView listChannels;

    private ChannelsAdapter adapter;
    private Contact contact;
    private OnInviteListener listener;

    public static InviteFragment newInstance(Contact contact) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_CONTACT, contact);

        InviteFragment fragment = new InviteFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        listener = (OnInviteListener) activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_dialog_invite, null);

        ButterKnife.bind(this, view);

        contact = getArguments().getParcelable(ARG_CONTACT);
        adapter = new ChannelsAdapter(getActivity(), contact.getChannels());
        listChannels.setAdapter(adapter);

        return new BottomSheet(view);
    }

    @OnItemClick(R.id.list_channels)
    @SuppressWarnings("unused")
    void onChannelClicked(int position) {
        listener.onInvite(contact, adapter.getItem(position));
        dismiss();
    }

    private static class ChannelsAdapter extends ArrayAdapter<Contact.Channel> {

        public ChannelsAdapter(Context context, List<Contact.Channel> channels) {
            super(context, R.layout.item_invite_channel, channels);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            TextView textView = (TextView) super.getView(position, view, parent);
            textView.setCompoundDrawablesWithIntrinsicBounds(getItem(position).getType().iconResId, 0, 0, 0);
            return textView;
        }
    }

    public interface OnInviteListener {

        void onInvite(Contact contact, Contact.Channel channel);
    }
}
