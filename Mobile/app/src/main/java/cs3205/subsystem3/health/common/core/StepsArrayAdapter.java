package cs3205.subsystem3.health.common.core;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import cs3205.subsystem3.health.common.logger.Log;

/**
 * Created by Yee on 10/25/17.
 */

public class StepsArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private List<String> data;

    public StepsArrayAdapter(Context context, int layoutResourceId, List<String> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    public void refreshEvents(List<String> updatedData) {
        Log.d(this.getClass().getName(), String.valueOf(updatedData.size()));
        this.data.clear();
        Log.d(this.getClass().getName(), String.valueOf(updatedData.size()));
        boolean a = this.data.addAll(updatedData);
        Log.d(this.getClass().getName(), a + String.valueOf(this.data.size()));
        notifyDataSetChanged();
    }
}