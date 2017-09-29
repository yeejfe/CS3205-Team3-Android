package cs3205.subsystem3.health.ui.healthcamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.R;


public class UploadPage extends Activity implements View.OnClickListener {

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_LOAD_VIDEO = 2;
    public static final String SERVER3_ADDRESS = "https://cs3205-3.comp.nus.edu.sg/upload";
    public static final String MESSAGE_EXCEED_MAX_SIZE = "Exceeded the maximum size: 10MB";
    public static final String MESSAGE_RESPONSE_TITLE = "Response from Servers";
    public static final String MESSAGE_SUCCESSFUL = "Successful";
    public static final String MESSAGE_FAIL = "Fail";

    private ImageView imageToUpload,videoToUpload;
    private VideoView videoToPreview;
    private Button bUploadImage,bUploadVideo;
    private TextView uploadImageName,uploadVideoName;

    private Uri objectToUpload = null;

    private String selectedPath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_page);

        imageToUpload = (ImageView)findViewById(R.id.imageToUpload);
        bUploadImage = (Button)findViewById(R.id.buttonUploadImage);
        uploadImageName = (TextView)findViewById(R.id.imageName);

        videoToUpload = (ImageView)findViewById(R.id.videoToUpload);
        bUploadVideo = (Button)findViewById(R.id.buttonUploadVideo);
        uploadVideoName = (TextView)findViewById(R.id.videoName);
        videoToPreview = (VideoView)findViewById(R.id.videoToPreview);

        imageToUpload.setOnClickListener(this);
        videoToUpload.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);
        bUploadVideo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.imageToUpload:
                Intent imageGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(imageGalleryIntent,RESULT_LOAD_IMAGE);
                break;
            case R.id.buttonUploadImage:
                uploadFileToServer(objectToUpload);
                break;
            case R.id.videoToUpload:
                Intent VideoGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(VideoGalleryIntent,RESULT_LOAD_VIDEO);
                break;
            case R.id.buttonUploadVideo:
                uploadFileToServer(objectToUpload);
                break;


        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RESULT_LOAD_IMAGE && resultCode==RESULT_OK && data!=null){
            Uri selectedImageUri = data.getData();
            imageToUpload.setImageURI(selectedImageUri);
            objectToUpload = selectedImageUri;

        }
        else if (requestCode==RESULT_LOAD_VIDEO && resultCode==RESULT_OK && data!=null) {
            Uri selectedVideoUri = data.getData();
            selectedPath = getVideoPath(selectedVideoUri);
            uploadVideoName.setText(selectedPath);
            videoToUpload.setVisibility(View.GONE);
            videoToPreview.setVideoURI(selectedVideoUri);
            videoToPreview.setVisibility(View.VISIBLE);
            objectToUpload = selectedVideoUri;
        }

    }

    public boolean uploadFileToServer(Uri pathUri){

        File f = new File(pathUri.getPath());
        long length = f.length()/(1024*1024);  // length is expressed in MB
        if(length<10.00) {
            InputStream stream = null;
            try {
                stream = new FileInputStream(f);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Client client = ClientBuilder.newClient();
            Invocation.Builder builder = client.target(SERVER3_ADDRESS)
                  // .queryParam() //type
                  //  .queryParam() //epoch unixtime
                    .request();
            Response response = builder.post(Entity.entity(stream, "application/json"));
            // Check Response


            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                showAlert(MESSAGE_SUCCESSFUL);
                return true;

            }
            else {
                showAlert(MESSAGE_FAIL);
                return false;
            }
        }
        else{
            showAlert(MESSAGE_EXCEED_MAX_SIZE);
            return false;
        }
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle(MESSAGE_RESPONSE_TITLE)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public String getVideoPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        cursor.close();

        return path;
    }


}
