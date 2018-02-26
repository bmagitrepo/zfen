package com.zoomlee.zoo.utils.tax;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Country;
import com.zoomlee.zoo.net.model.Tax;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @since 21.05.15.
 */
public class TaxReportGenerator {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("d MMM yyyy", Locale.US);
    private static final SimpleDateFormat FORMAT_D_MMM_YYYY = new SimpleDateFormat("d-MMM-yyyy", Locale.US);
    private static final SimpleDateFormat FORMAT_D_MMMM_YYYY = new SimpleDateFormat("d MMMM yyyy", Locale.US);

    private static final String REPORT_TEMPLATE_ASSET = "reports/report_template.html";
    private static final String REPORT_ROW_ASSET = "reports/report_row.html";

    private static final String REPORT_TIME_PERIOD_TAG = "<!-- ZOOMLE_REPORT_TIME_PERIOD_TAG -->";
    private static final String REPORT_DAYS_COUNT_TAG = "<!-- ZOOMLE_REPORT_DAYS_COUNT_TAG -->";
    private static final String REPORT_ROWS_TAG = "<!-- ZOOMLE_REPORT_TEMPLATE_TAG -->";

    private static final String TAX_COUNTRY_IMAGE_TAG = "<!-- ZOOMLE_TAX_REPORT_COUNTRY_IMAGE  -->";
    private static final String TAX_COUNTRY_NAME_TAG = "<!-- ZOOMLE_TAX_REPORT_COUNTRY_NAME  -->";
    private static final String TAX_PERIOD_TAG = "<!-- ZOOMLE_TAX_REPORT_PERIOD  -->";
    private static final String TAX_DAYS_COUNT_TAG = "<!-- ZOOMLE_TAX_REPORT_DAYS_COUNT  -->";
    private static final String TAX_DAYS_TEXT_TAG = "<!-- ZOOMLE_TAX_REPORT_DAYS_TEXT  -->";

    private Context context;
    private long toDate;
    private long fromDate;
    private int totalDays;
    private List<Tax> mergedTaxList;

    /**
     *
     * @param context
     * @param fromDate timestamp in milliseconds
     * @param toDate timestamp in milliseconds
     * @param totalDays total days of trips
     * @param taxList filtered taxes in this period
     */
    public TaxReportGenerator(Context context, long fromDate, long toDate, int totalDays, List<Tax> taxList) {
        this.context = context;
        this.toDate = toDate;
        this.fromDate = fromDate;
        this.totalDays = totalDays;
        this.mergedTaxList = mergeTaxes(taxList);
    }

    public File generateHtmlReport(String fileName) {
        File destFile = new File(context.getCacheDir(), fileName + ".html");

        final String reportRowTemplate = getStringFromAssets(REPORT_ROW_ASSET);
        final String reportTemplate = getStringFromAssets(REPORT_TEMPLATE_ASSET);
        if (reportRowTemplate == null || reportTemplate == null) return destFile;

        StringBuilder rows = new StringBuilder();
        for (Tax tax: mergedTaxList) {
            String period = FORMAT.format(new Date(tax.getDisplayArrivalMS())) + " - "
                    + FORMAT.format(new Date(tax.getDisplayDepartureMS()));

            String row = reportRowTemplate.replace(TAX_COUNTRY_IMAGE_TAG, tax.getCountryFlag());
            row = row.replace(TAX_COUNTRY_NAME_TAG, tax.getCountryName());
            row = row.replace(TAX_PERIOD_TAG, period);
            row = row.replace(TAX_DAYS_COUNT_TAG, "" + tax.getDaysCount());
            row = row.replace(TAX_DAYS_TEXT_TAG, getDaysFormat(tax.getDaysCount()));
            rows.append(row);
        }

        String period = FORMAT_D_MMMM_YYYY.format(new Date(fromDate)) + " - " +
                FORMAT_D_MMMM_YYYY.format(new Date(toDate));

        String report = reportTemplate.replace(REPORT_TIME_PERIOD_TAG, period);
        report = report.replace(REPORT_DAYS_COUNT_TAG, totalDays + " " + getDaysFormat(totalDays));
        report = report.replace(REPORT_ROWS_TAG, rows);

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(destFile));
            outputStreamWriter.write(report);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        return destFile;
    }

    public File generateCsvReport(String fileName) {
        File destFile = new File(context.getCacheDir(), fileName + ".csv");

        int countryId = SharedPreferenceUtils.getUtils().getUserSettings().getCountryId();
        DaoHelper<Country> countryDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Country.class);
        Country country = countryDaoHelper.getItemByRemoteId(context, countryId);
        String homeCountry = country == null ? null : country.getName();

        try {
            FileWriter fw = new FileWriter(destFile);

            fw.append("Tax Year/Period of Report");
            fw.append(',');
            fw.append(FORMAT.format(new Date(fromDate)) + " - " + FORMAT.format(new Date(toDate)));
            fw.append(",,\n");

            fw.append(",,,\n");

            fw.append("Home Country");
            fw.append(',');
            fw.append(homeCountry);
            fw.append(",,\n");

            fw.append("Days Traveled");
            fw.append(',');
            fw.append("" + totalDays);
            fw.append(",,\n");

            fw.append(",,,\n");

            fw.append("Country");
            fw.append(',');
            fw.append("From");
            fw.append(',');
            fw.append("To");
            fw.append(',');
            fw.append("Total days");
            fw.append('\n');

            for (Tax tax: mergedTaxList) {
                fw.append(tax.getCountryName());
                fw.append(',');
                fw.append(FORMAT_D_MMM_YYYY.format(new Date(tax.getDisplayArrivalMS())));
                fw.append(',');
                fw.append(FORMAT_D_MMM_YYYY.format(new Date(tax.getDisplayDepartureMS())));
                fw.append(',');
                fw.append("" + tax.getDaysCount());
                fw.append('\n');
            }

            fw.append("Total");
            fw.append(",,,");
            fw.append("" + totalDays);

            // fw.flush();
            fw.close();

        } catch (Exception e) {
        }

        return destFile;
    }

    /**
     *
     * @param daysCount
     * @return "day" or "days"
     */
    private String getDaysFormat(int daysCount) {
        return daysCount < 2 ? "day" : "days";
    }

    private String getStringFromAssets(String assertsName) {
        AssetManager assetManager = context.getAssets();
        BufferedReader r = null;
        try {
            InputStream is = assetManager.open(assertsName);
            r = new BufferedReader(new InputStreamReader(is));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }

            return total.toString();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (r != null) r.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private List<Tax> mergeTaxes(List<Tax> taxList) {
        List<Tax> mergedTaxList = new ArrayList<>();
        for (Tax tax: taxList) {
            int mergedTaxIndex = mergedTaxList.indexOf(tax);
            Tax mergedTax;
            if (mergedTaxIndex == -1) {
                mergedTax = new Tax(tax);
            } else {
                mergedTax = mergedTaxList.get(mergedTaxIndex);
                if (mergedTax.getDisplayDeparture() < tax.getDisplayDeparture())
                    mergedTax.setDisplayDeparture(tax.getDisplayDeparture());
                if (mergedTax.getDisplayArrival() < tax.getDisplayArrival())
                    mergedTax.setDisplayArrival(tax.getDisplayArrival());
            }

            mergedTaxList.add(mergedTax);
        }

        return mergedTaxList;
    }
}