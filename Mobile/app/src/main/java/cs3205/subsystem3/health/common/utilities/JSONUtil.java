package cs3205.subsystem3.health.common.utilities;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cs3205.subsystem3.health.model.Steps;

/**
 * Created by Yee on 10/08/17.
 */

public class JSONUtil {
    public static final Steps JSONtoSteps(@NonNull JSONObject jsonObject) {
        try {
            Steps data = new Steps(jsonObject.getLong(Steps.FIELD_RECORD), jsonObject.getString(Steps.FIELD_NAME));

            JSONObject timeObj = jsonObject.getJSONObject(Steps.FIELD_TIME);
            JSONArray jTimeArr = timeObj.getJSONArray(Steps.FIELD_VALUE);
            Steps.Time time = data.new Time();
            ArrayList<Long> timeValues = new ArrayList<Long>();

            for (int i = 0; i < jTimeArr.length(); i++) {
                timeValues.add(jTimeArr.getLong(i));
            }
            time.setValues(timeValues);

            JSONObject channelsObj = jsonObject.getJSONObject(Steps.FIELD_CHANNELS);

            JSONArray channelsArray = channelsObj.getJSONArray(Steps.FIELD_DATA);
            ArrayList<Steps.Channel> channelData = new ArrayList<Steps.Channel>();

            for (int i = 0; i < channelsArray.length(); i++) {
                Steps.Channel channel = data.new Channel();
                JSONObject channelObject = channelsArray.getJSONObject(i);

                channel.setName(channelObject.getString(Steps.FIELD_NAME));

                JSONArray channelArray = channelObject.getJSONArray(Steps.FIELD_VALUE);
                ArrayList<Long> values = new ArrayList<Long>();
                for (int j = 0; j < channelArray.length(); j++) {
                    values.add(channelArray.getLong(j));
                }

                channel.setValues(values);
                channelData.add(channel);
            }

            Steps.Channels channels = data.new Channels();
            channels.setData(channelData);
            data.setChannels(channels);

            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject stepsDataToJSON(@NonNull Steps data) {
        try {
            JSONObject jsonStepsObj = new JSONObject();
            jsonStepsObj.put(Steps.FIELD_TITLE, data.getTitle());
            jsonStepsObj.put(Steps.FIELD_TYPE, data.getType());
            jsonStepsObj.put(Steps.FIELD_RECORD, data.getTimestamp());
            jsonStepsObj.put(Steps.FIELD_AXIS_X, data.getX());
            jsonStepsObj.put(Steps.FIELD_AXIS_Y, data.getY());

            jsonStepsObj.put(Steps.FIELD_TIME, getTime(data.getTime()));

            jsonStepsObj.put(Steps.FIELD_CHANNELS, getChannels(data.getChannels()));

            return jsonStepsObj;

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    private static JSONObject getTime(Steps.Time time) {
        JSONObject timeObj = new JSONObject();

        try {
            timeObj.put(Steps.FIELD_UNIT, time.getUnit());
            timeObj.put(Steps.FIELD_MULTIPLIER, time.getMultiplier());
            timeObj.put(Steps.FIELD_DISPLAY_UNIT, time.getDisplayUnit());

            JSONArray jsonValueArray = new JSONArray(time.getValues());

            timeObj.put(Steps.FIELD_VALUE, jsonValueArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return timeObj;
    }

    private static JSONObject getChannels(Steps.Channels channels) {
        JSONObject channelsObj = new JSONObject();
        JSONArray channelsArray = new JSONArray();

        try {
            channelsObj.put(Steps.FIELD_UNIT, channels.getUnit());
            channelsObj.put(Steps.FIELD_MULTIPLIER, channels.getMultiplier());
            channelsObj.put(Steps.FIELD_DISPLAY_UNIT, channels.getDisplayUnit());

            for (int i = 0; i < channels.getData().size(); i++) {
                JSONObject channelObj = new JSONObject();
                JSONArray jsonValueArray = new JSONArray(channels.getData().get(i).getValues());

                channelObj.put(Steps.FIELD_NAME, Steps.FIELD_CHANNELS_TYPES[i]);
                channelObj.put(Steps.FIELD_VALUE, jsonValueArray);

                channelsArray.put(channelObj);
            }
            channelsObj.put(Steps.FIELD_DATA, channelsArray);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return channelsObj;
    }
}