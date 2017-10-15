package cs3205.subsystem3.health.model;

import java.util.ArrayList;

/**
 * Created by Yee on 10/07/17.
 */

public class Steps {
    public static final String FIELD_TIME_FORMAT = "timeFormat";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_RECORD = "timeOfRecord";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_CHANNEL = "channel";
    public static final String[] FIELD_CHANNELS_TYPES = {"noOfSteps","differenceInTime"};

    private static final String MILLISECONDS = "milliseconds";
    private static final String TYPE = "steps";

    private String type;
    private String interval;
    private long timestamp;
    private ArrayList<Long> time;
    private ArrayList<Channel> channels;

    public Steps(long timestamp){
        this.interval = MILLISECONDS;
        this.type = TYPE;
        this.timestamp = timestamp;
        this.time = new ArrayList<Long>();
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

    public ArrayList<Long> getTime() {
        return time;
    }

    public void setTime(ArrayList<Long> time) {
        this.time = time;
    }

    public class Channel {
        private ArrayList<Long> values;

        public ArrayList<Long> getValues() {
            return values;
        }

        public void setValues(ArrayList<Long> values) {
            this.values = values;
        }
    }
}
