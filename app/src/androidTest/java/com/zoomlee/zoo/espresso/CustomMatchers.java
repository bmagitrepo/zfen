package com.zoomlee.zoo.espresso;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class CustomMatchers {

    /**
     * Matches {@link android.support.v4.view.ViewPager} on selected page.
     *
     * @param selectedPage to check
     * @return new instance of matcher
     */
    public static Matcher<View> selectedPage(final int selectedPage) {
        return new BaseMatcher<View>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof ViewPager) {
                    ViewPager pager = (ViewPager) o;
                    return pager.getCurrentItem() == selectedPage;
                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("ViewPager selected page should be " + selectedPage);
            }
        };
    }

    /**
     * @return matcher on not empty recycler view
     */
    public static Matcher<View> notEmpty() {
        return new BaseMatcher<View>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof RecyclerView) {
                    RecyclerView view = (RecyclerView) o;
                    RecyclerView.Adapter adapter = view.getAdapter();

                    return adapter.getItemCount() > 0;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Recycler view shouldn't be empty");
            }
        };
    }
}
