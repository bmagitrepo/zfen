package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.net.model.Document;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * View to represent document icon coloured with document color with type icon inside.
 */
public class DocumentIconView extends LinearLayout {

    @BindView(R.id.image_icon)
    ImageView imageIcon;

    @BindView(R.id.line_indicator)
    View lineIndicator;

    private final float imageMargin;

    public DocumentIconView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.include_document_icon, this);

        setOrientation(HORIZONTAL);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DocumentIconView, 0, 0);
        this.imageMargin = array.getDimension(R.styleable.DocumentIconView_imagePadding, 0);
        array.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (isInEditMode()) {
            return;
        }

        ButterKnife.bind(this);

        if (imageMargin > 0) {
            ((LayoutParams) imageIcon.getLayoutParams()).topMargin = (int) imageMargin;
        }
    }

    public void setDocument(Document document) {
        int color = Color.parseColor("#" + document.getColorHEX());
        imageIcon.setImageResource(Category.getIconRes(document.getCategoryId()));
        imageIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        Drawable drawable = imageIcon.getBackground().mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            imageIcon.setBackground(drawable);
        else
            imageIcon.setBackgroundDrawable(drawable);
        lineIndicator.setBackgroundColor(color);
    }
}
