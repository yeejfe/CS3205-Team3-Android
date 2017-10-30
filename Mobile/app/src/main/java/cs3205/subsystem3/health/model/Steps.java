package cs3205.subsystem3.health.model;

import java.util.ArrayList;

/**
 * Created by Yee on 10/07/17.
 */

public class Steps {
    public static final String FIELD_UNIT = "unit";
    public static final String FIELD_MULTIPLIER = "multiplier";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_RECORD = "sessionTime";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_CHANNELS = "data";
    public static final String FIELD_DISPLAY_UNIT = "displayUnit";
    public static final String FIELD_DATA = "data";
    public static final String FIELD_AXIS_X = "x_axis";
    public static final String FIELD_AXIS_Y = "y_axis";
    public static final String FIELD_VALUE = "value";
    public static final String[] FIELD_CHANNELS_TYPES = {"differenceInTime"};

    private static final String MILLISECONDS = "milliseconds";
    private static final String TYPE = "steps";
    private static final String MULTIPLIER = "0.001";
    private static final String SECONDS = "seconds";

    private String title;
    private String type;
    private long timestamp;
    private String x;
    private String y;
    private Time time;
    private Channels channels;

    public Steps(long timestamp, String title) {
        this.title = title;
        this.type = TYPE;
        this.timestamp = timestamp;
        this.x = FIELD_TIME;
        this.y = FIELD_CHANNELS;

        this.time = new Time();
        this.channels = new Channels();
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

    public Channels getChannels() {
        return channels;
    }

    public void setChannels(Channels channels) {
        this.channels = channels;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
        private String displayUnit = SECONDS;
        private ArrayList<Long> values;

        public String getUnit() {
            return unit;
        }

        public String getMultiplier() {
            return multiplier;
        }

        public String getDisplayUnit() {
            return displayUnit;
        }

        public ArrayList<Long> getValues() {
            return values;
        }

        public void setValues(ArrayList<Long> values) {
            this.values = values;
        }
    }

    public class Channels {
        private String unit = MILLISECONDS;
        private String multiplier = MULTIPLIER;
        private String displayUnit = SECONDS;
        private ArrayList<Channel> data = new ArrayList<>();

        public String getUnit() {
            return unit;
        }

        public String getMultiplier() {
            return multiplier;
        }

        public String getDisplayUnit() {
            return displayUnit;
        }

        public ArrayList<Channel> getData() {
            return data;
        }

        public void setData(ArrayList<Channel> data) {
            this.data = data;
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
