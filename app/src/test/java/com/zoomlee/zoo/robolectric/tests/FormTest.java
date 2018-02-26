package com.zoomlee.zoo.robolectric.tests;

import android.content.Context;

import com.zoomlee.zoo.BuildConfig;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Form;
import com.zoomlee.zoo.net.model.FormField;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.robolectric.CustomRobolectricRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(CustomRobolectricRunner.class)
@Config(emulateSdk = 21, reportSdk = 21, constants = BuildConfig.class)
public class FormTest {

    private DaoHelper<Form> formDaoHelper;
    private DaoHelper<Person> personDaoHelper;
    private DaoHelper<FormField> formFieldDaoHelper;
    private Context context = RuntimeEnvironment.application;

    @Before
    public void setUp() throws Exception {
        formDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Form.class);
        personDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
        formFieldDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(FormField.class);
    }

    @Test
    public void testShouldBeNotEmpty() {
        Form form = new Form();
        form.setRemoteId(1);

        formDaoHelper.saveLocalChanges(context, form);
        formDaoHelper.saveLocalChanges(context, form);
        List<Form> formList = formDaoHelper.getAllItems(context);

        assertEquals(1, formList.size());
        Form savedForm = formList.get(0);
        assertEquals(form.getRemoteId(), savedForm.getRemoteId());
    }

    @Test
    public void testShouldBeEmpty() {
        assertTrue(formDaoHelper.getAllItems(context).isEmpty());
    }

    @Test
    public void testShouldBeTwoForm() {
        Form form = new Form();
        form.setRemoteId(1);
        formDaoHelper.saveLocalChanges(context, form);
        form.setId(-1);
        form.setRemoteId(2);
        formDaoHelper.saveLocalChanges(context, form);
        assertEquals(2, formDaoHelper.getAllItems(context).size());
        assertEquals(0, formFieldDaoHelper.getAllItems(context).size());
    }

    @Test
    public void testSaveRemoteChanges() {
        Form form = new Form();
        form.setRemoteId(1);
        formDaoHelper.saveRemoteChanges(context, form);
        assertEquals(1, formDaoHelper.getAllItems(context).size());
    }

    @Test
    public void testUpdateRemoteChanges() {
        Form form = new Form();

        List<FormField> formFields = new ArrayList<>();
        FormField formField1 = new FormField();
        formField1.setRemoteId(1);
        formField1.setFieldTypeId(1);
        formField1.setValue("bla");
        formFields.add(formField1);
        form.setData(formFields);
        form.setRemoteId(1);

        formDaoHelper.saveLocalChanges(context, form);
        assertEquals(1, formFieldDaoHelper.getAllItems(context).size());
        List<Form> formList = formDaoHelper.getAllItems(context);
        assertEquals(1, formList.size());
        assertEquals(1, formList.get(0).getData().size());

        FormField formField2 = new FormField();
        formField2.setRemoteId(2);
        formField2.setFieldTypeId(2);
        formField2.setValue("bla-bla");
        FormField formField3 = new FormField();
        formField3.setRemoteId(3);
        formField3.setFieldTypeId(3);
        formField3.setValue("bla-bla-bla");

        formFields = new ArrayList<>();
        formFields.add(formField2);
        formFields.add(formField3);
        form.setData(formFields);
        form.setId(-1);

        formDaoHelper.saveRemoteChanges(context, form);
        formList = formDaoHelper.getAllItems(context);

        assertEquals(1, formList.size());
        assertEquals(2, formFieldDaoHelper.getAllItems(context).size());
        Form savedForm = formList.get(0);
        assertEquals(2, savedForm.getData().size());
        if (savedForm.getData().get(0).getRemoteId() == formField2.getRemoteId()) {
            compareFormFields(savedForm.getData().get(0), formField2);
            compareFormFields(savedForm.getData().get(1), formField3);
        } else {
            compareFormFields(savedForm.getData().get(0), formField3);
            compareFormFields(savedForm.getData().get(1), formField2);
        }
    }

    private void compareFormFields(FormField expectField, FormField actualField) {
        assertEquals(expectField.getRemoteId(), actualField.getRemoteId());
        assertEquals(expectField.getFieldTypeId(), actualField.getFieldTypeId());
        assertEquals(expectField.getLocalFormId(), actualField.getLocalFormId());
        assertEquals(expectField.getValue(), actualField.getValue());
    }

    @Test
    public void testDeletePerson() {
        Person person = new Person();
        person.setRemoteId(2);
        personDaoHelper.saveRemoteChanges(context, person);

        Form form = new Form();
        List<FormField> formFields = new ArrayList<>();
        FormField formField1 = new FormField();
        formField1.setRemoteId(1);
        formField1.setFieldTypeId(1);
        formField1.setValue("bla");
        formFields.add(formField1);
        form.setData(formFields);
        form.setRemoteId(1);
        form.setRemotePersonId(person.getRemoteId());

        formDaoHelper.saveRemoteChanges(context, form);

        assertEquals(1, formDaoHelper.getAllItems(context).size());
        assertEquals(person.getId(), formDaoHelper.getAllItems(context).get(0).getLocalPersonId());

        personDaoHelper.deleteItem(context, person);

        assertEquals(0, formDaoHelper.getAllItems(context).size());
        assertEquals(0, formFieldDaoHelper.getAllItems(context).size());
    }
}