package com.zoomlee.zoo.robolectric.tests;

import android.content.Context;

import com.zoomlee.zoo.BuildConfig;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.robolectric.CustomRobolectricRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(CustomRobolectricRunner.class)
@Config(emulateSdk = 21, reportSdk = 21, constants = BuildConfig.class)
public class PersonTest {

    private DaoHelper<Person> personDaoHelper;
    private Context context = RuntimeEnvironment.application;

    @Before
    public void setUp() throws Exception {
        personDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
    }

    @Test
    public void testShouldBeNotEmpty() {
        Person person = new Person();
        personDaoHelper.saveLocalChanges(context, person);
        personDaoHelper.saveLocalChanges(context, person);
        assertEquals(1, personDaoHelper.getAllItems(context).size());
    }

    @Test
    public void testShouldBeEmpty() {
        assertTrue(personDaoHelper.getAllItems(context).isEmpty());
    }

    @Test
    public void testShouldBeTwoPersons() {
        Person person = new Person();
        personDaoHelper.saveLocalChanges(context, person);
        person.setId(-1);
        personDaoHelper.saveLocalChanges(context, person);
        assertEquals(2, personDaoHelper.getAllItems(context).size());
    }
}