package com.zoomlee.zoo.incitations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.AdapterView;

import com.zoomlee.zoo.ui.adapters.IncitationsAdapter;
import com.zoomlee.zoo.utils.BillingUtils;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Author vbevans94.
 */
public class IncitationsController {

    private final Random random = new Random();

    /**
     * Gets incitation for screen.
     *
     * @param screen on which we want to show incitation
     * @return incitation or null if nothing is available
     */
    public Incitation getIncitation(Screen screen) {
        switch (screen) {

            case DOCUMENTS:
            case NOTIFICATIONS:
                List<Incitation> incitations = new ArrayList<>();
                int invitesCount = SharedPreferenceUtils.getUtils().getUserSettings().getInvitesCount();
                if (invitesCount < 10) {
                    // less than 10 invites for user
                    incitations.add(Incitation.INVITE_YOUR_FRIENDS);
                }
                if (!BillingUtils.isPro(SharedPreferenceUtils.getUtils().getUserSettings())) {
                    // have no subscription
                    incitations.add(Incitation.SUBSCRIBE_TO_ZOOMLEE);
                }
                if (!incitations.isEmpty()) {
                    return incitations.get(random.nextInt(incitations.size()));
                }

                break;

            case TAX_TRACKING:
                int currentCountryId = SharedPreferenceUtils.getUtils().getUserSettings().getCountryId();
                if (currentCountryId == -1) {
                    return Incitation.SELECT_HOME_COUNTRY;
                }
                break;
        }

        return null;
    }

    /**
     * Processes incitation click.
     *
     * @param activity   to use
     * @param incitation to go after
     */
    public static void processIncitation(Activity activity, Incitation incitation) {
        Intent target = new Intent(activity, incitation.activityClass);
        target.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        activity.startActivityForResult(target, RequestCodes.INCITATION_ACTIVITY);
    }

    public IncitationsAdapter.Incitated createIncitated(int itemsCount, IncitationsController.Screen screen,
                                                        int minPosition, int maxPosition) {
        return new IncitatedImpl(itemsCount, screen, minPosition, maxPosition);
    }

    public enum Screen {

        DOCUMENTS, NOTIFICATIONS, TAX_TRACKING
    }

    private class IncitatedImpl implements IncitationsAdapter.Incitated {

        private final int incitationPosition;
        private final Incitation incitation;

        IncitatedImpl(int itemsCount, IncitationsController.Screen screen, int minPosition, int maxPosition) {
            incitation = IncitationsController.this.getIncitation(screen);

            if (itemsCount <= minPosition || incitation == null) {
                incitationPosition = AdapterView.INVALID_POSITION;
            } else {
                incitationPosition = Util.randInt(minPosition, Math.min(maxPosition, itemsCount));
            }
        }

        @Override
        public Context getContext() {
            throw new UnsupportedOperationException("This implementation doesn't provide context");
        }

        @Override
        public int getIncitationPosition() {
            return incitationPosition;
        }

        @Override
        public Incitation getIncitation() {
            return incitation;
        }
    }
}
