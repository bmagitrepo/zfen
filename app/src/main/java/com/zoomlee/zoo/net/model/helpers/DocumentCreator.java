package com.zoomlee.zoo.net.model.helpers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.ZoomleeApp;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.net.model.Color;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.DocumentsType;
import com.zoomlee.zoo.net.model.DocumentsType2Field;
import com.zoomlee.zoo.net.model.Field;
import com.zoomlee.zoo.net.model.FieldsType;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.provider.helpers.FieldsHelper;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.zoomlee.zoo.provider.helpers.DocumentsTypes2FieldTypesHelper.DocumentsTypes2FieldTypesContract;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 30.03.15.
 */
public class DocumentCreator {

    public static Document createNewDocument(Activity activity) {
        ZoomleeApp app = (ZoomleeApp) activity.getApplication();
        Category category = Category.TRAVEL_CATEGORY;
        DocumentsType docType = DocumentsType.PASSPORT_DOC_TYPE;
        Document newDocument = new Document();
        Person owner = getOwnerForDocument(app);

        newDocument.setName(docType.getName());
        newDocument.setCategoryId(category.getRemoteId());
        newDocument.setCategoryName(category.getName());
        newDocument.setCategoryWeight(category.getWeight());
        newDocument.setTypeId(docType.getRemoteId());
        newDocument.setTypeName(docType.getName());
        newDocument.setUserId(app.getSelectedPersonId());
        newDocument.setLocalPersonId(owner.getId());
        fillColor(activity, newDocument);
        initFields(activity, newDocument);

        return newDocument;
    }

    public static void prefillFields(Context context, Document document) {
        DaoHelper<Person> daoHelperPerson = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
        int ownerPersonId = document.getLocalPersonId();
        Person owner = ownerPersonId == Person.ME_ID ? SharedPreferenceUtils.getUtils().getUserSettings()
                : daoHelperPerson.getItemByLocalId(context, ownerPersonId);

        Set<String> typeSet = new HashSet<>();
        for (Field field : document.getFieldsList()) {
            if (field.getSuggest() == FieldsType.NOT_SUGGEST) continue;

            typeSet.add(String.valueOf(field.getFieldTypeId()));
            if (field.getFieldTypeId() == FieldsType.FIRST_NAME_TYPE_ID)
                field.setValue(owner.getName());
        }

        List<String> argsList = new ArrayList<>();
        argsList.add(String.valueOf(document.getLocalPersonId()));
        argsList.addAll(typeSet);

        String[] args = argsList.toArray(new String[]{});
        Cursor cursor = context.getContentResolver().query(FieldsHelper.FieldsHistoryContract.CONTENT_URI, null, null, args, null);
        if (cursor == null) return;

        int fieldTypeIdIndex = Util.findIndex(FieldsHelper.FieldsHistoryContract.ALL_COLUMNS_PROJECTION, FieldsHelper.FieldsHistoryContract.FIELD_TYPE_ID);
        int fieldValueIndex = Util.findIndex(FieldsHelper.FieldsHistoryContract.ALL_COLUMNS_PROJECTION, FieldsHelper.FieldsHistoryContract.FIELD_VALUE);

        List<Field> fieldsToFill = new ArrayList<>(document.getFieldsList());

        while (cursor.moveToNext()) {
            int fieldTypeId = cursor.getInt(fieldTypeIdIndex);
            String fieldValue = cursor.getString(fieldValueIndex);

            Iterator<Field> iterator = fieldsToFill.iterator();
            while (iterator.hasNext()) {
                Field field = iterator.next();
                if (field.getFieldTypeId() == fieldTypeId) {
                    field.setValue(fieldValue);
                    iterator.remove();
                }
            }
        }

        cursor.close();
    }

    public static void addTagToDoc(Context ctx, Document doc, int localTagId) {
        if (localTagId == -1)
            return;
        DaoHelper<Tag> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Tag.class);
        Tag tag = daoHelper.getItemByLocalId(ctx, localTagId);
        doc.getTagsList().add(tag);

    }

    public static void updateCategoryType(Context context, Document document, Category category, DocumentsType docType) {
        document.setCategoryId(category.getRemoteId());
        document.setCategoryName(category.getName());
        document.setCategoryWeight(category.getWeight());
        document.setTypeId(docType.getRemoteId());
        document.setTypeName(docType.getName());

        initFields(context, document);
    }

    private static Person getOwnerForDocument(ZoomleeApp app) {
        DaoHelper<Person> daoHelperPerson = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
        int ownerPersonId = app.getSelectedPersonId() == Person.ALL_ID ? Person.ME_ID : app.getSelectedPersonId();

        return ownerPersonId == Person.ME_ID ? SharedPreferenceUtils.getUtils().getUserSettings()
                : daoHelperPerson.getItemByLocalId(app, ownerPersonId);
    }

    /**
     * create random color
     *
     * @param document
     * @param context
     */
    private static void fillColor(Context context, Document document) {
        DaoHelper<Color> daoHelperColor = DaoHelpersContainer.getInstance().getDaoHelper(Color.class);
        List<Color> colors = daoHelperColor.getAllItems(context);
        Color color = colors.get(new Random().nextInt(colors.size()));
        document.setColorHEX(color.getHex());
        document.setColorId(color.getRemoteId());
        document.setColorName(color.getName());
    }

    private static void initFields(Context context, Document document) {
        DaoHelper<DocumentsType2Field> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(DocumentsType2Field.class);
        String selection = DocumentsTypes2FieldTypesContract.TABLE_NAME + "." + DocumentsTypes2FieldTypesContract.DOCUMENT_TYPE_ID + "=?";
        List<DocumentsType2Field> list = daoHelper.getAllItems(context, selection,
                DBUtil.getArgsArray(document.getTypeId()), null);

        List<Field> fieldList = new ArrayList<>();
        for (DocumentsType2Field dt2f : list)
            fieldList.add(new Field(dt2f));
        List<Field> oldFieldList = document.getFieldsList();
        for (Field field : oldFieldList)
            if (field.isCustom())
                fieldList.add(field);

        document.setFieldsList(fieldList);
    }
}
