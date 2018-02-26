package com.zoomlee.zoo.ui.fragments;

import com.zoomlee.zoo.ui.fragments.dialog.ImagePickerFragment;

import java.io.File;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 1/23/15
 */
public abstract class FragmentWithImagePicker extends FragmentWithBottomDialog {

    protected FragmentWithImagePicker(String fileName) {
        fragment = ImagePickerFragment.newInstance(fileName);
    }

    protected FragmentWithImagePicker(String fileName, boolean squared) {
        fragment = ImagePickerFragment.newInstance(fileName, squared);
    }

    public ImagePickerFragment getImagePicker() {
        return (ImagePickerFragment)fragment;
    }

    public abstract void onImageObtained(File image);
}
