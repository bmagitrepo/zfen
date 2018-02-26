package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.utils.DeveloperUtil;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Author vbevans94.
 */
public class AnyProgressView extends FrameLayout {

    private final static LayoutParams PARAMS = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private static final int DEF_TIME_DELTA = 200;
    private static final int DEF_X_DELTA = 4;

    private Drawable progressFrame;
    private Handler uiHandler = new UiHandler();
    private int frameWidth;
    private int currentX;
    private boolean running;
    private int xDelta;
    private long timeDelta;
    private final Queue<ImageView> frames = new ArrayDeque<>();

    public AnyProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AnyProgressView, 0, 0);
        progressFrame = array.getDrawable(R.styleable.AnyProgressView_progressFrame);
        xDelta = array.getInteger(R.styleable.AnyProgressView_xDelta, DEF_X_DELTA);
        timeDelta = array.getInteger(R.styleable.AnyProgressView_timeDelta, DEF_TIME_DELTA);
        array.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        refreshViews(w);

        start();
    }

    private void refreshViews(int w) {
        DeveloperUtil.michaelLog("refreshViews");

        frames.clear();

        frameWidth = progressFrame.getIntrinsicWidth();
        int nViews = w / frameWidth + (w % frameWidth == 0 ? 3 : 4); // plus some at each end

        for (int i = 0; i < nViews; i++) {
            ImageView frame;
            if (i >= getChildCount()) {
                frame = new ImageView(getContext());
                frame.setScaleType(ImageView.ScaleType.CENTER);
                frame.setAdjustViewBounds(true);
                addView(frame, PARAMS);
            } else {
                frame = (ImageView) getChildAt(i);
            }
            frame.setImageDrawable(progressFrame);

            frames.add(frame); // with them all the work will be done to avoid changing layout
        }

        currentX = -2 * frameWidth;

        measure(0, 0);
    }

    /**
     * Starts and/or shows up progress.
     */
    public void start() {
        if (!running || getVisibility() == GONE) {
            running = true;
            uiHandler.sendEmptyMessageDelayed(0, timeDelta);
        }

        setVisibility(VISIBLE);
    }

    /**
     * Stops and hides progress.
     */
    public void stop() {
        running = false;

        uiHandler.removeCallbacksAndMessages(null);
        uiHandler.removeCallbacks(null);

        setVisibility(GONE);
    }

    /**
     * Changes animated progress drawable.
     *
     * @param progressFrame to set
     */
    public void setProgressFrame(Drawable progressFrame) {
        stop();

        this.progressFrame = progressFrame;

        refreshViews(getWidth());
    }

    private class UiHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (running && !frames.isEmpty()) {
                currentX += xDelta;
                int x = currentX;
                // shift everybody a bit
                for (ImageView frame : frames) {
                    frame.setX(x);
                    x += frameWidth;
                }

                if (currentX >= -frameWidth) {
                    // take frame from the end and place at the beginning
                    currentX -= frameWidth;

                    frames.add(frames.poll()); // last becomes first
                }

                sendEmptyMessageDelayed(0, timeDelta);
            }
        }
    }
}
