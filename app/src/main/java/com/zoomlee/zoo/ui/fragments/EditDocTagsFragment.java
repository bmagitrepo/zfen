package com.zoomlee.zoo.ui.fragments;


import android.app.Service;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.cocosw.undobar.UndoBarController;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.provider.helpers.TagsProviderHelper;
import com.zoomlee.zoo.ui.activity.CreateEditDocActivity;
import com.zoomlee.zoo.ui.adapters.TagsAdapter;

import java.util.ArrayList;
import java.util.List;


public class EditDocTagsFragment extends Fragment implements UndoBarController.UndoListener {

    public static final String DELETE_POSITION = "delete_position";
    public static final String DELETE_TAG = "delete_tag";
    private TagsAdapter adapter;
    private List<Tag> documentTags = new ArrayList<>();

    public static EditDocTagsFragment newInstance() {
        return new EditDocTagsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_edit_doc_tags, container, false);

        RecyclerView tagsView = (RecyclerView) mView.findViewById(R.id.tagsRv);
        tagsView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        tagsView.setLayoutManager(mLayoutManager);
        adapter = new TagsAdapter();
        adapter.setListener(new TagsAdapter.OnItemSelectedListener() {
            @Override
            public void onSelect(Tag tag) {
                if (documentTags.contains(tag)) {
                    Toast.makeText(getActivity(), getString(R.string.message_already_have_tag), Toast.LENGTH_SHORT).show();
                    return;
                }
                addTag(tag);
            }

            @Override
            public void onSearch(String s) {
                searchTags(s);
            }

            @Override
            public void onSearchFocus() {
                CreateEditDocActivity activity = (CreateEditDocActivity) getActivity();
                activity.modifyActionBar();
                adapter.setSearchMode(TagsAdapter.SearchState.SEARCH);
            }

            @Override
            public void onRemove(int position) {
                Tag tagToDelete = adapter.getData().get(position);
                Bundle token = new Bundle();
                token.putInt(DELETE_POSITION, position);
                token.putParcelable(DELETE_TAG, tagToDelete);

                new UndoBarController.UndoBar(getActivity())
                        .message(getString(R.string.message_tag_deleted))
                        .listener(EditDocTagsFragment.this)
                        .token(token)
                        .show();
            }
        });
        tagsView.setAdapter(adapter);

        try2Fill();
        return mView;
    }

    private void addTag(Tag tag) {
        documentTags.add(0, tag);
        tag.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        adapter.cleanSearch();
    }

    private void searchTags(String s) {
        if (s.length() == 0) {
            adapter.setSearchMode(TagsAdapter.SearchState.EMPTY_SEARCH);
            adapter.setData(documentTags);
            return;
        }
        DaoHelper<Tag> tagDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Tag.class);
        String selection = TagsProviderHelper.TagsContract.TABLE_NAME + "." + TagsProviderHelper.TagsContract.NAME + " LIKE '%" + s + "%'";
        List<Tag> tags = tagDaoHelper.getAllItems(getActivity(), selection, null, TagsProviderHelper.TagsContract.TABLE_NAME + "." + TagsProviderHelper.TagsContract.UPDATE_TIME + " DESC");
        adapter.setSearchMode(TagsAdapter.SearchState.SEARCH);
        adapter.setData(tags);
    }

    public void acceptTags() {
        CreateEditDocActivity activity = (CreateEditDocActivity) getActivity();
        activity.defaultActionBar();
        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        adapter.setData(documentTags);
        adapter.setSearchMode(TagsAdapter.SearchState.NOT_SEARCH);
        adapter.cleanSearch();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Document document = ((CreateEditDocActivity) getActivity()).curDocument;
        document.setTagsList(getTagList());
    }

    private void try2Fill() {
        Document document = ((CreateEditDocActivity) getActivity()).curDocument;
        if (document == null)
            return;

        List<Tag> tags = document.getTagsList();
        documentTags.addAll(tags);
        adapter.setData(documentTags);
    }

    public List<Tag> getTagList() {
        return adapter.getData();
    }

    @Override
    public void onUndo(Parcelable parcelable) {
        Bundle token = (Bundle) parcelable;
        int position = token.getInt(DELETE_POSITION);
        Tag tag = token.getParcelable(DELETE_TAG);

        adapter.getData().add(position, tag);
        adapter.notifyDataSetChanged();
    }
}
