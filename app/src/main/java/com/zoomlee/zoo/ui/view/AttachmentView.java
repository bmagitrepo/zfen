package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.FilesType;
import com.zoomlee.zoo.utils.PicassoUtil;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 2/26/15
 */
public class AttachmentView extends FrameLayout {

    private ImageLoadingView loader;
    private TopImageView imageView;
    private int otherFileWidth;
    private int otherFileHeight;
    private BitmapReduceTransformation transformation;
    private com.zoomlee.zoo.net.model.File attachment;

    public AttachmentView(Context context) {
        this(context, null);
    }

    public AttachmentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AttachmentView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final Resources res = getResources();
        final int viewFlipperHeight = res.getDimensionPixelSize(R.dimen.file_item_height);

        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, viewFlipperHeight));

        loader = new ImageLoadingView(context);

        imageView = new TopImageView(context);

        otherFileWidth = getResources().getDimensionPixelSize(R.dimen.other_file_icon_width);
        otherFileHeight = getResources().getDimensionPixelSize(R.dimen.other_file_icon_height);
        transformation = new BitmapReduceTransformation(viewFlipperHeight);
        addView(loader);
        addView(imageView);
    }

    public void setAttachmentFile(com.zoomlee.zoo.net.model.File attachmentFile) {
        this.attachment = attachmentFile;
        loadImage();
    }

    private void loadImage() {
        if (attachment.getTypeId() != FilesType.IMAGE_TYPE) {
            loader.hide();
            imageView.setImageResource(R.drawable.other_file);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageView.getLayoutParams();
            layoutParams.width = otherFileWidth;
            layoutParams.height = otherFileHeight;
            layoutParams.gravity = Gravity.CENTER;
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            loader.show();

            RequestCreator requestCreator;
            if (attachment.getLocalPath() != null)
                requestCreator = PicassoUtil.getInstance().load(new java.io.File(attachment.getLocalPath()));
            else
                requestCreator = PicassoUtil.getInstance().load(attachment.getRemotePath());
            requestCreator.transform(transformation).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    loader.hide();
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    layoutParams.gravity = Gravity.CENTER;
                    imageView.setLayoutParams(layoutParams);
                    imageView.setScaleType(ImageView.ScaleType.MATRIX);
                }

                @Override
                public void onError() {
                    loader.showError();
                }
            });
        }
    }

    private static class BitmapReduceTransformation implements Transformation {
        private int viewFlipperHeight;

        public BitmapReduceTransformation(int viewFlipperHeight) {
            this.viewFlipperHeight = viewFlipperHeight;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            if (viewFlipperHeight >= source.getHeight()) return source;
            double aspectRatio = (double) source.getWidth() / (double) source.getHeight();
            int targetWidth = (int) (viewFlipperHeight * aspectRatio);
            Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, viewFlipperHeight, false);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "transformation desiredHeight";
        }
    }

    private class TopImageView extends ImageView {

        public TopImageView(Context context) {
            super(context);
        }

        @Override
        protected boolean setFrame(int l, int t, int r, int b) {
            final Matrix matrix = getImageMatrix();
            if (getDrawable() != null) {
                float scale;
                final int viewWidth = r - l - getPaddingLeft() - getPaddingRight();
                final int viewHeight = t - b - getPaddingTop() - getPaddingBottom();
                final int drawableWidth = getDrawable().getIntrinsicWidth();
                final int drawableHeight = getDrawable().getIntrinsicHeight();

                if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
                    scale = (float) viewHeight / (float) drawableHeight;
                } else {
                    scale = (float) viewWidth / (float) drawableWidth;
                }

                matrix.setScale(scale, scale);
                setImageMatrix(matrix);
            }

            return super.setFrame(l, t, r, b);
        }
    }
}
