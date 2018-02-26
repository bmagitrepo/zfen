package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.model.Person;

import static com.zoomlee.zoo.syncservice.resttask_executors.TaskExecutorFactory.RestTaskExecutor;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 23.02.15.
 */
class UploadPersonTaskExecutor implements RestTaskExecutor {

    @Override
    public boolean execute(Context context, RestTask restTask) {
        RestTaskExecutor taskExecutor;
        DaoHelper<Person> personDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
        Person person = personDaoHelper.getItemByLocalId(context, restTask.getLocalItemId(), true);
        if (person == null) return true;
        if (person.getRemoteId() != -1)
            taskExecutor = new PutPersonTaskExecutor();
        else
            taskExecutor = new PostPersonTaskExecutor();

        return taskExecutor.execute(context, restTask);
    }
}
