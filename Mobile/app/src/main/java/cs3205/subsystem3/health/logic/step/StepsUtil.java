package cs3205.subsystem3.health.logic.step;

import java.util.ArrayList;

import cs3205.subsystem3.health.model.Steps;

/**
 * Created by Yee on 10/16/17.
 */

public class StepsUtil {

    public static final long NO_OF_STEPS = 1;
    public static final int INDEX_0 = 0;

    public static Steps updateSteps(Steps data, long timestamp) {
        long firstTimestamp;

        if (data.getTimestamp() == 0) {
            data.setTimestamp(timestamp);
        }

        Steps.Time time = data.getTime();
        ArrayList<Long> timeValues = time.getValues();
        if (timeValues == null) {
            timeValues = new ArrayList<>();
            firstTimestamp = timestamp;
        } else {
            firstTimestamp = timeValues.get(INDEX_0);
        }
        timeValues.add(timestamp - firstTimestamp);
        time.setValues(timeValues);
        data.setTime(time);

        ArrayList<Steps.Channel> channels = data.getChannels();
        Steps.Channel noOfStepsChannel = data.new Channel();
        Steps.Channel timeDifferencesChannel = data.new Channel();

        ArrayList<Long> noOfSteps = new ArrayList<Long>();
        ArrayList<Long> timeDifferences = new ArrayList<Long>();

        if (data.getChannels().size() == 0) {
            noOfSteps.add(NO_OF_STEPS);
            timeDifferences.add(0L);
            noOfStepsChannel.setValues(noOfSteps);
            timeDifferencesChannel.setValues(timeDifferences);
            channels.add(noOfStepsChannel);
            channels.add(timeDifferencesChannel);
        } else {
            ArrayList<Long> values = new ArrayList<Long>();
            for (int i = 0; i < channels.size(); i++) {
                values = channels.get(i).getValues();

                if (i == 0) {
                    values.add(NO_OF_STEPS);
                    noOfStepsChannel.setValues(values);
                    channels.set(i, noOfStepsChannel);
                } else {
                    values.add(timestamp - timeValues.get(timeValues.size() - 2));
                    timeDifferencesChannel.setValues(values);
                    channels.set(i, timeDifferencesChannel);
                }
            }
        }
        data.setChannels(channels);

        return data;
    }
}
