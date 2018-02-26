package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.scopes.data.category.CategoriesByPersonTask;
import com.zoomlee.zoo.scopes.opendocument.DaggerSelectCategoryComponent;
import com.zoomlee.zoo.scopes.opendocument.SelectCategoryComponent;
import com.zoomlee.zoo.scopes.opendocument.SelectCategoryModule;
import com.zoomlee.zoo.ui.view.selectcategory.SelectCategoryView;
import com.zoomlee.zoo.utils.ActivityUtils;
import com.zoomlee.zoo.utils.RequestCodes;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import de.greenrobot.event.EventBus;

public class SelectCategoryActivity extends UnsecuredActivity implements SelectCategoryView.Presenter {

    public static final String EXTRA_PERSON_ID = "extra_person_id";

    @BindView(R.id.select_category_view)
    SelectCategoryView selectCategoryView;

    private GetTask task;
    private int personId;

    /**
     * Starts category selection step.
     * Returns result from the next step and {@link #RESULT_CANCELED} in case of just backing.
     *
     * @param activity         to start from
     * @param personId         from the previous step
     * @param attachmentIntent with attachment data
     * @see SelectDocumentActivity#startForResult SelectDocumentActivity.startForResult for more info
     */
    public static void startForResult(Activity activity, int personId, Intent attachmentIntent, int requestCode) {
        attachmentIntent.setComponent(new ComponentName(activity, SelectCategoryActivity.class));
        attachmentIntent.putExtra(EXTRA_PERSON_ID, personId);
        activity.startActivityForResult(attachmentIntent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_category_view);

        ButterKnife.bind(this);

        // create object pool and satisfy our dependencies from it
        createComponent().injectView(selectCategoryView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        personId = getIntent().getIntExtra(EXTRA_PERSON_ID, 0);

        // show profile picture
        updateABAvatar(personId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.SELECT_DOCUMENT) {
            switch (resultCode) {
                case RESULT_OK:
                case SecuredActionBarActivity.RESULT_PIN_FAILED:
                    // propagate result "up" the stream
                    setResult(resultCode);
                    ActivityUtils.finishAndRemoveTask(this);
                    break;

                default:
                    // we get here from next step by "back" or "up" button
                    unpin();
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private SelectCategoryComponent createComponent() {
        return DaggerSelectCategoryComponent.builder()
                .selectCategoryModule(new SelectCategoryModule(this))
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadCategories();
        EventBus.getDefault().register(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(Events.DocumentChanged event) {
        loadCategories();
    }

    private void loadCategories() {
        if (task == null) {
            // load categories
            task = new GetTask(this);
            task.execute(personId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (task != null) {
            // stop working, indicate about it
            task.cancel(true);
            task = null;
        }
    }

    @Override
    public void selectCategory(Category category) {
        SelectDocumentActivity.startForResult(this, category.getRemoteId(), getIntent(), RequestCodes.SELECT_DOCUMENT);
    }

    private class GetTask extends CategoriesByPersonTask {

        public GetTask(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(@NonNull List<Category> categories) {
            selectCategoryView.setCategories(categories);

            task = null;
        }
    }
}
