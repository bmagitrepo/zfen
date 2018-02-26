package com.zoomlee.zoo.net.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 3/3/15
 */
public class FQResponse {
    private Meta meta;
    private Response response;
    class Meta{
    int code;
    }

    class Response{
        @SerializedName("venues")
        List<Place> places;

        public List<Place> getPlaces(){
            return places;
        }
    }

    public List<Place> getPlaces(){
        return response.getPlaces();
    }

}
