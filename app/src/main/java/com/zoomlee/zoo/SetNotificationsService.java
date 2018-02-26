package com.zoomlee.zoo;

import android.app.IntentService;
import android.content.Intent;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Field;
import com.zoomlee.zoo.provider.helpers.FieldsHelper;
import com.zoomlee.zoo.utils.NotificationsUtil;
import com.zoomlee.zoo.utils.TimeUtil;

import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 3/19/15
 */
public class SetNotificationsService extends IntentService {
    private static final String NAME = "SetupNotificationsServices";

    public SetNotificationsService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        setupNoticiations();
    }

    private void setupNoticiations() {
        DaoHelper<Field> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Field.class);
        String selection = FieldsHelper.FieldsContract.TABLE_NAME + "." + FieldsHelper.FieldsContract.NOTIFY_ON + " > " + TimeUtil.getServerCurrentTimestamp();
        List<Field> fields = daoHelper.getAllItems(this, selection, null, null);

        for (Field field : fields)
            NotificationsUtil.addReminder(this, field);
    }
}
