package com.zoomlee.zoo.utils;

import com.zoomlee.zoo.invites.Contact;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Form;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.net.model.Tax;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 11.03.15.
 */
public final class Events {

    public static class GroupsChanged {}
    public static class FilesTypesChanged {}
    public static class DocumentsTypesChanged {}
    public static class DocumentsType2FieldsChanged {}
    public static class CountriesChanged {}
    public static class ColorsChanged {}
    public static class CategoriesChanged {}
    public static class CategoriesDocumentsTypesChanged {}
    public static class FieldsTypesChanged {}

    public static class DocumentChanged {
        public static final int UPDATED = 1;
        public static final int DELETED = -1;

        private final int status;
        private final Document document;

        public DocumentChanged(int status, Document document) {
            this.status = status;
            this.document = document;
        }

        public int getStatus() {
            return status;
        }

        public Document getDocument() {
            return document;
        }
    }

    public static class PersonChanged {
        public static final int UPDATED = 1;
        public static final int DELETED = -1;

        private final int status;
        private final Person person;

        public PersonChanged(int status, Person person) {
            this.status = status;
            this.person = person;
        }

        public int getStatus() {
            return status;
        }

        public Person getPerson() {
            return person;
        }
    }

    public static class TagChanged {
        public static final int UPDATED = 1;
        public static final int DELETED = -1;

        private final int status;
        private final Tag tag;

        public TagChanged (int status, Tag tag) {
            this.status = status;
            this.tag = tag;
        }

        public int getStatus() {
            return status;
        }

        public Tag getTag() {
            return tag;
        }
    }

    public static class InviteClicked {

        private final Contact contact;

        public InviteClicked(Contact contact) {
            this.contact = contact;
        }

        public Contact getContact() {
            return contact;
        }
    }

    public static class TaxChanged {
        public static final int UPDATED = 1;
        public static final int DELETED = -1;

        private final int status;
        private final Tax tax;

        public TaxChanged (int status, Tax tax) {
            this.status = status;
            this.tax = tax;
        }

        public int getStatus() {
            return status;
        }

        public Tax getTax() {
            return tax;
        }
    }

    public static class FormChanged {
        public static final int UPDATED = 1;
        public static final int DELETED = -1;

        private final int status;
        private final Form form;

        public FormChanged (int status, Form form) {
            this.status = status;
            this.form = form;
        }

        public int getStatus() {
            return status;
        }

        public Form getForm() {
            return form;
        }
    }
}
