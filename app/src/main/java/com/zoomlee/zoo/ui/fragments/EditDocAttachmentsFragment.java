package com.zoomlee.zoo.ui.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.File;
import com.zoomlee.zoo.ui.MaterialDialog;
import com.zoomlee.zoo.ui.activity.CreateEditDocActivity;
import com.zoomlee.zoo.ui.adapters.FilesAdapter;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.FileLoader;
import com.zoomlee.zoo.utils.FileUtil;
import com.zoomlee.zoo.utils.IntentUtils;

import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

public class EditDocAttachmentsFragment extends FragmentWithImagePicker {
    private static final String TMP_IMG = "tmp.png";
    public static final String ARG_FILE_PATH = "arg_file_path";
    private static final String ARG_FILE_TYPE = "arg_file_type";
    private FilesAdapter mAdapter;
    private View stubLayout;
    /**
     * File loader to load files that are opened with {@link IntentUtils}.
     */
    private FileLoader fileLoader;

    public static EditDocAttachmentsFragment newInstance() {
        EditDocAttachmentsFragment fragment = new EditDocAttachmentsFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public EditDocAttachmentsFragment() {
        super(TMP_IMG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_edit_doc_attachments, container, false);
        stubLayout = mView.findViewById(R.id.noDataLayout);

        RecyclerView filesView = (RecyclerView) mView.findViewById(R.id.files_rv);

        filesView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        filesView.setLayoutManager(mLayoutManager);

        Document document = ((CreateEditDocActivity) getActivity()).curDocument;
        List<File> files = document.getFilesList();
        mAdapter = new FilesAdapter(files);
        mAdapter.setListener(new FilesAdapter.OnActionClicked() {
            @Override
            public void onDeleteItem(int position) {
                showDeleteDialog(position);
            }

            @Override
            public void onAddFile() {
                openDialog();
            }

            @Override
            public void onItemClicked(int position) {
                Document document = ((CreateEditDocActivity) getActivity()).curDocument;
                File file = mAdapter.getFiles().get(position);
                fileLoader = IntentUtils.openFile((AppCompatActivity) getActivity(), file, document.getName());
            }
        });
        filesView.setAdapter(mAdapter);
        checkStub();

        String filePath = getArguments().getString(ARG_FILE_PATH);
        if (filePath != null) {
            // file preloaded
            attachFile(new java.io.File(filePath), (File.Type) getArguments().getSerializable(ARG_FILE_TYPE), false);
            // it's synchronized and further be managed by document
            getArguments().remove(ARG_FILE_PATH);
        }

        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Document document = ((CreateEditDocActivity) getActivity()).curDocument;
        document.setFilesList(getFilesList());
    }

    public void setInitialAttachment(String filePath, File.Type fileType) {
        getArguments().putString(ARG_FILE_PATH, filePath);
        getArguments().putSerializable(ARG_FILE_TYPE, fileType);
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().registerSticky(this);

        hideKeyboard();
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
        if (fileLoader != null) {
            fileLoader.onStop();
        }
    }

    private void showDeleteDialog(final int position) {
        MaterialDialog mMaterialDialog = new MaterialDialog(getActivity())
                .setTitle(R.string.title_warning)
                .setMessage(R.string.message_delete_confirmation)
                .setPositiveButton(R.string.delete_uc, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdapter.removeAt(position);
                        checkStub();
                    }
                })
                .setNegativeButton(R.string.cancel_uc, null);

        mMaterialDialog.show();
    }

    private void checkStub() {
        if (mAdapter.getItemCount() == 1) {
            stubLayout.setVisibility(View.VISIBLE);
        } else {
            stubLayout.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(FileObtainedEvent event) {
        attachFile(event.file, File.Type.IMAGE, true);
    }

    @Override
    public void onImageObtained(java.io.File image) {
        // Not used directly, just to satisfy interface.
        // Actual image otain callback is handled using registering sticky for FileObtainedEvent
    }

    private void attachFile(final java.io.File sourceFile, final File.Type fileType, final boolean deleteSource) {
        if (sourceFile == null) {
            closeDialog();
        } else {
            new AsyncTask<Void, Void, File>() {

                @Override
                protected File doInBackground(Void... params) {
                    DeveloperUtil.michaelLog("activity: " + getActivity());

                    Document document = ((CreateEditDocActivity) getActivity()).curDocument;

                    String fileName = "file_" + System.currentTimeMillis() + fileType.extension;
                    try {
                        java.io.File destFile = new java.io.File(getActivity().getFilesDir(), fileName);
                        FileUtil.copyFileUsingFileChannels(sourceFile, destFile);
                        File fileEntity = new File();
                        fileEntity.setTypeId(fileType.filesType);
                        fileEntity.setCreateTime(System.currentTimeMillis() / 1000);
                        fileEntity.setLocalPath(destFile.getAbsolutePath());
                        fileEntity.setUserId(document.getUserId());
                        return fileEntity;
                    } catch (IOException ioe) {
                        Log.e("EditDocAttachmentsFrag", "saveFileInternally error", ioe);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(File fileEntity) {
                    if (fileEntity != null) {
                        mAdapter.add(fileEntity);
                        checkStub();
                    }
                    closeDialog();

                    if (deleteSource) {
                        sourceFile.delete();
                    } else {
                        FileUtil.deleteIfNeeded(getActivity(), sourceFile);
                    }
                }
            }.execute();
        }
    }

    public List<File> getFilesList() {
        return mAdapter.getFiles();
    }

    public static class FileObtainedEvent {

        private final java.io.File file;

        public FileObtainedEvent(java.io.File file) {
            this.file = file;
        }
    }
}
