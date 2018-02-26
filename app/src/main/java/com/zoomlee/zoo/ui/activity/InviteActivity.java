package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.invites.Contact;
import com.zoomlee.zoo.invites.InvitesController;
import com.zoomlee.zoo.ui.adapters.InviteAdapter;
import com.zoomlee.zoo.ui.fragments.dialog.InviteFragment;
import com.zoomlee.zoo.ui.view.LoadingView;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.UiUtil;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import de.greenrobot.event.EventBus;

/**
 * Author vbevans94.
 */
public class InviteActivity extends SecuredActionBarActivity implements InviteFragment.OnInviteListener {

    @BindView(android.R.id.list)
    ListView listInvite;

    @BindView(R.id.loading_view)
    LoadingView loadingView;

    @BindView(R.id.noDataView)
    View noDataView;

    private InviteAdapter adapter;
    private InvitesController invitesController;
    private LoadTask loadTask;
    private InviteTask inviteTask;

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, InviteActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        activity.startActivityForResult(intent, RequestCodes.INVITE_ACTIVITY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invite);

        ButterKnife.bind(this);

        listInvite.setEmptyView(findViewById(R.id.noDataView));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adapter = new InviteAdapter(this);
        listInvite.setAdapter(adapter);

        invitesController = new InvitesController(this, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);

        if (adapter.getCount() == 0 && loadTask == null) {
            // execute only when needed and nothing in progress
            loadTask = new LoadTask();
            loadTask.execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);

        // stop working for unfocused screen
        if (loadTask != null) {
            loadTask.cancel(true);
            loadTask = null;
        }
        if (inviteTask != null) {
            inviteTask.cancel(true);
            inviteTask = null;
        }
    }

    @Override
    public void onInvite(Contact contact, Contact.Channel channel) {
        if (inviteTask == null) {
            // if no inviting is in progress
            inviteTask = new InviteTask(contact);
            inviteTask.execute(channel);
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(Events.InviteClicked event) {
        Contact contact = event.getContact();
        List<Contact.Channel> channels = contact.getChannels();
        if (channels.size() == 1) {
            onInvite(contact, channels.get(0)); // one channel, no choosing
        } else {
            InviteFragment.newInstance(contact)
                    .show(getSupportFragmentManager(), InviteFragment.TAG);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        UiUtil.customizeMenuForSearch(this, menu, new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class LoadTask extends AsyncTask<Void, Void, List<Contact>> {

        @Override
        protected void onPreExecute() {
            noDataView.setVisibility(View.GONE);
            loadingView.show();
        }

        @Override
        protected List<Contact> doInBackground(Void... params) {
            // get contacts
            return invitesController.getContacts();
        }

        @Override
        protected void onPostExecute(List<Contact> contacts) {
            adapter.replaceWith(contacts);

            // indicate we're finished
            loadTask = null;

            // remove indicator
            noDataView.setVisibility(View.VISIBLE);
            loadingView.hide();
        }
    }

    private class InviteTask extends AsyncTask<Contact.Channel, Void, Contact.Status> {

        private final Contact contact;

        private InviteTask(Contact contact) {
            this.contact = contact;
        }

        @Override
        protected Contact.Status doInBackground(Contact.Channel... params) {
            Contact.Channel channel = params[0];
            String phone = null;
            String email = null;
            if (channel.getType() == Contact.ChannelType.EMAIL) {
                email = channel.getValue();
            } else {
                phone = channel.getValue();
            }
            return invitesController.sendInvite(phone, email);
        }

        @Override
        protected void onPostExecute(Contact.Status status) {
            if (status == Contact.Status.ERROR) {
                Toast.makeText(InviteActivity.this, R.string.error_invite_failed, Toast.LENGTH_LONG).show();
            }

            contact.setStatus(status);
            adapter.update(contact);

            // indicate we're finished
            inviteTask = null;
        }
    }
}
