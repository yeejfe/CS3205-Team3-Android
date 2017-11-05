package cs3205.subsystem3.health.logic.step;

import java.util.ArrayList;

import cs3205.subsystem3.health.model.Steps;

/**
 * Created by Yee on 10/16/17.
 */

public class StepsUtil {
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
            firstTimestamp = data.getTimestamp();
        }
        timeValues.add(timestamp - firstTimestamp);
        time.setValues(timeValues);
        data.setTime(time);

        ArrayList<Steps.Channel> channels = data.getChannels().getData();
        Steps.Channel timeDifferencesChannel = data.new Channel();

        ArrayList<Long> timeDifferences = new ArrayList<Long>();

        if (data.getChannels().getData().size() == 0) {
            timeDifferences.add(0L);
            timeDifferencesChannel.setValues(timeDifferences);
            channels.add(timeDifferencesChannel);
        } else {
            for (int i = 0; i < channels.size(); i++) {
                ArrayList<Long> values = channels.get(i).getValues();

                long prevTimestamp = firstTimestamp + timeValues.get(timeValues.size() - 2);
                values.add(timestamp - prevTimestamp);
                timeDifferencesChannel.setValues(values);
                channels.set(i, timeDifferencesChannel);

            }
        }
        data.getChannels().setData(channels);

        return data;
    }
}
