package com.zoomlee.zoo.ui.fragments.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.view.BottomSheet;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.FileUtil;
import com.zoomlee.zoo.utils.PicassoUtil;
import com.zoomlee.zoo.utils.RequestCodes;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class ImagePickerFragment extends DialogFragment {
    private final static String TAG = "ImagePickerFragment";
    private static final String PARAM_FILE = "image_picker_param_file";
    private static final String PARAM_SQUARED = "image_picker_param_squared";
    public static final String STATE_URI_FROM_PHOTO = "state_uri_from_photo";
    private File file;

    private static final int MAX_PICK_IMAGE_PREVIEW = 3;

    private Uri uriFromPhoto;
    private File croppedPhotoFile;
    private boolean squared;

    private OnImagePickedListener listener;

    private Cursor cursor;
    private View imagesFrame;
    private String[] defaultImageIds = new String[MAX_PICK_IMAGE_PREVIEW];
    private ImageView[] imageViews = new ImageView[MAX_PICK_IMAGE_PREVIEW];
    private String[] imagePathes = new String[MAX_PICK_IMAGE_PREVIEW];
    private View[] maskViews = new View[MAX_PICK_IMAGE_PREVIEW];

    public static ImagePickerFragment newInstance(String fileName) {
        return newInstance(fileName, false);
    }

    public static ImagePickerFragment newInstance(String fileName, boolean squared) {
        ImagePickerFragment fragment = new ImagePickerFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_FILE, fileName);
        args.putBoolean(PARAM_SQUARED, squared);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        listener = (OnImagePickedListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        squared = getArguments().getBoolean(PARAM_SQUARED);
        String fileName = getArguments().getString(PARAM_FILE);
        file = getActivity().getFileStreamPath(fileName);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DeveloperUtil.michaelLog();
        View mView = View.inflate(getActivity(), R.layout.fragment_image_picker, null);

        imagesFrame = mView.findViewById(R.id.imagesFrame);
        maskViews[0] = mView.findViewById(R.id.maskView1);
        maskViews[1] = mView.findViewById(R.id.maskView2);
        maskViews[2] = mView.findViewById(R.id.maskView3);

        imageViews[0] = (ImageView) mView.findViewById(R.id.pictureView1);
        imageViews[1] = (ImageView) mView.findViewById(R.id.pictureView2);
        imageViews[2] = (ImageView) mView.findViewById(R.id.pictureView3);

        mView.findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        if (savedInstanceState != null) {
            uriFromPhoto = savedInstanceState.getParcelable(STATE_URI_FROM_PHOTO);
            DeveloperUtil.michaelLog("restoring state: " + uriFromPhoto);
        }

        mView.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        initImageViews();

        return new BottomSheet(mView);
    }

    private void initImageViews() {
        loadBitmaps();

        boolean imageFrameVisible = false;
        for (int i = 0; i < MAX_PICK_IMAGE_PREVIEW; i++) {
            if (imagePathes[i] != null) imageFrameVisible = true;
            initImageView(i);
        }

        imagesFrame.setVisibility(imageFrameVisible ? View.VISIBLE : View.GONE);
    }

    private void initImageView(final int number) {
        imageViews[number].setImageBitmap(null);
        String imagePath = imagePathes[number];
        if (imagePath != null) {
            // start creating request
            PicassoUtil.getInstance().load(new File(imagePath))
                    .resize(300, 300)
                    .centerCrop()
                    .into(imageViews[number]);
            imageViews[number].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cropImageWithId(defaultImageIds[number]);
                    for (int i = 0; i < MAX_PICK_IMAGE_PREVIEW; i++) {
                        maskViews[i].setVisibility(i == number ? View.VISIBLE : View.INVISIBLE);
                    }
                }
            });
        } else {
            imageViews[number].setOnClickListener(null);
        }
    }

    private void loadBitmaps() {
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        cursor = getActivity().getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        for (int i = 0; i < MAX_PICK_IMAGE_PREVIEW; i++) {
            defaultImageIds[i] = null;
            imagePathes[i] = null;

            if (cursor.moveToNext()) {
                String imageLocation = cursor.getString(1);
                File imageFile = new File(imageLocation);
                if (imageFile.exists()) {   // TODO: is there a better way to do this?
                    imagePathes[i] = imageLocation;
                    defaultImageIds[i] = cursor.getString(0);
                } else {
                    i--;
                }
            }
        }
        DeveloperUtil.michaelLog(imagePathes);
        cursor.close();
    }

    private void takePhoto() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File fileFromPhoto = new File(Environment.getExternalStorageDirectory(), "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        uriFromPhoto = Uri.fromFile(fileFromPhoto);
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, uriFromPhoto);
        takePicture.putExtra("outputX", 300);
        takePicture.putExtra("outputY", 300);
        takePicture.putExtra("scale", true);
        takePicture.putExtra("return-data", false);
        startActivityForResult(takePicture, RequestCodes.PICK_FROM_CAMERA);
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.setType("image/*");
        pickPhoto.putExtra("return-data", true);
        startActivityForResult(pickPhoto, RequestCodes.PICK_FROM_GALLERY);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (uriFromPhoto != null) {
            DeveloperUtil.michaelLog("saving state: " + uriFromPhoto);
            outState.putParcelable(STATE_URI_FROM_PHOTO, uriFromPhoto);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        DeveloperUtil.michaelLog("ImagePickerFragment.onActivityResult");
        switch (requestCode) {
            case RequestCodes.PICK_FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    DeveloperUtil.michaelLog(uriFromPhoto);
                    cropImageWithUri(uriFromPhoto);
                }
                break;
            case RequestCodes.CROP_IMAGE_REQUEST:
                if (uriFromPhoto != null) {
                    File fileFromPhoto = new File(uriFromPhoto.getPath());
                    fileFromPhoto.delete();
                }
                if (resultCode == Activity.RESULT_OK) {
                    DeveloperUtil.michaelLog("croppedPhotoFile: " + croppedPhotoFile);
                    if (croppedPhotoFile != null) {
                        try {
                            FileUtil.copyFileUsingFileChannels(croppedPhotoFile, file);
                        } catch (IOException ioe) {
                            Log.e(TAG, "Problem with copying avatar to destination file");
                            ioe.printStackTrace();
                        }
                    }

                    listener.onImagePicked(file);
                }
                // in any case we have to delete created for crop file
                if (croppedPhotoFile != null) {
                    croppedPhotoFile.delete();
                }
                break;
            case RequestCodes.PICK_FROM_GALLERY:
                if (imageReturnedIntent != null) {
                    cropImageWithUri(imageReturnedIntent.getData());
                }
                break;
        }
    }

    private String id2Accept;

    private void cropImageWithId(String id) {
        if (TextUtils.equals(id, id2Accept))
            cropImageWithUri(Uri.parse("content://media/external/images/media/" + id));
        else
            id2Accept = id;
    }

    private void cropImageWithUri(Uri uri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setType("image/*");

        List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities(cropIntent, 0);
        int size = list.size();

        if (size == 0) {
            Toast.makeText(getActivity(), "can_not_find_image_crop_app"/*R.string.can_not_find_image_crop_app*/, Toast.LENGTH_SHORT).show();
            if (uriFromPhoto != null) {
                File fileFromPhoto = new File(uriFromPhoto.getPath());
                fileFromPhoto.delete();
            }
        } else {
            ResolveInfo res = list.get(0);

            cropIntent.setClassName(res.activityInfo.packageName, res.activityInfo.name);
            cropIntent.setDataAndType(uri, "image/*");
            cropIntent.putExtra("crop", "true");
            if (squared) {
                cropIntent.putExtra("aspectX", 1);
                cropIntent.putExtra("aspectY", 1);
            }
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("return-data", false);
            try {
                croppedPhotoFile = File.createTempFile("crop", ".png.nomedia", Environment.getExternalStorageDirectory());
                cropIntent.putExtra("output", Uri.fromFile(croppedPhotoFile));
                cropIntent.putExtra("outputFormat", "PNG");
            } catch (IOException e) {
                e.printStackTrace();
            }

            startActivityForResult(cropIntent, RequestCodes.CROP_IMAGE_REQUEST);
        }
    }

    public interface OnImagePickedListener {
        void onImagePicked(File image);
    }
}
