package com.zoomlee.zoo.net.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 3/2/15
 */
public class Place {
    private String id;
    private String name;
    private Contact contact;
    private Location location;

    public LatLng getLocation() {
        return new LatLng(location.lat, location.lng);
    }

    public double getLatitude() {
        return location.lat;
    }

    public double getLongitude() {
        return location.lng;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return contact.formattedPhone != null ? contact.formattedPhone : contact.phone;
    }

    public String getAddress() {
        if (location.formattedAddress == null || location.formattedAddress.length < 3)
            return null;//TODO: check it
        return String.format("%s, %s, %s", location.formattedAddress[2], location.formattedAddress[1], location.formattedAddress[0]);
    }

    class Contact {
        String phone;
        String formattedPhone;

        @Override
        public String toString() {
            return "Contact{" +
                    "phone='" + phone + '\'' +
                    ", formattedPhone='" + formattedPhone + '\'' +
                    '}';
        }
    }

    class Location {
        String address;
        String[] formattedAddress;
        double lat;
        double lng;
        String distance;

        @Override
        public String toString() {
            return "Location{" +
                    "address='" + address + '\'' +
                    ", lat='" + lat + '\'' +
                    ", lng='" + lng + '\'' +
                    ", distance='" + distance + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Place{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", contact=" + contact +
                ", location=" + location +
                '}';
    }
}
