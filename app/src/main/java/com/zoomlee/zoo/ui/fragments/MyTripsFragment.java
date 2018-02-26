package com.zoomlee.zoo.ui.fragments;


import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.dao.TaxDaoHelper;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.Tax;
import com.zoomlee.zoo.provider.FilesProvider;
import com.zoomlee.zoo.ui.MaterialDialog;
import com.zoomlee.zoo.ui.activity.CreateEditTaxActivity;
import com.zoomlee.zoo.ui.activity.SubscriptionActivity;
import com.zoomlee.zoo.ui.adapters.IncitationsAdapter;
import com.zoomlee.zoo.ui.adapters.TaxesAdapter;
import com.zoomlee.zoo.ui.view.DateRangePicker;
import com.zoomlee.zoo.ui.view.EmptyView;
import com.zoomlee.zoo.ui.view.ZMTextView;
import com.zoomlee.zoo.utils.BillingUtils;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.tax.TaxReportGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class MyTripsFragment extends Fragment implements TaxesAdapter.ItemListener, DateRangePicker.RangeSetListener {

    @BindView(R.id.date_range_picker)
    DateRangePicker dateRangePicker;

    @BindView(R.id.text_send_report)
    ZMTextView textReport;

    @BindView(R.id.empty_taxes)
    EmptyView emptyTaxes;

    @BindView(R.id.taxesView)
    ListView listTaxes;

    @BindView(R.id.addNewBtn)
    ImageView addNewBtn;

    @BindView(R.id.lockView)
    View lockView;

    @BindView(R.id.renewBadge)
    ZMTextView renewBadge;

    private TaxesAdapter adapter;
    private DataLoadAsyncTask loadTask;
    private TaxDaoHelper taxDaoHelper = (TaxDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Tax.class);
    private boolean isLock = false;

    public static MyTripsFragment newInstance() {
        return new MyTripsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_trips, container, false);

        ButterKnife.bind(this, view);

        adapter = new TaxesAdapter(getActivity(), this);
        listTaxes.setAdapter(IncitationsAdapter.wrap(adapter, adapter));
        listTaxes.setEmptyView(emptyTaxes);

        dateRangePicker.above(listTaxes);
        dateRangePicker.setRangeSetListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        loadData();

        isLock = !BillingUtils.isPro(SharedPreferenceUtils.getUtils().getUserSettings());
        updateUiLock();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);

        cancelTask();
    }

    private void loadData() {
        cancelTask();
        loadTask = new DataLoadAsyncTask();
        loadTask.execute(dateRangePicker.getFromDate(), dateRangePicker.getToDate());
    }

    private void cancelTask() {
        if (loadTask != null) {
            loadTask.cancel(true);
            loadTask = null;
        }
    }

    @OnClick(R.id.addNewBtn)
    @SuppressWarnings("unused")
    void onAddClicked() {
        CreateEditTaxActivity.startToCreateTax(getActivity());
    }

    @OnClick(R.id.renewBadge)
    @SuppressWarnings("unused")
    void onRenewBadgeClicked() {
        SubscriptionActivity.startActivity(getActivity(), BillingUtils.ActionType.RENEW);
    }

    @OnClick(R.id.text_send_report)
    @SuppressWarnings("unused")
    void onSendClicked() {
        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.setType("message/rfc822");
        i.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.tax_report));

        List<ResolveInfo> resolveInfos = getActivity().getPackageManager().queryIntentActivities(i, 0);
        if (resolveInfos.size() == 0) {
            MaterialDialog mMaterialDialog = new MaterialDialog(getActivity())
                    .setMessage(R.string.no_app_to_share)
                    .setPositiveButton(R.string.ok, null);

            mMaterialDialog.show();
            return;
        }

        TaxReportGenerator taxReportGenerator = new TaxReportGenerator(getActivity(),
                dateRangePicker.getFromDate().getTimeInMillis(), dateRangePicker.getToDate().getTimeInMillis(),
                adapter.getDaysAbroad(), adapter.getData());
        File cvsReport = taxReportGenerator.generateCsvReport("TaxReport");
        File htmlReport = taxReportGenerator.generateHtmlReport("TaxReport");

        ArrayList<Uri> uris = new ArrayList<>();
        Uri cvsFileUri = Uri.parse("content://" + FilesProvider.CACHE_DIR_AUTHORITY + "/"
                + cvsReport.getName());
        Uri htmlFileUri = Uri.parse("content://" + FilesProvider.CACHE_DIR_AUTHORITY + "/"
                + htmlReport.getName());
        uris.add(cvsFileUri);
        uris.add(htmlFileUri);

        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        startActivityForResult(Intent.createChooser(i, null), RequestCodes.SEND_EMAIL);
    }

    @Override
    public void onItemClicked(Tax tax) {
        if (!isLock) {
            CreateEditTaxActivity.startToEditTax(getActivity(), tax);
        }
    }

    @Override
    public void onRangeSet() {
        loadData();
    }

    public void onEventMainThread(final Events.TaxChanged event) {
        loadData();
    }

    public void onEventMainThread(Events.PersonChanged event) {
        if (event.getPerson().getId() == Person.ME_ID) {
            isLock = !BillingUtils.isPro(SharedPreferenceUtils.getUtils().getUserSettings());
            updateUiLock();
        }
    }

    private void updateReportText(int days) {
        SpannableString builder = new SpannableString(getString(R.string.title_send_report, days));
        int start = builder.toString().indexOf('\n');
        int end = builder.length();
        int color = getResources().getColor(R.color.white_50);
        builder.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textReport.setText(builder);
    }

    private void updateUiLock() {
        addNewBtn.setVisibility(isLock ? View.GONE : View.VISIBLE);
        lockView.setVisibility(isLock ? View.VISIBLE : View.GONE);
        textReport.setVisibility(isLock ? View.GONE : View.VISIBLE);
        renewBadge.setVisibility(isLock ? View.VISIBLE : View.GONE);
        adapter.setShowIncitations(!isLock);
        if (isLock) {
            dateRangePicker.setVisibility(View.GONE);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) listTaxes.getLayoutParams();
            params.topMargin = 0;
            listTaxes.setLayoutParams(params);
        } else {
            dateRangePicker.setVisibility(View.VISIBLE);
            dateRangePicker.above(listTaxes);
        }

    }

    private class DataLoadAsyncTask extends AsyncTask<Calendar, Void, List<Tax>> {

        @Override
        protected void onPreExecute() {
            adapter.clear();
        }

        @Override
        protected List<Tax> doInBackground(Calendar... range) {
            if (isLock) {
                return taxDaoHelper.getAllItems(getActivity());
            } else {
                return taxDaoHelper.getTaxInInterval(getActivity(),
                        range[0].getTimeInMillis() / 1000,
                        range[1].getTimeInMillis() / 1000);
            }
        }

        @Override
        protected void onPostExecute(List<Tax> taxes) {
            DeveloperUtil.michaelLog("taxes");
            DeveloperUtil.michaelLog(taxes);
            adapter.setData(taxes);
            updateReportText(adapter.getDaysAbroad());
        }
    }
}
