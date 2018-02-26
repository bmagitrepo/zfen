package com.zoomlee.zoo.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.net.model.Color;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.DocumentsType;
import com.zoomlee.zoo.net.model.Field;
import com.zoomlee.zoo.net.model.File;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.net.model.helpers.DocumentCreator;
import com.zoomlee.zoo.ui.fragments.EditDocAttachmentsFragment;
import com.zoomlee.zoo.ui.fragments.EditDocDataFieldsFragment;
import com.zoomlee.zoo.ui.fragments.EditDocGeneralFragment;
import com.zoomlee.zoo.ui.fragments.EditDocTagsFragment;
import com.zoomlee.zoo.ui.fragments.FragmentWithBottomDialog;
import com.zoomlee.zoo.ui.fragments.dialog.ImagePickerFragment;
import com.zoomlee.zoo.ui.view.BetterViewPager;
import com.zoomlee.zoo.ui.view.SlidingTabLayout;
import com.zoomlee.zoo.utils.ActivityUtils;
import com.zoomlee.zoo.utils.BillingUtils;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.FileUtil;
import com.zoomlee.zoo.utils.GAEvents;
import com.zoomlee.zoo.utils.GAUtil;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;

import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;

public class CreateEditDocActivity extends SecuredActionBarActivity implements ImagePickerFragment.OnImagePickedListener {

    private static final String DOCUMENT_KEY = "document";
    public static final String EXTRA_DOCUMENT_ID = "zoomlee_extra_document_id";
    public static final String EXTRA_FROM_OPEN_IN = "zoomlee_extra_from_open_in";
    public static final String EXTRA_ATTACHMENT_PATH = "zoomlee_extra_attachment_path";
    public static final String EXTRA_ATTACHMENT_TYPE = "zoomlee_extra_attachment_type";
    public static final String EXTRA_TAG_ID = "zoomlee_EXTRA_TAG_ID";
    public static final String EXTRA_INITIAL_TAB = "zoomlee_extra_initial_tab";
    public static final int CREATE_DOCUMENT_ID = -1;

    public static final int GENERAL_TAB_IND = 0;
    public static final int DATA_FIELDS_TAB_IND = 1;
    public static final int ATTACHMENTS_TAB_IND = 2;
    public static final int TAGS_TAB_IND = 3;

    private EditDocGeneralFragment generalFragment;
    private EditDocDataFieldsFragment dataFieldsFragment;
    private EditDocAttachmentsFragment attachmentsFragment;
    private EditDocTagsFragment tagsFragment;
    public Document curDocument;

    private BetterViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private SlidingTabLayout mSlidingTabLayout;
    private boolean isModifiedActionBar = false;
    private View wrapper;

    /**
     * Starts activity for creation.
     *
     * @param activity to start from
     */
    public static void startActivity(Activity activity) {
        startActivity(activity, -1);
    }

    /**
     * Start activity for editing.
     *
     * @param activity   to start from
     * @param documentId to edit
     */
    public static void startActivity(Activity activity, int documentId) {
        startOnTab(activity, documentId, GENERAL_TAB_IND);
    }

    public static void startManageTags(Activity activity, int documentId) {
        startOnTab(activity, documentId, TAGS_TAB_IND);
    }

    public static void startWithTag(Activity activity, int localTagId) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TAG_ID, localTagId);
        startWithIntent(activity, CREATE_DOCUMENT_ID, GENERAL_TAB_IND, intent);
    }

    public static void withAttachment(Activity activity, String path, File.Type fileType, int documentId) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FROM_OPEN_IN, true);
        intent.putExtra(EXTRA_ATTACHMENT_PATH, path);
        intent.putExtra(EXTRA_ATTACHMENT_TYPE, fileType);
        startWithIntent(activity, documentId, ATTACHMENTS_TAB_IND, intent);
    }

    private static void startOnTab(Activity activity, int documentId, int initialTab) {
        startWithIntent(activity, documentId, initialTab, new Intent());
    }

    private static void startWithIntent(Activity activity, int documentId, int initialTab, Intent intent) {
        // direct to this activity
        intent.setComponent(new ComponentName(activity, CreateEditDocActivity.class));
        // remove pin protection
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        // set document
        intent.putExtra(EXTRA_DOCUMENT_ID, documentId);
        intent.putExtra(EXTRA_INITIAL_TAB, initialTab);

        if (documentId == CREATE_DOCUMENT_ID)
            activity.startActivityForResult(intent, RequestCodes.CREATE_DOCUMENT);
        else
            activity.startActivityForResult(intent, RequestCodes.EDIT_DOCUMENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_doc);

        wrapper = findViewById(R.id.wrapper);
        DeveloperUtil.michaelLog("CreateEditDocActivity.onCreate");

        // create fragments for tabs
        generalFragment = EditDocGeneralFragment.newInstance();
        dataFieldsFragment = EditDocDataFieldsFragment.newInstance();
        attachmentsFragment = EditDocAttachmentsFragment.newInstance();
        tagsFragment = EditDocTagsFragment.newInstance();

        mViewPager = (BetterViewPager) findViewById(R.id.viewpager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

        if (savedInstanceState != null) {
            curDocument = savedInstanceState.getParcelable(DOCUMENT_KEY);
        } else {
            int documentId = getIntent().getIntExtra(EXTRA_DOCUMENT_ID, CREATE_DOCUMENT_ID);
            if (documentId != CREATE_DOCUMENT_ID) {
                DaoHelper<Document> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
                curDocument = daoHelper.getItemByLocalId(this, documentId);
            } else {
                int localTagId = getIntent().getIntExtra(EXTRA_TAG_ID, -1);
                curDocument = DocumentCreator.createNewDocument(this);
                DocumentCreator.prefillFields(this, curDocument);
                DocumentCreator.addTagToDoc(this, curDocument, localTagId);
            }
        }

        // here we must switch to the attachments
        mViewPager.setCurrentItem(getIntent().getIntExtra(EXTRA_INITIAL_TAB, GENERAL_TAB_IND));

        getSupportActionBar().setTitle(curDocument.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateABAvatar(curDocument.getLocalPersonId());
        ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                DeveloperUtil.michaelLog();
                StringBuilder screenNameBuilder = new StringBuilder();
                screenNameBuilder
                        .append(curDocument.getId() == -1 ? GAEvents.ADD_DOCUMENT : GAEvents.EDIT_DOCUMENT)
                        .append(" - ")
                        .append(mSectionsPagerAdapter.getPageTitleNormalCase(position));
                GAUtil.getUtil().timeSpent(screenNameBuilder.toString());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        mSlidingTabLayout.setOnPageChangeListener(listener);
        listener.onPageSelected(0);

        setShowShadow(false);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getToolbar() != null) {
            getToolbar().setNavigationIcon(R.drawable.ic_nav_close);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        String attachmentPath = getIntent().getStringExtra(EXTRA_ATTACHMENT_PATH);
        if (fragment instanceof EditDocAttachmentsFragment && !TextUtils.isEmpty(attachmentPath)) {
            File.Type fileType = (File.Type) getIntent().getSerializableExtra(EXTRA_ATTACHMENT_TYPE);
            ((EditDocAttachmentsFragment) getCurrentFragment()).setInitialAttachment(attachmentPath, fileType);
            // remove to avoid multiple file attaching
            getIntent().removeExtra(EXTRA_ATTACHMENT_PATH);
        }
    }

    public void modifyActionBar() {
        DeveloperUtil.michaelLog();
        isModifiedActionBar = true;
        if (generalFragment.getActivity() != null)
            curDocument.setName(generalFragment.getDocumentName());
        final float yTransition = 0 - mSlidingTabLayout.getHeight();
        wrapper.animate().translationY(yTransition).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                DeveloperUtil.michaelLog();
                mSlidingTabLayout.setVisibility(View.GONE);
                wrapper.animate().translationY(0).setDuration(0).setListener(null).start();
            }
        }).start();

        // disable scrolling for swipable container
        mViewPager.setEnableScrolling(!isModifiedActionBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.add_tag);
        showOnlyName(true);
    }

    public void defaultActionBar() {
        isModifiedActionBar = false;
        mSlidingTabLayout.setVisibility(View.VISIBLE);
        final float yTransition = 0 - mSlidingTabLayout.getHeight();
        wrapper.animate().translationY(yTransition).setDuration(0).start();
        wrapper.animate().translationY(0).setDuration(300).start();

        // enable scrolling for swipable container
        mViewPager.setEnableScrolling(!isModifiedActionBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(curDocument.getName());
        showOnlyName(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_edit_doc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isFromOpenIn()) {
                    // we need "up" navigation
                    Intent upIntent = NavUtils.getParentActivityIntent(this);
                    upIntent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();

                    ActivityUtils.finishAndRemoveTask(this);
                } else {
                    // default behaviour is what we need
                    onBackPressed();
                }
                return true;
            case R.id.action_save:
                if (isModifiedActionBar)
                    saveTags();
                else
                    saveDocument();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveTags() {
        tagsFragment.acceptTags();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateDocument();
        outState.putParcelable(DOCUMENT_KEY, curDocument);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int fragmentRequestCode = requestCode & 0xffff;
        switch (fragmentRequestCode) {
            case RequestCodes.PICK_FROM_CAMERA:
            case RequestCodes.PICK_FROM_GALLERY:
            case RequestCodes.CROP_IMAGE_REQUEST:
            case RequestCodes.OPEN_ATTACHMENT:
                unpin();
                return;

            case RequestCodes.SUBSCRIPTION_ACTIVITY:
                unpin();
                BillingUtils.IntendedAction intendedAction = BillingUtils.intendedAction(resultCode, data);
                if (intendedAction.actionType == BillingUtils.ActionType.SELECT_PERSON && intendedAction.success) {
                    generalFragment.changePerson();
                }
                return;
        }

        switch (requestCode) {
            case RequestCodes.SELECT_PERSON:
                unpin();
                if (resultCode == RESULT_OK && generalFragment != null) {
                    Person selectedPerson = data.getParcelableExtra(PersonListActivity.EXTRA_PERSON_SELECTED);
                    curDocument.setLocalPersonId(selectedPerson.getId());
                    updateABAvatar(selectedPerson);
                    generalFragment.updatePerson(selectedPerson);
                }
                break;
            case RequestCodes.CATEGORY_TYPES_ACTIVITY:
                unpin();
                if (resultCode == RESULT_OK && generalFragment != null) {
                    Category selectedCategory = data.getParcelableExtra(IntentUtils.EXTRA_CATEGORY_SELECTED);
                    DocumentsType selectedType = data.getParcelableExtra(IntentUtils.EXTRA_DOC_TYPE_SELECTED);
                    updateCategoryType(selectedCategory, selectedType);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Fragment curFragment = getCurrentFragment();
        if (curFragment instanceof FragmentWithBottomDialog && ((FragmentWithBottomDialog) curFragment).isDialogOpened()) {
            ((FragmentWithBottomDialog) curFragment).closeDialog();
            return;
        }
        deleteUnsavedFiles();
        super.onBackPressed();
    }

    @Override
    public void onImagePicked(java.io.File image) {
        EventBus.getDefault().postSticky(new EditDocAttachmentsFragment.FileObtainedEvent(image));
    }

    /**
     * @return true if we came here from open in flow
     */
    private boolean isFromOpenIn() {
        return getIntent().getBooleanExtra(EXTRA_FROM_OPEN_IN, false);
    }

    private void deleteUnsavedFiles() {
        List<File> files = curDocument.getFilesList();
        for (File file : files)
            if (file.getId() == -1)
                FileUtil.deleteFile(file);
    }

    private void saveDocument() {
        updateDocument();
        boolean isNewDoc = curDocument.getId() == -1;
        DaoHelper<Document> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
        daoHelper.saveLocalChanges(this, curDocument);

        if (isNewDoc) {
            GAUtil.getUtil().eventMade(GAEvents.ACTION_DOCUMENTS_ADDED, null);
            String label = curDocument.getCategoryName() + ":" + curDocument.getTypeName();
            GAUtil.getUtil().eventMade(GAEvents.ACTION_DOCUMENTS_ADDED_TO_CATEGORY, label);
            List<File> filesList = curDocument.getFilesList();
            for (File file : filesList)
                GAUtil.getUtil().eventMade(GAEvents.ACTION_FILES_ADDED_TO_DOCUMENT, label);
        }
        if (isNewDoc)
            DocumentDetailsActivity.startActivity(this, curDocument.getId(), curDocument.getLocalPersonId());
        else
            setResult(Activity.RESULT_OK);
        finish();
    }

    private void updateDocument() {
        String notes = !generalFragment.isAdded() ? curDocument.getNotes() : generalFragment.getDocumentNotes();
        String name = !generalFragment.isAdded() ? curDocument.getName() : generalFragment.getDocumentName();
        List<Field> fields = !dataFieldsFragment.isAdded() ? curDocument.getFieldsList() : dataFieldsFragment.getFields();
        List<File> files = !attachmentsFragment.isAdded() ? curDocument.getFilesList() : attachmentsFragment.getFilesList();
        List<Tag> tags = !tagsFragment.isAdded() ? curDocument.getTagsList() : tagsFragment.getTagList();
        if (generalFragment.isAdded()) {
            Color color = generalFragment.getDocumentColor();
            Person owner = generalFragment.getOwner();

            curDocument.setColorHEX(color.getHex());
            curDocument.setColorId(color.getRemoteId());
            curDocument.setColorName(color.getName());
            curDocument.setLocalPersonId(owner.getId());
        }
        curDocument.setNotes(notes);
        curDocument.setName(TextUtils.isEmpty(name) ? getString(R.string.other_document) : name);
        curDocument.setFieldsList(fields);
        curDocument.setFilesList(files);
        curDocument.setTagsList(tags);
    }

    private Fragment getCurrentFragment() {
        return mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem());
    }

    private void updateCategoryType(Category category, DocumentsType docType) {
        if (docType.getRemoteId() == curDocument.getTypeId())
            return;

        if (curDocument.getRemoteId() == -1) {
            String docTypeName = docType.getName();
            curDocument.setName(docTypeName);
        }

        DocumentCreator.updateCategoryType(this, curDocument, category, docType);
        DocumentCreator.prefillFields(this, curDocument);

        generalFragment.updateCategoryType();
        dataFieldsFragment.updateCategoryType();
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case GENERAL_TAB_IND:
                    return generalFragment;
                case DATA_FIELDS_TAB_IND:
                    return dataFieldsFragment;
                case ATTACHMENTS_TAB_IND:
                    return attachmentsFragment;
                case TAGS_TAB_IND:
                    return tagsFragment;
                default:
                    throw new IndexOutOfBoundsException("You want to select tab with index " + position + ", but count of tabs is " + getCount());
            }

        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            return getPageTitleNormalCase(position).toUpperCase(l);
        }

        public String getPageTitleNormalCase(int position) {
            switch (position) {
                case GENERAL_TAB_IND:
                    return getString(R.string.general);
                case DATA_FIELDS_TAB_IND:
                    return getString(R.string.data_fields);
                case ATTACHMENTS_TAB_IND:
                    return getString(R.string.attachemnts);
                case TAGS_TAB_IND:
                    return getString(R.string.tags);
            }
            return "";
        }
    }
}
