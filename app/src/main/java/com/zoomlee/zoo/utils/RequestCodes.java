package com.zoomlee.zoo.utils;

import java.lang.reflect.Field;
import java.util.HashSet;

public final class RequestCodes {

    // To add new request code, just add it to the end of the list, and set to last value plus one
    public static final int SELECT_DOCUMENT = 101;
    public static final int SELECT_CATEGORY = 102;
    public static final int CATEGORY_TYPES_ACTIVITY = 103;
    public static final int PLAY_SERVICES_UNAVAILABLE = 104;
    public static final int CROP_IMAGE_REQUEST = 105;
    public static final int PICK_FROM_CAMERA = 106;
    public static final int PICK_FROM_GALLERY = 107;
    public static final int TAGS_SETTINGS_ACTIVITY = 108;
    public static final int INVITE_ACTIVITY = 109;
    public static final int SET_PIN_ACTIVITY = 110;
    public static final int DOCUMENTS_ACTIVITY = 111;
    public static final int CREATE_DOCUMENT = 112;
    public static final int EDIT_DOCUMENT = 113;
    public static final int DOCUMENT_DETAILS_ACTIVITY = 114;
    public static final int SEND_EMAIL = 115;
    public static final int OPEN_ATTACHMENT = 116;
    public static final int CHANGE_EMAIL_PHONE = 117;
    public static final int SELECT_PERSON = 118;
    public static final int MANAGE_PERSONS = 119;
    public static final int EDIT_PERSON = 120;
    public static final int CREATE_PERSONS = 121;
    public static final int CONFIRMATION_ACTIVITY = 122;
    public static final int GET_COUNTRY = 123;
    public static final int PIN_REQUEST_CODE = 124;
    public static final int SUBSCRIPTION_ACTIVITY = 125;
    public static final int CREATE_EDIT_TAX = 126;
    public static final int INCITATION_ACTIVITY = 127;
    public static final int PAY_SUBS = 128;
    public static final int SELECT_DOCUMENTS = 129;
    public static final int IMMIGRATION_FORM_ACTIVITY = 130;
    public static final int PRINT_REQUEST = 131;
    public static final int EDIT_FORM_ACTIVITY = 132;
    public static final int FORM_ARTICLES_ACTIVITY = 133;

    private static HashSet<Integer> requestCodes = new HashSet<>();

    static {
        Field[] fields = RequestCodes.class.getDeclaredFields();
        try {
            for (Field field : fields)
                if (!field.getType().equals(HashSet.class))
                    requestCodes.add(field.getInt(null));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /*public static boolean isZoomleeRequest(int requestCode) {
        // both activity and fragment request codes
        return requestCodes.contains(requestCode) || requestCodes.contains(requestCode & 0xffff);
    }*/
}
