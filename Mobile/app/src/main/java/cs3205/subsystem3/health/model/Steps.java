package cs3205.subsystem3.health.model;

import java.util.ArrayList;

/**
 * Created by Yee on 10/07/17.
 */

public class Steps {
    private static String INTERVAL = "seconds";
    private static String TYPE = "steps";

    private String interval;
    private String type;
    private long timestamp;
    private ArrayList<Integer> time;
    private Channel channel;

    public Steps(){
        this.interval = INTERVAL;
        this.type = TYPE;
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

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public ArrayList<Integer> getTime() {
        return time;
    }

    public void setTime(ArrayList<Integer> time) {
        this.time = time;
    }

    public class Channel {
        private ArrayList<ArrayList<Integer>> channels;

        public ArrayList<ArrayList<Integer>> getChannels() {
            return channels;
        }

        public void setChannels(ArrayList<ArrayList<Integer>> channels) {
            this.channels = channels;
        }
    }
}
