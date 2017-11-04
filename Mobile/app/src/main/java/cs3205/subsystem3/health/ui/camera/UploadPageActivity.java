package cs3205.subsystem3.health.ui.camera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.common.miscellaneous.Value;
import cs3205.subsystem3.health.common.utilities.JSONWebToken;
import cs3205.subsystem3.health.common.utilities.SessionManager;
import cs3205.subsystem3.health.common.utilities.UploadHandler;
import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;
import cs3205.subsystem3.health.logic.camera.MetaInfoExtractor;
import cs3205.subsystem3.health.logic.camera.PathExtractor;
import cs3205.subsystem3.health.ui.nfc.NFCReaderActivity;

/**
 * Created by panjiyun.
 */

public class UploadPageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_LOAD_IMAGE = 1;
    private static final int REQUEST_LOAD_VIDEO = 2;
    public static final int REQUEST_READ_NFC = 70;

    private ImageView imageToUpload, videoToUpload;
    private Button bUploadImage, bUploadVideo;
    private TextView uploadImageName, uploadVideoName;

    private String selectedPath = null;
    private String selectedImagePath = null;
    private String selectedVideoPath = null;

    private String nfcToken;
    private String jwtToken;
    private RemoteDataSource.Type choice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_upload_page);

        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);
        bUploadImage = (Button) findViewById(R.id.buttonUploadImage);
        uploadImageName = (TextView) findViewById(R.id.imageName);

        videoToUpload = (ImageView) findViewById(R.id.videoToUpload);
        bUploadVideo = (Button) findViewById(R.id.buttonUploadVideo);
        uploadVideoName = (TextView) findViewById(R.id.videoName);

        imageToUpload.setOnClickListener(this);
        videoToUpload.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);
        bUploadVideo.setOnClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionManager.isLogoutTimerSet()) {
            SessionManager.cancelLogoutTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SessionManager.isLogoutTimerSet()) {
            SessionManager.resetLogoutTimer(this);
        } else {
            SessionManager.setLogoutTimer(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageToUpload:
                Intent imageGalleryIntent = new Intent(this, CustomGallery.class);
                imageGalleryIntent.putExtra(CameraActivity.GALLERY_REQUEST_TYPE, CameraActivity.GalleryRequestType.UPLOAD);
                startActivityForResult(imageGalleryIntent, REQUEST_LOAD_IMAGE);
                break;
            case R.id.buttonUploadImage:
                choice = RemoteDataSource.Type.IMAGE;
                if (selectedImagePath == null) {
                    break;
                } else {
                    getJwtToken();
                    getNfcToken();
                    break;
                }
            case R.id.videoToUpload:
                Intent VideoGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(VideoGalleryIntent, REQUEST_LOAD_VIDEO);
                break;
            case R.id.buttonUploadVideo:
                choice = RemoteDataSource.Type.VIDEO;
                if (selectedVideoPath == null) {
                    break;
                } else {
                    getJwtToken();
                    getNfcToken();
                    break;
                }


        }

    }

    private void getJwtToken() {
        jwtToken = "";
        jwtToken = JSONWebToken.getInstance().getData();
        Log.d(this.getClass().getSimpleName(), jwtToken);
    }


    private void getNfcToken() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, AppMessage.TOAST_MESSAGE_NFC_UNAVAILABLE, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent startNFCReadingActivity = new Intent(this, NFCReaderActivity.class);
        startActivityForResult(startNFCReadingActivity, REQUEST_READ_NFC);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {

            selectedImagePath = data.getStringExtra(CustomGallery.SELECTED_IMAGE_PATH);
            Uri selectedImageUri = Uri.fromFile(new File(selectedImagePath));
            uploadImageName.setText("Image to upload: " + MetaInfoExtractor.getFileName(selectedImagePath) + "\nEpoch time: " + MetaInfoExtractor.getEpochTimeStamp(selectedImagePath));
            imageToUpload.setImageURI(selectedImageUri);

        } else if (requestCode == REQUEST_LOAD_VIDEO && resultCode == RESULT_OK && data != null) {
            Uri selectedVideoUri = data.getData();
            selectedVideoPath = PathExtractor.getPath(this, selectedVideoUri);
            uploadVideoName.setText("Video to upload: " + MetaInfoExtractor.getFileName(selectedVideoPath) + "\nEpoch time: " + MetaInfoExtractor.getEpochTimeStamp(selectedVideoPath));
            videoToUpload.setVisibility(View.GONE);

        } else if (requestCode == REQUEST_READ_NFC) {
            if (resultCode == RESULT_OK) {
                nfcToken = data.getStringExtra(Value.KEY_VALUE_LOGIN_INTENT_PASSWORD);
                Log.d(this.getClass().getSimpleName(), "NFC token is " + nfcToken);
                if (choice.equals(RemoteDataSource.Type.IMAGE)) {
                    selectedPath = selectedImagePath;
                } else {
                    selectedPath = selectedVideoPath;
                }
                UploadHandler uploadHander = new UploadHandler(selectedPath, choice, this, jwtToken, nfcToken);
                uploadHander.startUpload();
            }
        }

    }

}
