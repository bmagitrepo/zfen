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
public class PasscodeView extends EditText {

    private final static int PASSCODE_LENGTH = 4;

    private Paint mCirclePaint;
    private Paint mTextPaint;
    private Paint mLinePaint;
    private int circleRadius;

    private OnPasscodeEnteredListener listener;
    private float strokeWidth;
    private int xStart;
    private int yStart;
    private int deltaX;
    private int textY;

    public PasscodeView(Context context) {
        this(context, null);
    }

    public PasscodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasscodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DeveloperUtil.michaelLog();

        getTextColors().getDefaultColor();
        init();
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
    }

    private void init() {
        Resources resources = getResources();

        xStart = resources.getDimensionPixelSize(R.dimen.passcode_x_start);
        yStart = resources.getDimensionPixelSize(R.dimen.passcode_y_start);
        strokeWidth = resources.getDimension(R.dimen.divider_height);
        circleRadius = (int) resources.getDimension(R.dimen.passcode_circle_radius);
        textY = resources.getDimensionPixelSize(R.dimen.passcode_text_y);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(resources.getColor(R.color.pin_item_empty));
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(strokeWidth);
        // mCirclePaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(getTextColors().getDefaultColor());
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(resources.getDimension(R.dimen.passcode_text_size));

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(resources.getColor(R.color.green_zoomlee));
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(strokeWidth);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (listener != null && s.length() == PASSCODE_LENGTH)
            listener.onPasscodeEntered(s.toString());
    }

    public void setOnPasscodeEnteredListener(OnPasscodeEnteredListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        deltaX = (w - 2 * xStart) / (PASSCODE_LENGTH - 1);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        //super.onDraw(canvas);

        int height = getHeight();
        int width = getWidth();

        int length = length();
        int x = xStart;
        int y = height - textY;

        // draw text
        for (int i = 0; i < length; i++) {
            String letter = getText().subSequence(i, i + 1).toString();
            canvas.drawText(letter, x, y, mTextPaint);
            x += deltaX;
        }

        // draw empty circles
        y = height - yStart;
        for (int i = length; i < PASSCODE_LENGTH; i++) {
            canvas.drawCircle(x, y, circleRadius, mCirclePaint);
            x += deltaX;
        }

        // draw line below
        int lineY = (int) (height - strokeWidth);
        canvas.drawLine(0, lineY, width, lineY, mLinePaint);
    }

    public interface OnPasscodeEnteredListener {

        void onPasscodeEntered(String passcode);
    }
}
