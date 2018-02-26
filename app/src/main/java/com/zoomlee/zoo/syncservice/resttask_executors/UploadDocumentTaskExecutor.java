package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.model.Document;

import static com.zoomlee.zoo.syncservice.resttask_executors.TaskExecutorFactory.RestTaskExecutor;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 23.02.15.
 */
class UploadDocumentTaskExecutor implements RestTaskExecutor {

    @Override
    public boolean execute(Context context, RestTask restTask) {
        RestTaskExecutor taskExecutor;
        DaoHelper<Document> documentDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
        Document document = documentDaoHelper.getItemByLocalId(context, restTask.getLocalItemId(), true);
        if (document == null) return true;
        if (document.getRemoteId() != -1)
            taskExecutor = new PutDocumentTaskExecutor();
        else
            taskExecutor = new PostDocumentTaskExecutor();

        return taskExecutor.execute(context, restTask);
    }
}
