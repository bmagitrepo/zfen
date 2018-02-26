package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.net.model.DocumentsType;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.ui.adapters.TypeAdapter;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.UiUtil;

import java.util.List;

public class CategoryTypeListActivity extends SecuredActionBarActivity implements AdapterView.OnItemClickListener {

    public static final String EXTRA_SELECTED_TYPE_ID = "zoomlee_EXTRA_SELECTED_TYPE_ID";
    public static final String EXTRA_DOCUMENT_PERSON_ID = "zoomlee_EXTRA_docs_person_id";

    private View noDataView;
    private List<Category> categories;
    private TypeAdapter adapter;
    private int docsPersonId;

    public static void startForResult(Activity activity, int selectedTypeId, int docsPersonId) {
        Intent intent = new Intent(activity, CategoryTypeListActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        intent.putExtra(EXTRA_SELECTED_TYPE_ID, selectedTypeId);
        intent.putExtra(EXTRA_DOCUMENT_PERSON_ID, docsPersonId);
        activity.startActivityForResult(intent, RequestCodes.CATEGORY_TYPES_ACTIVITY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_category_type_list);

        int selectedTypeId = getIntent().getIntExtra(EXTRA_SELECTED_TYPE_ID, -1);
        docsPersonId = getIntent().getIntExtra(EXTRA_DOCUMENT_PERSON_ID, Person.ME_ID);
        categories = initCategories();
        ListView categoryList = (ListView) findViewById(R.id.categoryTypeList);
        noDataView = findViewById(R.id.noDataView);
        categoryList.setEmptyView(noDataView);

        adapter = new TypeAdapter(this, categories, selectedTypeId);

        categoryList.setAdapter(adapter);
        categoryList.setOnItemClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handleIntent(getIntent());
        updateABAvatar(docsPersonId);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            DeveloperUtil.michaelLog(query);
            adapter.getFilter().filter(query);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeveloperUtil.michaelLog();
        if (adapter.getItemViewType(position) == TypeAdapter.CATEGORY)
            return;
        DocumentsType selectedType = adapter.getType(position);
        Category selectedCategory = adapter.getCategory(position);

        Intent returnedIntent = new Intent();
        returnedIntent.putExtra(IntentUtils.EXTRA_CATEGORY_SELECTED, selectedCategory);
        returnedIntent.putExtra(IntentUtils.EXTRA_DOC_TYPE_SELECTED, selectedType);
        setResult(RESULT_OK, returnedIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        UiUtil.customizeMenuForSearch(this, menu, new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private List<Category> initCategories() {
        DaoHelper<Category> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Category.class);
        List<Category> categories = daoHelper.getAllItems(this);
        Category other = categories.remove(categories.size() - 1);
        categories.add(0, other);
        return categories;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.CREATE_DOCUMENT)
            finish();
    }
}
