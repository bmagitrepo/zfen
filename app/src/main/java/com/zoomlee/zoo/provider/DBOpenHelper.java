package com.zoomlee.zoo.provider;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zoomlee.zoo.provider.helpers.BaseProviderHelper;
import com.zoomlee.zoo.provider.helpers.CountriesProviderHelper.CountriesContract;
import com.zoomlee.zoo.provider.helpers.FieldsTypesProviderHelper.FieldsTypesContract;
import com.zoomlee.zoo.provider.helpers.FormFieldsProviderHelper;
import com.zoomlee.zoo.provider.helpers.FormsProviderHelper;
import com.zoomlee.zoo.provider.helpers.Tags2DocumentsProviderHelper;
import com.zoomlee.zoo.provider.helpers.TagsProviderHelper;
import com.zoomlee.zoo.provider.helpers.TaxProviderHelper;
import com.zoomlee.zoo.utils.FileUtil;

import java.io.File;
import java.util.List;

import static com.zoomlee.zoo.provider.helpers.FieldsHelper.FieldsContract;
import static com.zoomlee.zoo.provider.helpers.RestTasksHelper.RestTasksContract;

/**
 * SQLite backend for @{link ZoomleeProvider}.
 * <p/>
 * Provides access to an disk-backed, SQLite datastore which is utilized by ZoomleeProvider. This
 * database should never be accessed by other parts of the application directly.
 */
class DBOpenHelper extends SQLiteOpenHelper {
    /**
     * Schema version.
     */
    public static final int DATABASE_VERSION = 4;
    /**
     * Filename for SQLite file.
     */
    public static final String DATABASE_NAME = "internal.db";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            FileUtil.copyFromAssetsToData(context, DATABASE_NAME, dbFile);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        List<BaseProviderHelper> baseProviderHelpers = ProviderHelpersContainer.getInstance().getProviderHelpers();
        for (BaseProviderHelper baseProviderHelper : baseProviderHelpers)
            db.execSQL(baseProviderHelper.getCreateTableSQL());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + FieldsContract.TABLE_NAME + " ADD COLUMN " + FieldsContract.CREATE_TIME + " " + BaseProviderHelper.TYPE_INTEGER);
            db.execSQL(new TagsProviderHelper().getCreateTableSQL());
            db.execSQL(new Tags2DocumentsProviderHelper().getCreateTableSQL());
            increaseRestTaskTypes(db, 4, 7, 1);
        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + CountriesContract.TABLE_NAME + " ADD COLUMN " + CountriesContract.FLAG + " " + BaseProviderHelper.TYPE_TEXT);
            db.execSQL("ALTER TABLE " + FieldsTypesContract.TABLE_NAME + " ADD COLUMN " + FieldsTypesContract.SUGGEST + " " + BaseProviderHelper.TYPE_INTEGER);
            db.execSQL("ALTER TABLE " + FieldsTypesContract.TABLE_NAME + " ADD COLUMN " + FieldsTypesContract.REMINDER + " " + BaseProviderHelper.TYPE_INTEGER);
            db.execSQL(new TaxProviderHelper().getCreateTableSQL());
            increaseRestTaskTypes(db, 10, 15, 2);
            increaseRestTaskTypes(db, 6, 9, 1);
        }

        if (oldVersion < 4) {
            db.execSQL(new FormsProviderHelper().getCreateTableSQL());
            db.execSQL(new FormFieldsProviderHelper().getCreateTableSQL());
            increaseRestTaskTypes(db, 12, 17, 1);
        }
    }

    /**
     * increase all "type"-column values in range [from, to] of "rest_tasks" table on value
     * @param db
     * @param from
     * @param to
     * @param value - to increase table values
     */
    private void increaseRestTaskTypes(SQLiteDatabase db, int from, int to, int value) {
        try {
            db.execSQL("UPDATE " + RestTasksContract.TABLE_NAME
                    + " SET " + RestTasksContract.TYPE + " = " + RestTasksContract.TYPE + " + " + value
                    + " WHERE " + RestTasksContract.TYPE + " >= " + from
                    + " AND " + RestTasksContract.TYPE + " <= " + to);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}