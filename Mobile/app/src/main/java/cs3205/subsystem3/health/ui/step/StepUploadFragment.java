package cs3205.subsystem3.health.ui.step;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.data.source.local.Repository;
import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;

import static cs3205.subsystem3.health.common.core.JSONFileWriter.FOLDER;
import static cs3205.subsystem3.health.common.core.JSONFileWriter.FRONT_SLASH;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.ACCESS_TOKEN;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.EMPTY_STRING;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.NFC_HASH;
import static cs3205.subsystem3.health.common.core.SharedPreferencesConstant.TOKEN_SHARED_PREFERENCES;

/**
 * Created by Yee on 10/06/17.
 */

public class StepUploadFragment extends Fragment implements View.OnClickListener {
    public static final String STEPS = FRONT_SLASH + FOLDER;
    ListView listView;
    Button buttonUpload;

    private String selectedItem = "";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_step_upload, null);

        ArrayList<String> filesinfolder = Repository.getFiles(getActivity().getExternalFilesDir(null).getAbsolutePath() + STEPS);

        listView = (ListView) view.findViewById(R.id.steps_list_view);
        listView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, filesinfolder));

        buttonUpload = (Button) view.findViewById(R.id.btn_step_upload);

        buttonUpload.setOnClickListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectListItems(position);
            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_step_upload:
                upload();
                break;
        }
    }

    private void selectListItems(int pos) {
        selectedItem = listView.getItemAtPosition(pos).toString();
        buttonUpload.setEnabled(true);
    }

    private void upload() {
        SharedPreferences pref = getActivity().getSharedPreferences(TOKEN_SHARED_PREFERENCES, Activity.MODE_PRIVATE);
        String token = pref.getString(ACCESS_TOKEN, EMPTY_STRING);
        String hash = pref.getString(NFC_HASH, EMPTY_STRING);

        File file = new File(selectedItem);

        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RemoteDataSource rDS = new RemoteDataSource();
        Log.i("UPload", "Upload");
        rDS.buildFileUploadRequest(stream, token, hash, Long.valueOf(file.getName()), RemoteDataSource.Type.STEPS);
        Toast.makeText(getActivity(), "Upload Successful.", Toast.LENGTH_SHORT).show();
        Log.i("UPload", rDS.toString());
        rDS.close();

        buttonUpload.setEnabled(false);
    }
}
