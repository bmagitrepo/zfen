package com.zoomlee.zoo.ui.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Place;

import java.util.Locale;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 3/4/15
 */
public class PlaceInfoView extends LinearLayout {
    private static final String NO_PHONE = "No phone";
    private TextView phoneValueView;
    private TextView addressValueView;
    private Place place;

    public PlaceInfoView(Context context) {
        this(context, null);
    }

    public PlaceInfoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlaceInfoView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_place_info, this);

        phoneValueView = (TextView) findViewById(R.id.phoneValue);
        addressValueView = (TextView) findViewById(R.id.addressValue);
        addressValueView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (place == null) return;

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse(String.format(Locale.US, "http://maps.google.com/maps?daddr=%f,%f", place.getLatitude(), place.getLongitude())));
                context.startActivity(intent);
            }
        });

        phoneValueView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String curPhone = phoneValueView.getText().toString();
                if (NO_PHONE.equals(curPhone))
                    return;

                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + curPhone));
                context.startActivity(intent);
            }
        });
    }

    public void setPlace(Place place) {
        this.place = place;
        String phone = place.getPhone();
        if (TextUtils.isEmpty(phone))
            phoneValueView.setText(NO_PHONE);
        else
            phoneValueView.setText(phone);


        String address = place.getAddress();
        if (TextUtils.isEmpty(address))
            address = String.format("%f, %f", place.getLatitude(), place.getLongitude());

        addressValueView.setText(address);
    }


}
