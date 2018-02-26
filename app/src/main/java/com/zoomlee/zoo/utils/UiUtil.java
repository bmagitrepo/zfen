package com.zoomlee.zoo.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Person;

import java.io.File;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 1/13/15
 */
public class UiUtil {

    public static void loadPersonIcon(Person person, ImageView imageView, int stubRes) {
        Picasso picasso = PicassoUtil.getInstance();
        if (person.getImageLocal144Path() != null && new File(person.getImageLocal144Path()).exists()) {
            picasso.load(new File(person.getImageLocal144Path()))
                    .placeholder(stubRes)
                    .transform(new CircleTransform())
                    .into(imageView);
        } else {
            picasso.load(person.getImageRemote144Path())
                    .placeholder(stubRes)
                    .transform(new CircleTransform())
                    .into(imageView);
        }
    }

    public static void loadPersonIcon(Person person, ImageView imageView, boolean white) {
        int iconRes = white ? R.drawable.stub_person_white : R.drawable.stub_person_green;
        Picasso picasso = PicassoUtil.getInstance();
        if (person.getImageLocal144Path() != null && new File(person.getImageLocal144Path()).exists())
            picasso.load(new File(person.getImageLocal144Path())).placeholder(iconRes).into(imageView);
        else
            picasso.load(person.getImageRemote144Path()).placeholder(iconRes).into(imageView);
    }

    public static void loadFilePreview(com.zoomlee.zoo.net.model.File file, ImageView imageView) {
        Picasso picasso = PicassoUtil.getInstance();

        if (file.getLocalPath() != null && new File(file.getLocalPath()).exists())
            picasso.load(new File(file.getLocalPath())).placeholder(R.drawable.other_file).into(imageView);
        else
            picasso.load(file.getRemotePath()).placeholder(R.drawable.other_file).into(imageView);
    }

    /**
     * Customizes menu to add search facility.
     *
     * @param activity to use
     * @param menu     to be inflated into
     * @param listener to handle search requests
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void customizeMenuForSearch(Activity activity, Menu menu, SearchView.OnQueryTextListener listener) {
        activity.getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        android.support.v7.widget.SearchView searchView = null;
        if (searchItem != null) {
            searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchItem);
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));

            searchView.setOnQueryTextListener(listener);
            searchView.setIconifiedByDefault(false);
            UiUtil.customizeSearchView(searchView);
        }

    }

    /**
     * Makes up the search action view by brand book.
     *
     * @param searchView to be set up
     */
    private static void customizeSearchView(ViewGroup searchView) {
        Resources res = searchView.getResources();
        int searchPlateId = R.id.search_plate;
        View searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate != null) {

            searchPlate.setBackgroundResource(android.R.color.transparent);
            int searchTextId = R.id.search_src_text;
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText != null) {
                searchText.setHint(R.string.search);
                searchText.setTextColor(res.getColor(R.color.white));
                searchText.setHintTextColor(res.getColor(R.color.unselected_tab));
                searchText.setBackgroundResource(R.color.green_zoomlee);
            }

            int searchMagIconId = R.id.search_mag_icon;
            ImageView searchMagIcon = (ImageView) searchView.findViewById(searchMagIconId);
            if (searchMagIcon != null) {
                DeveloperUtil.michaelLog();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) searchMagIcon.getLayoutParams();
                params.leftMargin = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    params.setMarginStart(0);
                searchMagIcon.setLayoutParams(params);
                searchMagIcon.setImageResource(R.drawable.icon_search);
            }
        }
    }

    public static void hide(View view) {
        view.setVisibility(View.GONE);
    }

    public static void show(View view) {
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Shows view using fade in animation.
     *
     * @param view to show
     */
    public static void fadeIn(View view) {
        view.setAlpha(0);
        UiUtil.show(view);
        view.animate().alpha(1).setListener(null).start();
    }

    /**
     * Hides view out using fade out animation.
     *
     * @param view to hide
     */
    public static void fadeOut(final View view) {
        view.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                UiUtil.hide(view);
            }
        }).start();
    }
}
