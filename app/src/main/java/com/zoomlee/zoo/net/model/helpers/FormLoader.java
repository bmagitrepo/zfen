package com.zoomlee.zoo.net.model.helpers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.SparseIntArray;

import com.radaee.pdf.Document;
import com.radaee.pdf.Matrix;
import com.radaee.pdf.Page;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Country;
import com.zoomlee.zoo.net.model.FieldsType;
import com.zoomlee.zoo.net.model.Form;
import com.zoomlee.zoo.net.model.FormField;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.provider.helpers.FieldsHelper;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.Util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @since 04.06.15.
 */
public class FormLoader {

    // assets/cache file names
    public static final String PDF_FORM_FOR_VIEW = "preview_form.pdf";
    public static final String PDF_FORM_FOR_PRINT = "print_form.pdf";

    private static final SparseIntArray PRE_FILLED_FIELDS = new SparseIntArray();
    static {
        PRE_FILLED_FIELDS.append(FormField.FAMILY_NAME_TYPE_ID, FieldsType.LAST_NAME_TYPE_ID);
        PRE_FILLED_FIELDS.append(FormField.FIRST_NAME_TYPE_ID, FieldsType.FIRST_NAME_TYPE_ID);
        PRE_FILLED_FIELDS.append(FormField.BIRTH_DATE_MOUNTH1_TYPE_ID, FieldsType.DATE_OF_BIRTH_TYPE_ID);
        PRE_FILLED_FIELDS.append(FormField.BIRTH_DATE_MOUNTH2_TYPE_ID, FieldsType.DATE_OF_BIRTH_TYPE_ID);
        PRE_FILLED_FIELDS.append(FormField.BIRTH_DATE_DAY1_TYPE_ID, FieldsType.DATE_OF_BIRTH_TYPE_ID);
        PRE_FILLED_FIELDS.append(FormField.BIRTH_DATE_DAY2_TYPE_ID, FieldsType.DATE_OF_BIRTH_TYPE_ID);
        PRE_FILLED_FIELDS.append(FormField.BIRTH_DATE_YEAR1_TYPE_ID, FieldsType.DATE_OF_BIRTH_TYPE_ID);
        PRE_FILLED_FIELDS.append(FormField.BIRTH_DATE_YEAR2_TYPE_ID, FieldsType.DATE_OF_BIRTH_TYPE_ID);
        PRE_FILLED_FIELDS.append(FormField.PASSPORT_ISSUED_BY_TYPE_ID, FieldsType.COUNTRY_OF_ISSUE_TYPE_ID);
        PRE_FILLED_FIELDS.append(FormField.PASSPORT_NUMBER_TYPE_ID, FieldsType.PASSPORT_NUMBER_TYPE_ID);
        PRE_FILLED_FIELDS.append(FormField.COUNTRY_OF_RESIDENCE_TYPE_ID, -1);
    }

    private Context context;
    private Form form;
    private boolean preFilled;

    public FormLoader(Context context, Form form) {
        this.context = context;
        this.form = form;
    }

    public FormLoader(Context context, Form form, boolean preFilled) {
        this.context = context;
        this.form = form;
        this.preFilled = preFilled;
    }

    /**
     * pre fill form using person's documents fields
     */
    public void preFillFields() {
        if (!preFilled) {
            boolean changed = preFillFieldsFromDocs();
            if (changed) {
                DaoHelper<Form> formDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Form.class);
                formDaoHelper.saveLocalChanges(context, form);
            }
            preFilled = true;
        }
    }

    /**
     * pre fill form using person's documents fields
     * @return true - if changed, false - otherwise
     */
    private boolean preFillFieldsFromDocs() {
        boolean changed = false;
        Set<String> typeSet = new HashSet<>();
        List<FormField> fieldsToFill = new ArrayList<>();
        for (FormField formField : form.getData()) {
            if (formField.getValue() != null || PRE_FILLED_FIELDS.indexOfKey(formField.getFieldTypeId()) < 0) continue;

            if (formField.getFieldTypeId() == FormField.COUNTRY_OF_RESIDENCE_TYPE_ID) {
                if (form.getLocalPersonId() == Person.ME_ID)
                    changed = changed || preFillHomeCountryField(formField);
                continue;
            }
            typeSet.add(String.valueOf(PRE_FILLED_FIELDS.get(formField.getFieldTypeId())));
            fieldsToFill.add(formField);
        }

        Cursor cursor = getDocFieldsCursor(typeSet);
        if (cursor == null) return changed;

        int fieldTypeIdIndex = Util.findIndex(FieldsHelper.FieldsHistoryContract.ALL_COLUMNS_PROJECTION, FieldsHelper.FieldsHistoryContract.FIELD_TYPE_ID);
        int fieldValueIndex = Util.findIndex(FieldsHelper.FieldsHistoryContract.ALL_COLUMNS_PROJECTION, FieldsHelper.FieldsHistoryContract.FIELD_VALUE);
        while (cursor.moveToNext()) {
            int fieldTypeId = cursor.getInt(fieldTypeIdIndex);
            String fieldValue = cursor.getString(fieldValueIndex);

            Iterator<FormField> iterator = fieldsToFill.iterator();
            int month = 0;
            int day = 0;
            int year = 0;
            Calendar calendar = Calendar.getInstance();
            if (fieldTypeId == FieldsType.DATE_OF_BIRTH_TYPE_ID) {
                calendar.setTimeInMillis(Integer.valueOf(fieldValue) * 1000L);
                month = calendar.get(Calendar.MONTH) + 1;
                day = calendar.get(Calendar.DAY_OF_MONTH);
                year = calendar.get(Calendar.YEAR) % 100;
            }
            while (iterator.hasNext()) {
                FormField formField = iterator.next();
                if (PRE_FILLED_FIELDS.get(formField.getFieldTypeId()) == fieldTypeId) {
                    switch (formField.getFieldTypeId()) {
                        case FormField.BIRTH_DATE_MOUNTH1_TYPE_ID:
                            formField.setValue(String.valueOf(month / 10));
                            break;
                        case FormField.BIRTH_DATE_MOUNTH2_TYPE_ID:
                            formField.setValue(String.valueOf(month % 10));
                            break;
                        case FormField.BIRTH_DATE_DAY1_TYPE_ID:
                            formField.setValue(String.valueOf(day / 10));
                            break;
                        case FormField.BIRTH_DATE_DAY2_TYPE_ID:
                            formField.setValue(String.valueOf(day % 10));
                            break;
                        case FormField.BIRTH_DATE_YEAR1_TYPE_ID:
                            formField.setValue(String.valueOf(year / 10));
                            break;
                        case FormField.BIRTH_DATE_YEAR2_TYPE_ID:
                            formField.setValue(String.valueOf(year % 10));
                            break;
                        default:
                            formField.setValue(fieldValue);
                            iterator.remove();
                            break;
                    }
                }
            }
        }

        cursor.close();

        return changed;
    }

    private Cursor getDocFieldsCursor(Set<String> typeSet) {
        List<String> argsList = new ArrayList<>();
        argsList.add(String.valueOf(form.getLocalPersonId()));
        argsList.addAll(typeSet);

        String[] args = argsList.toArray(new String[]{});
        Cursor cursor = context.getContentResolver().query(FieldsHelper.FieldsHistoryContract.CONTENT_URI, null, null, args, null);
        return cursor;
    }

    private boolean preFillHomeCountryField(FormField formField) {
        boolean changed = false;
        User user = SharedPreferenceUtils.getUtils().getUserSettings();
        if (user.getCountryId() == -1) return changed;

        DaoHelper<Country> countryDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Country.class);
        Country country = countryDaoHelper.getItemByRemoteId(context, user.getCountryId());
        if (country.getName() != null) {
            formField.setValue(country.getName());
            changed = true;
        }

        return changed;
    }

    /**
     *
     * @return filled pdf (for pre view) file in cache dir
     */
    public File getViewAblePdfForm() {
        return getPdfForm(false);
    }

    /**
     *
     * create filled pdf (for printing) file in cache dir
     * @param onLoadCompleteListener listener to result
     */
    public void getPrintAblePdfFormAsync(OnLoadCompleteListener onLoadCompleteListener) {
        if (onLoadCompleteListener == null) throw new IllegalArgumentException("OnLoadCompleteListener is NULL!!!");

        new LoadPrintPdfTask(onLoadCompleteListener).execute();
    }

    private File getPdfForm(boolean forPrint) {
        preFillFields();
        String pdfFile = forPrint ? PDF_FORM_FOR_PRINT : PDF_FORM_FOR_VIEW;
        PDDocument pdfDocument = loadBasePdf(pdfFile);
        fillPdfFields(pdfDocument);

        File file = new File(context.getCacheDir(), pdfFile);
        try {
            pdfDocument.save(file);
            pdfDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private void setField(PDAcroForm acroForm, String name, String value) throws IOException {
        if (value == null) return;
        PDField field = acroForm.getField(name);
        if (field instanceof PDTextField) {
            ((PDTextField)field).setValue(value);
        }
        else
        {
            System.err.println("No field found with name:" + name);
        }
    }

    private void fillPdfFields(PDDocument pdf) {
        try {
            PDDocumentCatalog docCatalog = pdf.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();
            for (FormField formField: form.getData()) {
                setField(acroForm, formField.getFieldTypeId() + "", formField.getValue());
            }
        }
        catch (Exception e){
            DeveloperUtil.michaelLog(e);
            e.printStackTrace();
        }
    }

    private PDDocument loadBasePdf(String pdfFile) {
        PDDocument pdf = null;
        try {
            pdf = PDDocument.load(context.getAssets().open(pdfFile));
        } catch (Exception e){
            DeveloperUtil.michaelLog(e);
            e.printStackTrace();
        }

        return pdf;
    }

    public interface OnLoadCompleteListener {
        void loadCompleted(File formFile);
    }

    private class LoadPrintPdfTask extends AsyncTask<Void, Void, File> {

        private OnLoadCompleteListener onLoadCompleteListener;
        private File printPdfFile;

        public LoadPrintPdfTask(OnLoadCompleteListener onLoadCompleteListener) {
            this.onLoadCompleteListener = onLoadCompleteListener;
        }

        @Override
        protected File doInBackground(Void... params) {
            File result = null;
            printPdfFile = getPdfForm(true);

            Document pdfDocument = new Document();
            int ret = pdfDocument.Open(printPdfFile.getAbsolutePath(), "");
            if (ret != 0) {
                pdfDocument.Close();
                return result;
            }

            Page page = pdfDocument.GetPage(0);
            Matrix mat = null;
            try {
                final int dpi = 72;
                //calculate the display ratio that page full-fill on screen.
                //the result is stored in variant "dpix".
                float dpix = pdfDocument.GetPageWidth(0);
                float dpiy = pdfDocument.GetPageWidth(0);
                final int disp_w = (int) dpix * 3; //3x increase quality
                final int disp_h = (int) dpiy * 3;
                dpix = disp_w * dpi / dpix;
                dpiy = (disp_h - 50) * dpi / dpiy;
                if( dpix > dpiy ) dpix = dpiy;

                if( dpix > dpiy ) dpix = dpiy; //get min dpi to fit page
                int w = (int)(dpix * pdfDocument.GetPageWidth(0) / dpi);
                int h = (int)(dpix * pdfDocument.GetPageHeight(0) / dpi);
                mat = new com.radaee.pdf.Matrix(dpix/dpi, -dpix/dpi, 0, dpix * pdfDocument.GetPageHeight(0)/dpi);

                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(Color.WHITE);
                page.RenderToBmp(bitmap, mat);

                File imageFile = new File(context.getCacheDir(), "form.png");
                FileOutputStream fout = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, fout);

                fout.close();
                result = imageFile;
                bitmap.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mat != null) mat.Destroy();
                page.Close();
                pdfDocument.Close();
            }


            return result;
        }

        @Override
        protected void onPostExecute(File imageFile) {
            if (imageFile != null) onLoadCompleteListener.loadCompleted(imageFile);
            else onLoadCompleteListener.loadCompleted(printPdfFile);
        }
    }
}
