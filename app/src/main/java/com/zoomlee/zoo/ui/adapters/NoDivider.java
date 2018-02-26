package com.zoomlee.zoo.ui.adapters;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 5/19/15
 */
public interface NoDivider {

    /**
     * @param position to check on
     * @return true if divider shouldn't be drawn
     */
    boolean noDivider(int position);

    /**
     * @return true if divider should not be drawn after the last element of list view
     */
    boolean noFooterLine();
}
