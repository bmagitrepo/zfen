package com.zoomlee.zoo.syncservice;

import com.zoomlee.zoo.net.RestTask;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 30.01.15.
 */
public class SyncEvents {
    public static final int SYNC_STARTED = 1;
    public static final int SYNC_FINISHED = 2;
    public static final int SYNC_ERROR = 3;

    private SyncEvents(){}

    public static class SyncServiceStatus {
        private int status;

        public SyncServiceStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    public static class RestTaskStatus {
        private int status;
        private RestTask restTask;

        public RestTaskStatus(int status, RestTask restTask) {
            this.status = status;
            this.restTask = restTask;
        }

        public int getStatus() {
            return status;
        }

        public RestTask getRestTask() {
            return restTask;
        }
    }
}
