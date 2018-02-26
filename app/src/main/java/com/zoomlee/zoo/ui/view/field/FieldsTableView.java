package com.zoomlee.zoo.ui.view.field;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Field;
import com.zoomlee.zoo.net.model.FieldsType;

import java.util.List;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 23.03.15.
 */
public class FieldsTableView extends TableLayout {

    private final static TableRow.LayoutParams LEFT_CELL_PARAMS = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
    private final static TableRow.LayoutParams RIGHT_CELL_PARAMS = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);

    private final static TableRow.LayoutParams FULL_CELL_PARAMS = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

    /**
     * Indicates whether field values are trimmed or full sized.
     */
    private boolean trim;
    /**
     * Number of visible fields.
     */
    private int count;
    /**
     * Indicates whether we need to use cache or no.
     */
    private boolean useCache;

    private boolean isWorkPermit;

    public FieldsTableView(Context context) {
        this(context, null);
    }

    public FieldsTableView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int bottomMargin = getResources().getDimensionPixelSize(R.dimen.table_row_bottom_margin);
        LEFT_CELL_PARAMS.bottomMargin = bottomMargin;
        RIGHT_CELL_PARAMS.bottomMargin = bottomMargin;
        int cellMargin = getResources().getDimensionPixelSize(R.dimen.table_cell_margin);
        LEFT_CELL_PARAMS.rightMargin = cellMargin;
        RIGHT_CELL_PARAMS.leftMargin = cellMargin;

        FULL_CELL_PARAMS.bottomMargin = bottomMargin;
    }

    /**
     * Fills fields table view with fields.
     *
     * @param fields         to get data from
     * @param maxFieldsCount to bound maximum number of fields displayed, if <=0: all fields displayed
     * @param trim           field values or keep them full size
     */
    public void setFields(List<Field> fields, int maxFieldsCount, boolean trim, boolean isWorkPermit) {
        this.useCache = trim && this.trim;
        this.trim = trim;
        this.count = maxFieldsCount <= 0 ? fields.size() : Math.min(maxFieldsCount, fields.size());
        this.isWorkPermit = isWorkPermit;

        fillViews(fields);
    }

    private void fillViews(List<Field> fields) {
        // make clean up in previous view hierarchy to start again
        cleanUp();

        FieldView fieldView = null;
        TableRow row;
        int i = 0;
        boolean reuse = false; // for using fields fields created in previous iterations
        boolean needHightlight = false;
        if (isWorkPermit) {
            Field fieldCountry = new Field();
            fieldCountry.setFieldTypeId(70);
            int countryFieldIndex = fields.indexOf(fieldCountry);
            if (countryFieldIndex > -1) {
                String countryName = fields.get(countryFieldIndex).getValue();

                needHightlight = "US".equalsIgnoreCase(countryName)
                        || "USA".equalsIgnoreCase(countryName)
                        || "united states".equalsIgnoreCase(countryName)
                        || "united states of america".equalsIgnoreCase(countryName);
            }
        }
        while (i < count || reuse) { // we may have one view not processed
            // rule: process one row per iteration
            row = fetchRow(i);
            if (trim) {
                // left field view
                fieldView = fetchFieldView(row, fields.get(i++), 0, needHightlight);
                addToRow(row, fieldView, LEFT_CELL_PARAMS);
                if (i == count) {
                    // set visibility of last cached FiledView GONE
                    if (useCache && row.getChildCount() > 1) {
                        row.getChildAt(1).setVisibility(GONE);
                    }
                    break;
                }
                // right field view
                fieldView = fetchFieldView(row, fields.get(i++), 1, needHightlight);
                addToRow(row, fieldView, RIGHT_CELL_PARAMS);
            } else {
                // try one field
                if (!reuse) {
                    // if not reuse then create
                    fieldView = createFieldView(fields.get(i++), needHightlight);
                    fieldView.measure(0, 0);
                }
                reuse = false;

                if (notFitHalf(fieldView)) {
                    // full size field
                    row.addView(fieldView, FULL_CELL_PARAMS);
                } else {
                    // maybe two fields
                    if (i == count) {
                        // the last one, and the only one
                        row.addView(fieldView, FULL_CELL_PARAMS);
                        break;
                    }
                    FieldView tempFieldView = fieldView;
                    // try to fit second view that might be reused if not fit
                    fieldView = createFieldView(fields.get(i++), needHightlight);
                    fieldView.measure(0, 0);
                    if (notFitHalf(fieldView)) {
                        // will leave it for next iteration, now put only what we have already
                        row.addView(tempFieldView, FULL_CELL_PARAMS);
                        reuse = true;
                    } else {
                        row.addView(tempFieldView, LEFT_CELL_PARAMS);
                        row.addView(fieldView, RIGHT_CELL_PARAMS);
                    }
                }
            }
        }
    }

    private void cleanUp() {
        if (getChildCount() <= 0) {
            return;
        }
        if (useCache) {
            // in case of trim we want to reuse views "if exist", so we remove only unneeded views
            int rowCount = count / 2 + count % 2;
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++)
                getChildAt(i).setVisibility(i < rowCount ? VISIBLE : GONE);

        } else {
            // when no trim, we can't predict how views where placed, so we clean everything
            removeAllViews();
        }
    }

    /**
     * Checks whether field view fits half screen being not trimmed.
     *
     * @param fieldView to check
     * @return true if fits
     */
    private boolean notFitHalf(FieldView fieldView) {
        return fieldView.getMeasuredWidth() + LEFT_CELL_PARAMS.rightMargin > getWidth() / 2;
    }

    /**
     * Fetches row for placing field at index.
     *
     * @param index of the field
     * @return created or cached row instance
     */
    private TableRow fetchRow(int index) {
        if (useCache) {
            int rowIndex = index / 2 + index % 2;
            if (rowIndex < getChildCount()) {
                return (TableRow) getChildAt(rowIndex);
            }
        }
        TableRow row = new TableRow(getContext());
        addView(row);
        return row;
    }

    /**
     * Retrieves field view from cache if using or create new one otherwise.
     *
     * @param row   to try to get view from
     * @param field to set
     * @param index in the row
     * @return field view
     */
    private FieldView fetchFieldView(TableRow row, Field field, int index, boolean needToHighlight) {
        if (useCache && row.getChildCount() > index) {
            FieldView fieldView = (FieldView) row.getChildAt(index);
            fieldView.setVisibility(VISIBLE);
            fieldView.setField(field);
            return fieldView;
        }
        return createFieldView(field, needToHighlight);
    }

    private FieldView createFieldView(Field field, boolean needToHighlight) {
        FieldView fieldView = new FieldView(getContext());
        fieldView.setSingleLine(trim);
        fieldView.setField(field);
        if ((field.getFieldTypeId() == FieldsType.CASE_NUMBER_TYPE_ID || field.getFieldTypeId() == FieldsType.RECEIPT_NUMBER_TYPE_ID)
                && needToHighlight)
            fieldView.needToHighlight();
        return fieldView;
    }

    /**
     * Adds to row if it hasn't this field view yet.
     *
     * @param row       to add to
     * @param fieldView to be added
     * @param params    to add with
     */
    private void addToRow(TableRow row, FieldView fieldView, TableRow.LayoutParams params) {
        if (fieldView.getParent() == null) {
            row.addView(fieldView, params);
        }
    }
}
