package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.EditText;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.utils.DeveloperUtil;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 1/13/15
 */
public class CreatePinView extends EditText {

    private static final int PIN_LENGTH = 4;
    private static final float SPACE_FRACTION = 1.f * 59 / 186; // proportion between line and space size

    private int strokeWidth;
    private int lineLength;
    private int spaceLength;
    private int radius;
    private Paint circlePaint;
    private Paint linePaint;
    private Paint nextLinePaint;
    private OnPinEnteredListener listener;

    public CreatePinView(Context context) {
        this(context, null);
    }

    public CreatePinView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CreatePinView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DeveloperUtil.michaelLog();
        init();
    }

    private void init() {
        // init for input
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        // init for drawing
        Resources resources = getResources();
        int greenColor = resources.getColor(R.color.green_zoomlee);
        int greyColor = resources.getColor(R.color.text_hint);
        radius = (int) resources.getDimension(R.dimen.set_pin_dot_radius);
        strokeWidth = (int) resources.getDimension(R.dimen.divider_height);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(greenColor);
        // circlePaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(greyColor);
        linePaint.setStrokeWidth(strokeWidth);

        nextLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nextLinePaint.setStyle(Paint.Style.STROKE);
        nextLinePaint.setColor(greenColor);
        nextLinePaint.setStrokeWidth(strokeWidth);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (listener != null && s.length() == PIN_LENGTH) {
            listener.onPinEntered(s.toString());
        }
    }

    public void setOnPinEnteredListener(OnPinEnteredListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        lineLength = (int) (1.f * w / ((SPACE_FRACTION + 1) * PIN_LENGTH - SPACE_FRACTION));
        spaceLength = (int) (SPACE_FRACTION * lineLength);
    }

    public void setPinItemColorBlack() {
        circlePaint.setColor(getResources().getColor(R.color.black));
    }

    public String getPin() {
        return getText().toString();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int textLength = length();
        for (int i = 0; i < PIN_LENGTH; i++) {
            float x = (spaceLength + lineLength) * i;
            float y = getHeight() - strokeWidth;
            canvas.drawLine(x, y, x + lineLength, y, i == textLength ? nextLinePaint : linePaint);
        }

        float y = getHeight() / 2;
        for (int i = 0; i < textLength; i++) {
            canvas.drawCircle((spaceLength + lineLength) * i + lineLength / 2, y, radius, circlePaint);
        }
    }

    public interface OnPinEnteredListener {

        void onPinEntered(String pin);
    }
}
