package com.zoomlee.zoo.ui.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.invites.Contact;
import com.zoomlee.zoo.ui.view.invite.InviteItemView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author vbevans94.
 */
public class InviteAdapter extends BaseAdapter implements Filterable, NoDivider {

    private final List<ContactItem> contactItems = new ArrayList<>();
    private final List<Contact> contacts = new ArrayList<>();
    private final LayoutInflater inflater;
    private final Filter filter = new ItemFilter();

    public InviteAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void replaceWith(List<Contact> contacts) {
        this.contacts.clear();
        this.contacts.addAll(contacts);

        // filter all
        getFilter().filter("");
    }

    private void setItems(List<ContactItem> contactItems) {
        this.contactItems.clear();
        this.contactItems.addAll(contactItems);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return contactItems.size();
    }

    @Override
    public ContactItem getItem(int position) {
        return contactItems.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return Type.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type.ordinal();
    }

    @Override
    public boolean noDivider(int position) {
        return position > 0 && Type.values()[getItemViewType(position - 1)] == Type.SECTION;
    }

    @Override
    public boolean noFooterLine() {
        return false;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ContactItem item = getItem(position);
        if (view == null) {
            switch (item.type) {

                case SECTION:
                    view = inflater.inflate(R.layout.item_invite_section, parent, false);
                    break;

                case ITEM:
                    view = inflater.inflate(R.layout.item_invite, parent, false);
                    break;
            }
        }

        if (view instanceof InviteItemView) {
            InviteItemView itemView = (InviteItemView) view;
            itemView.bind(item.contact);
        } else {
            TextView sectionView = (TextView) view;
            sectionView.setText(item.section);
        }

        return view;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    /**
     * Updates this contact in the list.
     *
     * @param contact to be found and updated in the list
     */
    public void update(Contact contact) {
        int position = 0;
        for (ContactItem item : contactItems) {
            if (contact.equals(item.contact)) {
                break;
            }
            position++;
        }

        if (position < getCount()) {
            // copy this contact's data to one in the list
            getItem(position).contact.copy(contact);

            notifyDataSetChanged();
        }
    }

    /**
     * Builds contact item list from contacts.
     *
     * @param contacts to build from
     * @return contact items including sections and items
     */
    private static List<ContactItem> fromContacts(List<Contact> contacts) {
        Collections.sort(contacts);

        // form items for adapter, to display sections and items
        List<InviteAdapter.ContactItem> items = new ArrayList<>();
        String previousSection = null;
        for (Contact contact : contacts) {
            String section = InviteAdapter.ContactItem.section(contact);
            if (!section.equals(previousSection)) {
                items.add(InviteAdapter.ContactItem.newSection(section));
            }
            items.add(InviteAdapter.ContactItem.newItem(contact));
            previousSection = section;
        }

        return items;
    }

    public enum Type {

        SECTION, ITEM
    }

    private static class ContactItem {

        private final Type type;
        private final Contact contact;
        private final String section;

        ContactItem(Type type, Contact contact, String section) {
            this.type = type;
            this.contact = contact;
            this.section = section;
        }

        static ContactItem newSection(String section) {
            return new ContactItem(Type.SECTION, null, section);
        }

        static ContactItem newItem(Contact contact) {
            return new ContactItem(Type.ITEM, contact, null);
        }

        static String section(Contact contact) {
            if (TextUtils.isEmpty(contact.getName())) {
                return "#";
            } else {
                return contact.getName().substring(0, 1);
            }
        }
    }

    private class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Contact> result = new ArrayList<>();
            for (Contact item : contacts) {
                if (item.getName().startsWith(constraint.toString().toUpperCase())) {
                    result.add(item);
                }
            }

            List<ContactItem> contactItems = fromContacts(result);
            FilterResults filterResults = new FilterResults();
            filterResults.values = contactItems;
            filterResults.count = contactItems.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            setItems((List<ContactItem>) results.values);
        }
    }
}
