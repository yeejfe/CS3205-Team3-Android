package cs3205.subsystem3.health.model;

import java.util.ArrayList;

/**
 * Created by Yee on 10/07/17.
 */

public class Steps {
    public static final String FIELD_INTERVAL = "interval";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_CHANNEL = "channel";

    private static final String INTERVAL = "seconds";
    private static final String TYPE = "steps";

    private String type;
    private String interval;
    private long timestamp;
    private ArrayList<Integer> time;
    private ArrayList<Channel> channels;

    public Steps(long timestamp){
        this.interval = INTERVAL;
        this.type = TYPE;
        this.timestamp = timestamp;
        this.time = new ArrayList<Integer>();
        this.channels = new ArrayList<Channel>();
    }

    public String getInterval() {
        return interval;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<Channel> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<Channel> channels) {
        this.channels = channels;
    }

    public ArrayList<Integer> getTime() {
        return time;
    }

    public void setTime(ArrayList<Integer> time) {
        this.time = time;
    }

    public class Channel {
        private ArrayList<Integer> values;

        public ArrayList<Integer> getValues() {
            return values;
        }

        public void setValues(ArrayList<Integer> values) {
            this.values = values;
        }
    }
}
