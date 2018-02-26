package com.zoomlee.zoo.ui.fragments;

import android.app.Service;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.zoomlee.zoo.R;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 1/23/15
 */
public abstract class FragmentWithBottomDialog extends Fragment {

    public static final String DIALOG_TAG = "DialogFragment";
    protected DialogFragment fragment;

    private boolean closeDialogAtResume = false;
    private boolean isOpened;
    private InputMethodManager imm;

    protected FragmentWithBottomDialog() {
    }

    public void openDialog() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        fragment.show(fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom), DIALOG_TAG);
        isOpened = true;
    }

    public void closeDialog() {
        if (!isResumed()) {
            closeDialogAtResume = true;
            return;
        }
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(DIALOG_TAG);
        if (fragment != null) {
            fragment.dismiss();
        }
        isOpened = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (closeDialogAtResume) {
            closeDialog();
            closeDialogAtResume = false;
        }
    }

    public boolean isDialogOpened() {
        return isOpened;
    }

    protected void hideKeyboardForView(View v) {
        imm = imm == null ? (InputMethodManager)
                getActivity().getSystemService(Service.INPUT_METHOD_SERVICE) : imm;
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    protected void hideKeyboard(){
        hideKeyboardForView(getView());
    }
}
