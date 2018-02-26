package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;

import com.zoomlee.zoo.net.RestTask;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 29.01.15.
 */
public class TaskExecutorFactory {

    public static RestTaskExecutor getExecutor(int taskType) {
        switch (taskType) {
            case RestTask.Types.STATIC_DATA_GET:
                return new GetStaticDataTaskExecutor();
            case RestTask.Types.USER_DATA_GET:
                return new GetUserDataTaskExecutor();
            case RestTask.Types.USER_GET:
                return new GetUserTaskExecutor();
            case RestTask.Types.USER_GET_ICON:
                return new GetUserIconTaskExecutor();
            case RestTask.Types.USER_PUT:
                return new PutUserTaskExecutor();
            case RestTask.Types.PERSON_GET_ICON:
                return new GetPersonIconTaskExecutor();
            case RestTask.Types.PERSON_UPLOAD:
                return new UploadPersonTaskExecutor();
            case RestTask.Types.PERSON_DELETE:
                return new DeletePersonTaskExecutor();
            case RestTask.Types.DOCUMENTS_UPLOAD:
                return new UploadDocumentTaskExecutor();
            case RestTask.Types.DOCUMENTS_DELETE:
                return new DeleteDocumentTaskExecutor();
            case RestTask.Types.FILES_GET:
                return new GetFileTaskExecutor();
            case RestTask.Types.FILES_POST:
                return new PostFileTaskExecutor();
            case RestTask.Types.FILES_DELETE:
                return new DeleteFileTaskExecutor();
            case RestTask.Types.TAG_PUT:
                return new PutTagTaskExecutor();
            case RestTask.Types.TAG_DELETE:
                return new DeleteTagTaskExecutor();
            case RestTask.Types.TAX_UPLOAD:
                return new UploadTaxTaskExecutor();
            case RestTask.Types.TAX_DELETE:
                return new DeleteTaxTaskExecutor();
            case RestTask.Types.FORM_POST:
                return new PostFormTaskExecutor();
            default:
                break;
        }

        return null;
    }

    public interface RestTaskExecutor {
        boolean execute(Context context, RestTask restTask);
    }
}