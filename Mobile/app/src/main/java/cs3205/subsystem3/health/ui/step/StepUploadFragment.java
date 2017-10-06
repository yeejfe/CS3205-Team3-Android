package cs3205.subsystem3.health.ui.step;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;

/**
 * Created by Yee on 10/06/17.
 */

public class StepUploadFragment extends Fragment implements View.OnClickListener {
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_step_upload, null);

        ArrayList<String> filesinfolder = GetFiles(getActivity().getExternalFilesDir(null).getAbsolutePath() + "/steps");

        listView = (ListView) view.findViewById(R.id.steps_list_view);
        listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, filesinfolder));

        return view;
    }

    @Override
    public void onClick(View view) {
        SharedPreferences pref = getActivity().getSharedPreferences("Token_SharedPreferences", Activity.MODE_PRIVATE);
        String token = pref.getString("access_token", "");
        String hash = pref.getString("nfc_hash", "");

        File file = new File(listView.getSelectedItem().toString());

        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RemoteDataSource rDS = new RemoteDataSource();
        rDS.buildStepUploadRequest(stream,token,hash);
        rDS.close();
    }

    public ArrayList<String> GetFiles(String dirPath) {
        ArrayList<String> arrayListOfFiles = new ArrayList<String>();
        File f = new File(dirPath);
        f.mkdirs();
        File[] listFiles = f.listFiles();
        if (listFiles.length == 0) {
            return null;
        } else {
            for (int i = 0; i < listFiles.length; i++)
                arrayListOfFiles.add(listFiles[i].getAbsolutePath());
        }
        return arrayListOfFiles;
    }
}
