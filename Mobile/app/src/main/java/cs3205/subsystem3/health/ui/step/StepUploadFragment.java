package cs3205.subsystem3.health.ui.step;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.common.miscellaneous.Value;
import cs3205.subsystem3.health.common.utilities.StepsUploadTask;
import cs3205.subsystem3.health.data.source.local.Repository;
import cs3205.subsystem3.health.ui.nfc.NFCReaderActivity;

import static cs3205.subsystem3.health.common.core.JSONFileWriter.FOLDER;
import static cs3205.subsystem3.health.common.core.JSONFileWriter.FRONT_SLASH;

/**
 * Created by Yee on 10/06/17.
 */

public class StepUploadFragment extends Fragment implements View.OnClickListener {
    public static final String STEPS = FRONT_SLASH + FOLDER;
    ListView listView;
    Button buttonUpload;

    ArrayAdapter arrayAdapter;

    ArrayList<ArrayList<String>> filesinfolder = new ArrayList<ArrayList<String>>();
    ArrayList<String> selectedItems = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_step_upload, null);

        filesinfolder = Repository.getFiles(getActivity().getFilesDir().getAbsolutePath() + STEPS);

        listView = (ListView) view.findViewById(R.id.steps_list_view);
        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, filesinfolder.get(Repository.sessionNames));
        listView.setAdapter(arrayAdapter);

        buttonUpload = (Button) view.findViewById(R.id.btn_step_upload);

        buttonUpload.setOnClickListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                checkSizeOfSelected(checked.size());
            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_step_upload:
                promptForUpload();
                break;
        }
    }

    private void checkSizeOfSelected(int checkedSize) {
        if (checkedSize == 0) {
            buttonUpload.setEnabled(false);
        } else {
            buttonUpload.setEnabled(true);
        }
    }

    private int getCheckedFiles() {
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        for (int i = 0; i < checked.size(); i++) {
            // Item position in arrayAdapter
            int position = checked.keyAt(i);
            // Add if it is checked i.e.) == TRUE!
            if (checked.valueAt(i))
                selectedItems.add(filesinfolder.get(Repository.filePaths).get(position));
        }

        return checked.size();
    }

    private void promptForUpload() {
        if (getCheckedFiles() == 0) {
            Toast.makeText(getActivity(), AppMessage.TOAST_MESSAGE_NO_FILE_SELECTED, Toast.LENGTH_SHORT).show();
            buttonUpload.setEnabled(false);
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Confirm Upload");
        alertDialogBuilder.setMessage("Are you sure?");
        alertDialogBuilder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent startNFCReadingActivity = new Intent(getActivity(), NFCReaderActivity.class);
                startActivityForResult(startNFCReadingActivity, 88);
            }
        });

        alertDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        buttonUpload.setEnabled(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 88) {
            String tag_password = data.getStringExtra(Value.KEY_VALUE_LOGIN_INTENT_PASSWORD);
            new StepsUploadTask().execute(tag_password,
                    String.valueOf(System.currentTimeMillis()), selectedItems, getContext());
        }
    }
}
