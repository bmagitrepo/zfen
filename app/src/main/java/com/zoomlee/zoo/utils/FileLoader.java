package com.zoomlee.zoo.utils;

import android.content.Context;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.model.File;
import com.zoomlee.zoo.syncservice.RestTaskPoster;
import com.zoomlee.zoo.syncservice.SyncEvents;

import de.greenrobot.event.EventBus;

/**
 * Author vbevans94.
 */
public class FileLoader {

    private final Context context;
    private File file;
    private boolean loaded = false;
    private final FileLoadListener listener;

    public FileLoader(Context context, File file, FileLoadListener listener) {
        this.context = context;
        this.file = file;
        this.listener = listener;
    }

    public static void loadFromServer(Context context, int localId, boolean immediate) {
        RestTask restTask = new RestTask(localId, RestTask.Types.FILES_GET);
        RestTaskPoster.postTask(context, restTask, immediate);
    }

    public File getFile() {
        return file;
    }

    public void onEventMainThread(SyncEvents.RestTaskStatus restTaskStatus) {
        if (restTaskStatus.getStatus() == SyncEvents.SYNC_FINISHED &&
                restTaskStatus.getRestTask().getType() == RestTask.Types.FILES_GET &&
                restTaskStatus.getRestTask().getLocalItemId() == file.getId() &&
                !loaded) {
            DaoHelper<File> fileDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(File.class);
            File updatedFile = fileDaoHelper.getItemByLocalId(context, file.getId());
            if (updatedFile == null) {
                listener.onFileGone();
                return;
            }
            file = updatedFile;
            revisitFile();
        }
    }

    /**
     * Checks if file is loaded by revisiting it in the database.
     * @return true if file is already synced
     */
    public boolean revisitFile() {
        if (file.getLocalPath() == null) {

            // need to take some action
            listener.onLoadStarted();

            DaoHelper<File> fileDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(File.class);
            File updatedFile = fileDaoHelper.getItemByLocalId(context, file.getId());

            // file somehow gone
            if (updatedFile == null) {
                listener.onFileGone();
                return false;
            }

            file = updatedFile;
        }
        if (file.getLocalPath() != null) {
            if (new java.io.File(file.getLocalPath()).exists()) {
                listener.onFileLoaded(file);

                loaded = true;
                EventBus.getDefault().unregister(this);
                return true;
            } else {
                // file's path is set but there is no file under that path
                loadFromServer(context, file.getId(), true);
                listener.onLoadStarted(); // notify load
                return false;
            }
        }

        return false;
    }

    /**
     * Must be called every time activity whose context is used gets started.
     */
    public void onStart() {
        if (!loaded && !revisitFile()) {
            EventBus.getDefault().register(this);
        }
    }

    /**
     * Must be called every time activity stops.
     */
    public void onStop() {
        EventBus.getDefault().unregister(this);
    }

    public boolean isFileLoaded() {
        return file.getLocalPath() != null;
    }

    public interface FileLoadListener {

        void onFileLoaded(File file);

        void onLoadStarted();

        void onFileGone();
    }
}
