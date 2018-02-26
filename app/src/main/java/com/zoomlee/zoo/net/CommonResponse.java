package com.zoomlee.zoo.net;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 1/13/15
 */
public class CommonResponse<Body> {
    private Error error;
    private Body body;

    public Error getError() {
        return error;
    }

    public Body getBody() {
        return body;
    }

}
