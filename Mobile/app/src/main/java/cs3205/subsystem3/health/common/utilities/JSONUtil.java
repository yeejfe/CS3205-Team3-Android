package cs3205.subsystem3.health.common.utilities;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.model.Steps;

/**
 * Created by Yee on 10/08/17.
 */

public class JSONUtil {
    public static final String ERROR = "Error";

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

            JSONArray channelsArray = jsonObject.getJSONArray(Steps.FIELD_CHANNEL);
            ArrayList<Steps.Channel> channels = new ArrayList<Steps.Channel>();

            for (int i = 0; i < channelsArray.length(); i++) {
                Steps.Channel channel = data.new Channel();
                JSONObject channelObject = channelsArray.getJSONObject(i);

                channel.setName(channelObject.getString(Steps.FIELD_NAME));

                JSONArray channelArry = channelObject.getJSONArray(Steps.FIELD_VALUE);
                ArrayList<Long> values = new ArrayList<Long>();
                for (int j = 0; j < channelArry.length(); j++) {
                    values.get(channelArry.getInt(j));
                }

                channel.setValues(values);
                channels.add(channel);
            }

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
            jsonStepsObj.put(Steps.FIELD_NAME, data.getName());
            jsonStepsObj.put(Steps.FIELD_TYPE, data.getType());
            jsonStepsObj.put(Steps.FIELD_RECORD, data.getTimestamp());
            jsonStepsObj.put(Steps.FIELD_AXIS_X, data.getX());
            jsonStepsObj.put(Steps.FIELD_AXIS_Y, data.getY());

            jsonStepsObj.put(Steps.FIELD_TIME, getTime(data.getTime()));

            jsonStepsObj.put(Steps.FIELD_CHANNEL, getChannels(data.getChannels()));

            return jsonStepsObj;

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public static String stepsDataToJSONString(@NonNull Steps data) {
        return stepsDataToJSON(data).toString();
    }

    public static JSONObject updateStepsDataToJSON(@NonNull JSONObject stepJSON, ArrayList<Integer> time, ArrayList<Steps.Channel> channels) {
        try {
            stepJSON.put(Steps.FIELD_TIME, new JSONArray(time));
            stepJSON.put(Steps.FIELD_CHANNEL, getChannels(channels));
            return stepJSON;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String updateStepsDataToJSONString(@NonNull JSONObject stepJSON, ArrayList<Integer> time, ArrayList<Steps.Channel> channels) {
        try {
            stepJSON.put(Steps.FIELD_TIME, new JSONArray(time));
            stepJSON.put(Steps.FIELD_CHANNEL, getChannels(channels));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ERROR;
    }

    private static JSONObject getTime(Steps.Time time) {
        JSONObject timeObj = new JSONObject();

        try {
            timeObj.put(Steps.FIELD_UNIT, time.getUnit());
            timeObj.put(Steps.FIELD_MULTIPLIER, time.getMultiplier());

            JSONArray jsonValueArray = new JSONArray(time.getValues());

            timeObj.put(Steps.FIELD_VALUE, jsonValueArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return timeObj;
    }

    private static JSONArray getChannels(ArrayList<Steps.Channel> channels) {
        JSONArray channelsArray = new JSONArray();

        try {
            for (int i = 0; i < channels.size(); i++) {
                JSONObject channelObj = new JSONObject();
                JSONArray jsonValueArray = new JSONArray(channels.get(i).getValues());

                channelObj.put(Steps.FIELD_NAME, Steps.FIELD_CHANNELS_TYPES[i]);
                channelObj.put(Steps.FIELD_VALUE, jsonValueArray);

                channelsArray.put(channelObj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return channelsArray;
    }
}