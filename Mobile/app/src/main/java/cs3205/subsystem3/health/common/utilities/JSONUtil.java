package cs3205.subsystem3.health.common.utilities;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cs3205.subsystem3.health.model.Steps;

import static cs3205.subsystem3.health.model.Steps.FIELD_CHANNELS_TYPES;

/**
 * Created by Yee on 10/08/17.
 */

public class JSONUtil {
    public static final String ERROR = "Error";

    public static final Steps JSONtoSteps(@NonNull JSONObject jsonObject) {
        try {
            Steps data = new Steps(jsonObject.getLong(Steps.FIELD_RECORD));

            JSONArray jTimeArr = jsonObject.getJSONArray(Steps.FIELD_TIME);
            ArrayList<Long> time = new ArrayList<Long>();
            for (int i = 0; i < jTimeArr.length(); i++) {
                time.add(jTimeArr.getLong(i));
            }
            data.setTime(time);

            JSONObject jChannelsObj = jsonObject.getJSONObject(Steps.FIELD_CHANNEL);
            ArrayList<Steps.Channel> channels = new ArrayList<Steps.Channel>();

            for (int i = 0; i < jChannelsObj.length(); i++) {
                Steps.Channel channel = data.new Channel();
                JSONArray channelArry = jChannelsObj.getJSONArray(FIELD_CHANNELS_TYPES[i]);
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
            jsonStepsObj.put(Steps.FIELD_TYPE, data.getType());
            jsonStepsObj.put(Steps.FIELD_TIME_FORMAT, data.getInterval());
            jsonStepsObj.put(Steps.FIELD_RECORD, data.getTimestamp());

            JSONArray jsonTimeArray = new JSONArray(data.getTime());
            jsonStepsObj.put(Steps.FIELD_TIME, jsonTimeArray);

            jsonStepsObj.put(Steps.FIELD_CHANNEL, getChannels(data.getChannels()));

            return jsonStepsObj;

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public static String stepsDataToJSONString(@NonNull Steps data) {
        try {
            JSONObject jsonStepsObj = new JSONObject();
            jsonStepsObj.put(Steps.FIELD_TYPE, data.getType());
            jsonStepsObj.put(Steps.FIELD_TIME_FORMAT, data.getInterval());
            jsonStepsObj.put(Steps.FIELD_RECORD, data.getTimestamp());

            JSONArray jsonTimeArray = new JSONArray(data.getTime());
            jsonStepsObj.put(Steps.FIELD_TIME, jsonTimeArray);

            jsonStepsObj.put(Steps.FIELD_CHANNEL, getChannels(data.getChannels()));

            return jsonStepsObj.toString();

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return ERROR;

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

    private static JSONObject getChannels(ArrayList<Steps.Channel> channels) {
        JSONObject channelObj = new JSONObject();

        try {
            for (int i = 0; i < channels.size(); i++) {
                JSONArray jsonValueArray = new JSONArray(channels.get(i).getValues());
                channelObj.put(FIELD_CHANNELS_TYPES[i], jsonValueArray);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return channelObj;
    }
}