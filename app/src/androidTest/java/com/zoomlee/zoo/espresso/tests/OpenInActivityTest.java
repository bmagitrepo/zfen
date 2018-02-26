package com.zoomlee.zoo.espresso.tests;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.activity.OpenDocumentActivity;
import com.zoomlee.zoo.utils.IntentUtils;

import java.io.File;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.zoomlee.zoo.espresso.CustomMatchers.notEmpty;
import static com.zoomlee.zoo.espresso.CustomMatchers.selectedPage;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
public class OpenInActivityTest extends ActivityInstrumentationTestCase2<OpenDocumentActivity> {

    private File tempFile;

    public OpenInActivityTest() {
        super(OpenDocumentActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        tempFile = File.createTempFile("test", ".pdf");
        Uri file = Uri.fromFile(tempFile);
        Intent intent = new Intent();
        intent.setData(file);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        setActivityIntent(intent);
        getActivity();
    }

    public void testFileNameDisplayed() {
        onView(withId(R.id.text_file_name)).check(matches(withText(tempFile.getName())));
    }

    public void testCancel() {
        onView(withText(R.string.title_cancel)).perform(click());
        assertEquals(getActivity().isFinishing(), true);
    }

    public void testCreateDocumentFlow() {
        onView(withText(R.string.title_create_new_document)).perform(click());

        onView(withId(R.id.viewpager)).check(matches(selectedPage(2)));
        onView(withId(R.id.files_rv)).check(matches(notEmpty()));
    }

    public void testUpNavigationFromCreateEdit() {
        onView(withText(R.string.title_create_new_document)).perform(click());

        // press "up"
        onView(withContentDescription("Navigate up")).perform(click());

        // check if we are on the main activity by checking if drawer is present
        onView(withId(R.id.navigation_drawer)).check(matches(is(anything())));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        if (tempFile != null) {
            //noinspection ResultOfMethodCallIgnored
            tempFile.delete();
        }
    }
}