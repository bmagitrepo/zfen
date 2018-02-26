package com.zoomlee.zoo.invites;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.InviteDataApi;
import com.zoomlee.zoo.net.model.Invite;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

import static android.provider.ContactsContract.DisplayPhoto;
import static com.zoomlee.zoo.net.api.InviteDataApi.ResponseCodes;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @since 22.04.15.
 */
public class InvitesController {

    private final String isoCode;
    private Context context;
    private InviteDataApi api;
    private Map<String, Contact.Status> invitedPhones;
    private Map<String, Contact.Status> invitedEmails;
    private List<Contact> contacts;
    private ContactsLoadListener contactsLoadListener;

    public InvitesController(Context context, ContactsLoadListener contactsLoadListener) {
        this.context = context;
        this.api = buildDocumentDataApi();
        this.contactsLoadListener = contactsLoadListener;
        this.isoCode = Util.getCountryIsoCode(context);
    }

    private InviteDataApi buildDocumentDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(InviteDataApi.class);
    }

    private void getInvites() {
        invitedPhones = new HashMap<>();
        invitedEmails = new HashMap<>();
        try {
            CommonResponse<List<Invite>> commonResponse = api.getInvites(SharedPreferenceUtils.getUtils().getPrivateKey());
            if (commonResponse.getError().getCode() == 200) {
                for (Invite invite : commonResponse.getBody()) {
                    switch (invite.getType()) {
                        case Invite.TYPE_INVITED_BY_EMAIL:
                            invitedEmails.put(invite.getToEmailPhone(), Contact.Status.INVITED);
                            break;
                        case Invite.TYPE_INVITED_BY_EMAIL_EXIST:
                            invitedEmails.put(invite.getToEmailPhone(), Contact.Status.JOINED);
                            break;
                        case Invite.TYPE_INVITED_BY_PHONE:
                            invitedPhones.put(formatPhoneNumber(invite.getToEmailPhone()), Contact.Status.INVITED);
                            break;
                        case Invite.TYPE_INVITED_BY_PHONE_EXIST:
                            invitedPhones.put(formatPhoneNumber(invite.getToEmailPhone()), Contact.Status.JOINED);
                            break;
                    }
                }
            }
        } catch (RetrofitError error) {
            error.printStackTrace();
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^\\d]", "");
    }

    /**
     * Sends invite to email or phone.
     *
     * @param phone to send invite
     * @param email to send invite
     * @return {@linkplain com.zoomlee.zoo.invites.Contact.Status}
     */
    public Contact.Status sendInvite(String phone, String email) {
        if (phone != null) {
            phone = phone.replaceAll("[^[0-9][a-z][A-Z]]", "");
        }

        Contact.Status status = Contact.Status.ERROR;
        try {
            CommonResponse<Object> commonResponse = api.postInvite(SharedPreferenceUtils.getUtils().getPrivateKey(),
                    phone, email);
            switch (commonResponse.getError().getCode()) {
                case ResponseCodes.PostInvite.ALL_GOOD:
                case ResponseCodes.PostInvite.ALLREADY_SEND_TO_THIS_EMAIL:
                case ResponseCodes.PostInvite.ALLREADY_SEND_TO_THIS_PHONE:
                    status = Contact.Status.INVITED;
                    break;
                case ResponseCodes.PostInvite.USER_WITH_THIS_EMAIL_EXIST:
                case ResponseCodes.PostInvite.USER_WITH_THIS_PHONE_EXIST:
                    status = Contact.Status.JOINED;
                    break;
            }
        } catch (RetrofitError error) {
            error.printStackTrace();
        }
        return status;
    }

    /**
     * Gets contacts from device and updates they invited status.<br/>
     * (status will not be updated in case of connection errors)
     *
     * @return list of contacts
     */
    public List<Contact> getContacts() {
        getInvites();
        return getContactsFromContentProvider();
    }

    private List<Contact> getContactsFromContentProvider() {
        contacts = new ArrayList<>();
        Cursor contactsCursor = getContactsCursor();
        Cursor rawIdCursor = getRawIdsCursor();

        try {
            if (contactsCursor != null && contactsCursor.moveToFirst()) {
                int rawIdColumn = contactsCursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID);
                int nameColumn = contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
                //depends on mimeType contains DISPLAY_NAME, phone number, email
                int data1Column = contactsCursor.getColumnIndex(ContactsContract.Data.DATA1);
                int mimeTypeColumn = contactsCursor.getColumnIndex(ContactsContract.Data.MIMETYPE);

                Set<Integer> rawIdsSet = new HashSet<>();
                while(rawIdCursor.moveToNext()) {
                    rawIdsSet.add(rawIdCursor.getInt(0));
                }
                rawIdCursor.close();

                Contact contact = null;
                contactsCursor.moveToFirst();
                while (!contactsCursor.isAfterLast()) {
                    int rawId = contactsCursor.getInt(rawIdColumn);
                    if (contact == null || rawId != contact.getId()) {
                        if (!rawIdsSet.contains(rawId)) {
                            contactsCursor.moveToNext();
                            continue;
                        }
                        addContact(contact);
                        contact = new Contact();
                        contact.setId(rawId);
                    }

                    String mimeType = contactsCursor.getString(mimeTypeColumn);
                    contact.setName(contactsCursor.getString(nameColumn));
                    switch (mimeType) {
                        case ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE:
                            // int photoColumn = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO);
                            int photoFileIdColumn = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID);
                            long photoId = contactsCursor.getLong(photoFileIdColumn);
                            // contact.setPhoto(contactsCursor.getBlob(photoColumn));
                            if (photoId != 0)
                                contact.setDisplayPhotoUri(ContentUris.withAppendedId(DisplayPhoto.CONTENT_URI, photoId));
                            break;
                        case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                            String phoneNumber = contactsCursor.getString(data1Column);
                            contact.addChannel(Contact.ChannelType.PHONE, phoneNumber);
                            if (contact.getStatus() != Contact.Status.JOINED) {
                                String formattedPhone = formatPhoneNumber(phoneNumber);
                                Contact.Status status = invitedPhones.get(formattedPhone);
                                if (status != null) {
                                    contact.setStatus(status);
                                }
                            }
                            break;
                        case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                            String email = contactsCursor.getString(data1Column);
                            contact.addChannel(Contact.ChannelType.EMAIL, email);
                            if (contact.getStatus() != Contact.Status.JOINED) {
                                Contact.Status status = invitedEmails.get(email);
                                if (status != null) {
                                    contact.setStatus(status);
                                }
                            }
                            break;
                    }
                    contactsCursor.moveToNext();
                }

                // add last formed item
                addContact(contact);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (contactsCursor != null)
                contactsCursor.close();
        }

        return contacts;
    }

    private Cursor getRawIdsCursor() {
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[] {ContactsContract.Data.NAME_RAW_CONTACT_ID};

        return cr.query(ContactsContract.Data.CONTENT_URI,
                projection, null, null, ContactsContract.Data.NAME_RAW_CONTACT_ID + " ASC ");
    }

    private void addContact(Contact contact) {
        if (contact != null && !contact.getChannels().isEmpty()) {
            contacts.add(contact);

            if (contactsLoadListener != null)
                contactsLoadListener.onContactLoaded(contact);
        }
    }

    private Cursor getContactsCursor() {
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[]{
                ContactsContract.Data.RAW_CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Data.DATA1,
                ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID,
                // ContactsContract.CommonDataKinds.Photo.PHOTO,
                ContactsContract.Data.MIMETYPE};
        String selection = ContactsContract.Data.MIMETYPE + " = ? or "
                + ContactsContract.Data.MIMETYPE + " = ? or "
                + ContactsContract.Data.MIMETYPE + " = ?";
        String[] selectionArgs = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE};

        return cr.query(ContactsContract.Data.CONTENT_URI,
                projection, selection, selectionArgs,
                ContactsContract.Data.RAW_CONTACT_ID + " ASC");
    }

    public interface ContactsLoadListener {
        void onContactLoaded(Contact contact);
    }
}
