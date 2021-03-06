package org.a0z.mpd;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by facetoe on 26/12/13.
 */
public class MPDResponse implements Serializable {

    // Event-ID's for PMix internal events...
    public static final int EVENT_CONNECT = 0;
    public static final int EVENT_DISCONNECT = 1;
    public static final int EVENT_CONNECTFAILED = 2;
    public static final int EVENT_CONNECTSUCCEEDED = 3;
    public static final int EVENT_STARTMONITOR = 4;
    public static final int EVENT_STOPMONITOR = 5;
    public static final int EVENT_EXECASYNC = 6;
    public static final int EVENT_EXECASYNCFINISHED = 7;

    // Event-ID's for JMPDComm events (from the listener)...
    public static final int EVENT_CONNECTIONSTATE = 11;
    public static final int EVENT_PLAYLIST = 12;
    public static final int EVENT_RANDOM = 13;
    public static final int EVENT_REPEAT = 14;
    public static final int EVENT_STATE = 15;
    public static final int EVENT_TRACK = 16;
    public static final int EVENT_UPDATESTATE = 17;
    public static final int EVENT_VOLUME = 18;
    public static final int EVENT_TRACKPOSITION = 19;

    // Events I've added for Bluetooth
    public static final int EVENT_GET_PLAYLIST_CHANGES = 20;
    public static final int SYNC_READ_WRITE = 21;
    public static final int EVENT_UPDATE_RAW_CHANGES = 22;
    public static final int EVENT_ERROR = 23;

    private int responseType;
    private int numObjects;
    private String[] objectJSON;

    private boolean synchronous;

    public MPDResponse(int responseType, Object... obj) {
        this.responseType = responseType;
        numObjects = obj.length;
        objectJSON = new String[numObjects];
        Gson gson = new Gson();
        for (int i = 0; i < numObjects; i++) {
            objectJSON[i] = gson.toJson(obj[i]);
        }
    }

    public String getObjectJSON(int index) {
        if (index >= numObjects || index < 0)
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        return objectJSON[index];
    }

    public int getResponseType() {
        return responseType;
    }

    public int getNumObjects() {
        return numObjects;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    @Override
    public String toString() {
        return "MPDResponse{" +
                "responseType=" + responseType +
                ", numObjects=" + numObjects +
                '}';
    }
}
