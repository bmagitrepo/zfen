package com.zoomlee.zoo.invites;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.zoomlee.zoo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @since 23.04.15.
 */
public class Contact implements Comparable<Contact>, Parcelable {

    private long id;
    private String name = "";
    private Status status = Status.NONE;
    private Uri displayPhotoUri;
    private Set<Channel> channels = new HashSet<>();

    /**
     * Copies all data from other contact.
     *
     * @param other to be copied from
     */
    public void copy(Contact other) {
        id = other.id;
        name = other.name;
        status = other.status;
        displayPhotoUri = other.displayPhotoUri;
        channels = other.channels;
    }

    /**
     * @return raw contact id in Android DB
     */
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return display name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            // it shouldn't be null
            this.name = name.toUpperCase();
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Channel> getChannels() {
        List<Channel> result = new ArrayList<>(channels);
        Collections.sort(result); // phones first

        return result;
    }

    public void addChannel(ChannelType type, String value) {
        channels.add(new Channel(type, value));
    }

    /**
     * @return comma separated channels for contact to be invited
     */
    public String getChannelsString() {
        List<Channel> channels = getChannels();
        StringBuilder builder = new StringBuilder();
        for (Channel channel : channels) {
            builder.append(channel.value).append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        return builder.toString();
    }

    /**
     * @return display photo or null if no photo
     */
    public Uri getDisplayPhotoUri() {
        return displayPhotoUri;
    }

    public void setDisplayPhotoUri(Uri displayPhotoUri) {
        this.displayPhotoUri = displayPhotoUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contact contact = (Contact) o;

        if (id != contact.id) return false;
        return !(name != null ? !name.equals(contact.name) : contact.name != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(@NonNull Contact another) {
        return name.compareTo(another.name);
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", displayPhotoUri=" + displayPhotoUri +
                ", channels=" + channels +
                '}';
    }

    public enum Status {

        NONE(R.string.title_invite, true),
        ERROR(R.string.title_invite, true),
        INVITED(R.string.title_invited, false),
        JOINED(R.string.title_joined, false);

        public final int titleResId;
        public final boolean inviteEnabled;

        Status(int titleResId, boolean inviteEnabled) {
            this.titleResId = titleResId;
            this.inviteEnabled = inviteEnabled;
        }
    }

    public enum ChannelType {

        PHONE(R.drawable.ic_phone),
        EMAIL(R.drawable.ic_mail_green);

        public final int iconResId;

        ChannelType(int iconResId) {
            this.iconResId = iconResId;
        }
    }

    public static class Channel implements Comparable<Channel> {

        private final ChannelType type;
        private final String value;

        public Channel(ChannelType type, String value) {
            this.type = type;
            this.value = value;
        }

        public ChannelType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Channel channel = (Channel) o;

            if (type != channel.type) return false;
            return !(value != null ? !value.equals(channel.value) : channel.value != null);

        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public int compareTo(@NonNull Channel another) {
            return type.ordinal() - another.type.ordinal();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.status == null ? -1 : this.status.ordinal());
        dest.writeParcelable(this.displayPhotoUri, 0);
        dest.writeList(this.getChannels());
    }

    public Contact() {
    }

    private Contact(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        int tmpStatus = in.readInt();
        this.status = tmpStatus == -1 ? null : Status.values()[tmpStatus];
        this.displayPhotoUri = in.readParcelable(Uri.class.getClassLoader());
        List<Channel> channelList = new ArrayList<>();
        in.readList(channelList, List.class.getClassLoader());
        this.channels = new HashSet<>(channelList);
    }

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        public Contact createFromParcel(Parcel source) {
            return new Contact(source);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}
