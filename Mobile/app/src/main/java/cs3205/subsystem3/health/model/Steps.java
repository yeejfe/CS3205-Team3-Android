package cs3205.subsystem3.health.model;

import java.util.ArrayList;

/**
 * Created by Yee on 10/07/17.
 */

public class Steps {
    public static final String FIELD_UNIT = "unit";
    public static final String FIELD_MULTIPLIER = "multiplier";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_RECORD = "sessionTime";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_CHANNEL = "channels";
    public static final String FIELD_AXIS_X = "x_axis";
    public static final String FIELD_AXIS_Y = "y_axis";
    public static final String FIELD_VALUE = "value";
    public static final String[] FIELD_CHANNELS_TYPES = {"noOfSteps","differenceInTime"};

    private static final String MILLISECONDS = "milliseconds";
    private static final String TYPE = "steps";
    private static final String MULTIPLIER = "0.001";

    private String name;
    private String type;
    private long timestamp;
    private String x;
    private String y;
    private Time time;
    private ArrayList<Channel> channels;

    public Steps(long timestamp, String name) {
        this.name = name;
        this.type = TYPE;
        this.timestamp = timestamp;
        this.x = FIELD_TIME;
        this.y = FIELD_CHANNEL;

        this.time = new Time();
        this.channels = new ArrayList<Channel>();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public class Time {
        private String unit = MILLISECONDS;
        private String multiplier = MULTIPLIER;
        private ArrayList<Long> values;

        public String getUnit() {
            return unit;
        }

        public String getMultiplier() {
            return multiplier;
        }

        public ArrayList<Long> getValues() {
            return values;
        }

        public void setValues(ArrayList<Long> values) {
            this.values = values;
        }
    }

    public class Channel {
        private String name;
        private ArrayList<Long> values;

        public ArrayList<Long> getValues() {
            return values;
        }

        public void setValues(ArrayList<Long> values) {
            this.values = values;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
