package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.cocosw.undobar.UndoBarController;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.ui.adapters.TagsSettingsAdapter;
import com.zoomlee.zoo.ui.view.EmptyRecyclerView;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class TagsSettingsActivity extends SecuredActionBarActivity implements UndoBarController.AdvancedUndoListener {

    public static final String DELETE_POSITION = "delete_position";
    public static final String DELETE_TAG = "delete_tag";

    private List<Tag> baseTags;
    private TagsSettingsAdapter adapter;
    private LoadDataAsyncTask loadTask;
    private EmptyRecyclerView tagsView;
    private View focusedView;
    private Menu menu;
    private UndoBarController.UndoBar undoBar;
    private Tag tagToDelete;

    private DaoHelper<Tag> tagsDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Tag.class);
    private boolean editMode = false;

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, TagsSettingsActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        activity.startActivityForResult(intent, RequestCodes.TAGS_SETTINGS_ACTIVITY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tags_settings);
        initUi();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.tags);
        updateABAvatar(null);
    }

    private void initUi() {
        tagsView = (EmptyRecyclerView) findViewById(R.id.tagsRv);
        tagsView.setHasFixedSize(true);
        tagsView.setEmptyView(findViewById(R.id.noDataView));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        tagsView.setLayoutManager(mLayoutManager);
        adapter = new TagsSettingsAdapter();
        adapter.setListener(new TagsSettingsAdapter.OnTagModifiedListener() {

            @Override
            public void onFocusChanged(View v, boolean hasFocuse, int position) {
                if (hasFocuse) tagsView.scrollToPosition(position);
                focusedView = hasFocuse ? v : null;
                if (hasFocuse && !editMode) {
                    enableEditMode();
                }
            }

            @Override
            public void onDelete(Tag tag, int position) {
                adapter.removeAt(position);

                Bundle token = new Bundle();
                token.putInt(DELETE_POSITION, position);
                token.putParcelable(DELETE_TAG, tag);

                clearUndoBar();

                tagToDelete = tag;
                undoBar = new UndoBarController.UndoBar(TagsSettingsActivity.this)
                        .message(getString(R.string.message_tag_deleted))
                        .listener(TagsSettingsActivity.this)
                        .token(token);
                undoBar.show();
            }
        });
        tagsView.setAdapter(adapter);
    }

    private void clearFocus() {
        adapter.clearFocus();
        if (focusedView == null) return;

        hideKeyboard();
        focusedView.clearFocus();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
    }

    private void enableEditMode() {
        clearUndoBar();
        editMode = true;
        EventBus.getDefault().unregister(this);
        cancelTask();
        baseTags = cloneTags();

        showOnlyName(true);
        menu.findItem(R.id.action_save).setVisible(true);
    }

    private void enableNormalMode(boolean saveChanges) {
        clearUndoBar();
        tagToDelete = null;
        editMode = false;
        showOnlyName(false);
        menu.findItem(R.id.action_save).setVisible(false);
        clearFocus();

        if (saveChanges) saveTagsChanges();
        else adapter.setData(baseTags);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
            loadData();
        }
    }

    private List<Tag> cloneTags() {
        List<Tag> adapterTags = adapter.getData();
        List<Tag> result = new ArrayList<>(adapterTags.size());
        for (Tag tag: adapterTags) result.add(new Tag(tag));
        return result;
    }

    private void saveTagsChanges() {
        List<Tag> adapterTags = adapter.getData();

        baseTagsLoop:
        for (Tag baseTag: baseTags) {
            for (Tag updatedTag: adapterTags) {
                if (baseTag.getId() == updatedTag.getId()) {
                    if (!baseTag.getName().equals(updatedTag.getName())){
                        if (updatedTag.getName().length() > 0)
                            tagsDaoHelper.saveLocalChanges(this, updatedTag);
                        else
                            updatedTag.setName(baseTag.getName());
                    }
                    continue baseTagsLoop;// continue first loop
                }
            }
            tagsDaoHelper.deleteItem(this, baseTag);
        }

        adapter.setData(adapterTags);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!editMode && !EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
            loadData();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearUndoBar();
        if (!editMode) {
            EventBus.getDefault().unregister(this);
            cancelTask();
        }
    }

    private void clearUndoBar() {
        if (undoBar != null) {
            undoBar.clear();
            undoBar = null;
        }
    }

    private void loadData() {
        cancelTask();
        loadTask = new LoadDataAsyncTask();
        loadTask.execute();
    }

    private void cancelTask() {
        if (loadTask != null) {
            loadTask.cancel(true);
            loadTask = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tags_settings, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
                enableNormalMode(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (editMode)
            enableNormalMode(false);
        else
            super.onBackPressed();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(Events.TagChanged event) {
        loadData();
    }

    @Override
    public void onUndo(Parcelable parcelable) {
        tagToDelete = null;
        Bundle token = (Bundle) parcelable;
        int position = token.getInt(DELETE_POSITION);
        Tag tag = token.getParcelable(DELETE_TAG);

        adapter.add(position, tag);
    }

    @Override
    public void onHide(@Nullable Parcelable parcelable) {
        if (!editMode) {
            Bundle token = (Bundle) parcelable;
            Tag tag = token.getParcelable(DELETE_TAG);
            tagsDaoHelper.deleteItem(this, tag);
        }
    }

    @Override
    public void onClear(@NonNull Parcelable[] parcelables) {
        if (!editMode) {
            for (Parcelable parcelable: parcelables) {
                Bundle token = (Bundle) parcelable;
                Tag tag = token.getParcelable(DELETE_TAG);
                tagsDaoHelper.deleteItem(this, tag);
            }
        }
    }

    private class LoadDataAsyncTask extends AsyncTask<Void, Void, List<Tag>> {

        @Override
        protected List<Tag> doInBackground(Void... params) {

            return tagsDaoHelper.getAllItems(TagsSettingsActivity.this);
        }

        @Override
        protected void onPostExecute(List<Tag> tags) {
            if (tagToDelete != null) {
                tags.remove(tagToDelete);
            }
            adapter.setData(tags);
        }
    }
}
