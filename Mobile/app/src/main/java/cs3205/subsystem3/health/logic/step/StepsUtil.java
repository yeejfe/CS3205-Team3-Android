package cs3205.subsystem3.health.logic.step;

import java.util.ArrayList;

import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.model.Steps;

/**
 * Created by Yee on 10/16/17.
 */

public class StepsUtil {

    public static final long NO_OF_STEPS = 1;

    public static Steps updateSteps(Steps data, long timestamp) {
        if (data.getTimestamp() == 0) {
            data.setTimestamp(timestamp);
        }

        ArrayList<Long> time = data.getTime();
        time.add(timestamp);
        data.setTime(time);

        ArrayList<Steps.Channel> channels = data.getChannels();
        Steps.Channel noOfStepsChannel = data.new Channel();
        Steps.Channel timeDifferencesChannel = data.new Channel();

        ArrayList<Long> noOfSteps = new ArrayList<Long>();
        ArrayList<Long> timeDifferences = new ArrayList<Long>();
        long timeDiff;
        if(time.size() >= 1) {
            timeDiff = Timestamp.getEpochTimeMillis() - data.getTimestamp();
        } else {
            timeDiff = Timestamp.getEpochTimeMillis() - data.getTimestamp();
        }

        if (data.getChannels().size() == 0) {
            noOfSteps.add(NO_OF_STEPS);
            timeDifferences.add(timeDiff);
            noOfStepsChannel.setValues(noOfSteps);
            timeDifferencesChannel.setValues(timeDifferences);
            channels.add(noOfStepsChannel);
            channels.add(timeDifferencesChannel);
        } else {
            ArrayList<Long> values = new ArrayList<Long>();
            for (int i = 0; i < channels.size(); i++) {
                values = channels.get(i).getValues();

                if(i == 0) {
                    values.add(NO_OF_STEPS);
                    noOfStepsChannel.setValues(values);
                    channels.set(i, noOfStepsChannel);
                } else {
                    values.add(timeDiff);
                    timeDifferencesChannel.setValues(values);
                    channels.set(i, timeDifferencesChannel);
                }
            }
        }
        data.setChannels(channels);

        return data;
    }

    private static Steps updateSteps(Steps data, Long steps, long timestamp) {
        boolean timeValueExist = false;
        int index = -1;

        if (data.getTimestamp() == 0) {
            data.setTimestamp(timestamp);
        }

        ArrayList<Long> time = data.getTime();
        if (time.size() == 0) {
            time.add((long) 0);
        } else {
            long timeValue = Timestamp.getEpochTimeMillis() - data.getTimestamp();
            if (!time.contains(timeValue)) {
                time.add(timeValue);
            } else {
                index = time.indexOf(timeValue);
                timeValueExist = true;
            }
        }
        data.setTime(time);

        ArrayList<Steps.Channel> channels = data.getChannels();
        ArrayList<Long> values = new ArrayList<Long>();
        Steps.Channel channel = data.new Channel();
        if (data.getChannels().size() == 0) {
            values.add(steps);
            channel.setValues(values);
            channels.add(channel);
        } else {
            for (int i = 0; i < channels.size(); i++) {
                values = channels.get(i).getValues();
                if (timeValueExist) {
                    long updatedValue = values.get(index) + steps;
                    values.set(index, updatedValue);
                } else {
                    values.add(steps);
                }
                channel.setValues(values);
                channels.set(i, channel);
            }
        }
        data.setChannels(channels);

        return data;
    }
}
