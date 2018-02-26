package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.ViewFlipper;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.File;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 19.02.15.
 */
public class ImageFlipper extends LinearLayout implements CompoundButton.OnCheckedChangeListener {

    private static final int MAX_DOTS_COUNT = 15;
    private static final int SWIPE_MIN_DISTANCE = 40;
    private static final int SWIPE_MAX_OFF_PATH = 250;

    private List<RadioButton> radioButtonsList;
    private LinearLayout radioBtnLayout;

    private ViewFlipper mViewFlipper;
    private AttachmentView supportView;
    private AttachmentView visibleView;
    private Animation.AnimationListener mAnimationListener;
    private final GestureDetector detector = new GestureDetector(new SwipeGestureDetector());

    private boolean selfCheckedChanged;
    private int prevCheckedItem;
    private int minCapacity;
    private int maxCapacity;
    private int firstSmallDotIndex;
    private int startOfFirstSmallDot;
    private volatile boolean animationFinished = true;
    private int visibleFileIndex;
    private OnClickListener listener;

    private List<File> files;

    public ImageFlipper(Context context) {
        this(context, null);
    }

    public ImageFlipper(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageFlipper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.image_flipper, this);

        mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        radioBtnLayout = (LinearLayout) findViewById(R.id.switchButtonsLayout);
        supportView = (AttachmentView) findViewById(R.id.supportView);
        visibleView = (AttachmentView) findViewById(R.id.visibleView);
    }


    public File getCurrentFile(){
        return files.get(visibleFileIndex);
    }


    @Override
    public void setOnClickListener(OnClickListener l) {
        listener = l;
    }

    public void init(List<File> files, final ScrollView scrollView, int selectedImage) {
        this.files = files;

        mViewFlipper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                if (detector.onTouchEvent(event))
                    scrollView.requestDisallowInterceptTouchEvent(true);

                return true;
            }
        });

        mAnimationListener = new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                animationFinished = true;
            }
        };

        prevCheckedItem = 0;
        animationFinished = true;
        selfCheckedChanged = false;
        visibleFileIndex = selectedImage;

        initRadioButtons();
        initViewImages();
    }

    public boolean initialized() {
        return this.files != null;
    }

    private void initRadioButtons() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        int dotsCount = Math.min(MAX_DOTS_COUNT, files.size());
        radioButtonsList = new ArrayList<>(dotsCount);
        radioBtnLayout.removeAllViews();
        int radioBtnSize = getResources().getDimensionPixelSize(R.dimen.radio_btn_full_size);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(radioBtnSize, radioBtnSize);
        LinearLayout.LayoutParams marginLp = new LinearLayout.LayoutParams(radioBtnSize, radioBtnSize);
        marginLp.leftMargin = getResources().getDimensionPixelSize(R.dimen.radio_btn_left_margin);

        for (int i = 0; i < dotsCount; i++) {
            RadioButton radioButton = (RadioButton) layoutInflater.inflate(R.layout.image_switcher_rb, null);
            if (i != 0) {
                radioButton.setLayoutParams(marginLp);
            } else {
                radioButton.setLayoutParams(lp);
                radioButton.setChecked(true);
            }

            radioButton.setOnCheckedChangeListener(this);
            radioButtonsList.add(radioButton);
            radioBtnLayout.addView(radioButton);
        }

        //calculate dots capacity
        minCapacity = files.size() / dotsCount;
        maxCapacity = minCapacity + 1;
        firstSmallDotIndex = files.size() % dotsCount;
        startOfFirstSmallDot = maxCapacity * firstSmallDotIndex;
    }

    private void initViewImages() {
        mViewFlipper.setInAnimation(null);
        mViewFlipper.setOutAnimation(null);
        mViewFlipper.setDisplayedChild(0);
        if (visibleView.getId() != R.id.visibleView) {
            AttachmentView helpIV = visibleView;
            visibleView = supportView;
            supportView = helpIV;
        }

        loadNextImageToView();
        if (prevCheckedItem != getDotIndexByImage(visibleFileIndex)) {
            selfCheckedChanged = true;
            radioButtonsList.get(getDotIndexByImage(visibleFileIndex)).setChecked(true);
        }
    }

    private void loadNextImageToView() {
        AttachmentView helpIV = visibleView;
        visibleView = supportView;
        supportView = helpIV;

        File file = files.get(visibleFileIndex);
        visibleView.setAttachmentFile(file);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isChecked) return;

        int index = radioButtonsList.indexOf(buttonView);
        if (index == prevCheckedItem) return;

        radioButtonsList.get(prevCheckedItem).setChecked(false);
        prevCheckedItem = index;

        if (!selfCheckedChanged) {
            boolean nextAnimation = getImageIndexByDot(index) > visibleFileIndex;
            visibleFileIndex = getImageIndexByDot(index);
            if (nextAnimation)
                applyNextAnimation();
            else
                applyPrevAnimation();
        } else {
            selfCheckedChanged = false;
        }
    }

    private int getDotIndexByImage(int imageIndex) {
        if (imageIndex < startOfFirstSmallDot)
            return imageIndex / maxCapacity;
        else
            return firstSmallDotIndex + (imageIndex - startOfFirstSmallDot) / minCapacity;
    }

    private int getImageIndexByDot(int dotIndex) {
        if (dotIndex == 0)
            return 0;
        else
            return minCapacity * dotIndex + Math.min(firstSmallDotIndex, dotIndex);
    }

    private void proceedLeftSwipe() {
        if (!animationFinished) return;

        if (visibleFileIndex < files.size() - 1) {
            animationFinished = false;
            visibleFileIndex++;
            applyNextAnimation();
            if (prevCheckedItem != getDotIndexByImage(visibleFileIndex)) {
                selfCheckedChanged = true;
                radioButtonsList.get(getDotIndexByImage(visibleFileIndex)).setChecked(true);
            }
        }
    }

    private void proceedRightSwipe() {
        if (!animationFinished) return;

        if (visibleFileIndex > 0) {
            animationFinished = false;
            visibleFileIndex--;
            applyPrevAnimation();
            if (prevCheckedItem != getDotIndexByImage(visibleFileIndex)) {
                selfCheckedChanged = true;
                radioButtonsList.get(getDotIndexByImage(visibleFileIndex)).setChecked(true);
            }
        }
    }

    private void applyNextAnimation() {
        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.left_in));
        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.left_out));
        mViewFlipper.getInAnimation().setAnimationListener(mAnimationListener);
        loadNextImageToView();
        mViewFlipper.showNext();
    }

    private void applyPrevAnimation() {
        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.right_in));
        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.right_out));
        mViewFlipper.getInAnimation().setAnimationListener(mAnimationListener);
        loadNextImageToView();
        mViewFlipper.showNext();
    }

    class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (listener != null)
                listener.onClick(ImageFlipper.this);
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
                    proceedLeftSwipe();
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
                    proceedRightSwipe();
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }
    }
}
