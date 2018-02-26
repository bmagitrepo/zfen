package com.zoomlee.zoo;

import android.database.Cursor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;

import com.zoomlee.zoo.provider.ZoomleeProvider;
import com.zoomlee.zoo.provider.helpers.BaseProviderHelper;
import com.zoomlee.zoo.provider.helpers.PersonsProviderHelper;

public class ContentProviderTest extends ProviderTestCase2<ZoomleeProvider> { // Extend your base class and replace the generic with your content provider

    private static final String TAG = ContentProviderTest.class.getName();

    private static MockContentResolver resolve; // in the test case scenario, we use the MockContentResolver to make queries

    public ContentProviderTest(Class<ZoomleeProvider> providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
    }


    public ContentProviderTest() {
        super(ZoomleeProvider.class, "BaseProviderHelper.CONTENT_AUTHORITY"); // TODO: fix it
    }


    @Override
    public void setUp() {
        try {
            Log.i(TAG, "Entered Setup");
            super.setUp();
            resolve = this.getMockContentResolver();
        }
        catch(Exception e) {


        }
    }

    @Override
    public void tearDown() {
        try{
            super.tearDown();
        }
        catch(Exception e) {

        }
    }

    public void testCase() {
        Log.i("TAG","Basic Insert Test");
    }

    public void testPreconditions() {
        // using this test to check data already inside my asana profile

        Log.i("TAG","Test Preconstructed Database");
        String[] projection = {BaseProviderHelper.DataColumns.REMOTE_ID, BaseProviderHelper.DataColumns._ID};
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        Cursor result = resolve.query(PersonsProviderHelper.PersonsContract.CONTENT_URI, projection, selection, selectionArgs, sortOrder);

        assertEquals(0, result.getCount()); //check number of returned rows
        assertEquals(2, result.getColumnCount()); //check number of returned columns

    }

}