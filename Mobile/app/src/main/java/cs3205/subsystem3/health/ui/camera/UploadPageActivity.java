package cs3205.subsystem3.health.ui.camera;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.Value;
import cs3205.subsystem3.health.common.utilities.JSONWebToken;
import cs3205.subsystem3.health.common.utilities.SessionManager;
import cs3205.subsystem3.health.common.utilities.UploadHandler;
import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;
import cs3205.subsystem3.health.ui.nfc.NFCReaderActivity;

import static cs3205.subsystem3.health.common.utilities.UploadHandler.MESSAGE_NFC_READ_FAIL;


public class UploadPageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_LOAD_IMAGE = 1;
    private static final int REQUEST_LOAD_VIDEO = 2;
    public static final int REQUEST_READ_NFC = 70;

    public static final String CONTENT_DOWNLOADS_PUBLIC_DOWNLOADS = "content://downloads/public_downloads";
    public static final String EXTERNAL_STORAGE = "com.android.externalstorage.documents";
    public static final String PROVIDERS_DOWNLOADS = "com.android.providers.downloads.documents";
    public static final String PROVIDERS_MEDIA = "com.android.providers.media.documents";


    private ImageView imageToUpload, videoToUpload;
    private VideoView videoToPreview;
    private Button bUploadImage, bUploadVideo;
    private TextView uploadImageName, uploadVideoName;

    private String selectedPath = null;

    private String nfcToken;
    private String jwtToken;
    private RemoteDataSource.Type choice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_page);

        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);
        bUploadImage = (Button) findViewById(R.id.buttonUploadImage);
        uploadImageName = (TextView) findViewById(R.id.imageName);

        videoToUpload = (ImageView) findViewById(R.id.videoToUpload);
        bUploadVideo = (Button) findViewById(R.id.buttonUploadVideo);
        uploadVideoName = (TextView) findViewById(R.id.videoName);
        videoToPreview = (VideoView) findViewById(R.id.videoToPreview);

        imageToUpload.setOnClickListener(this);
        videoToUpload.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);
        bUploadVideo.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionManager.isTimerSet()) {
            SessionManager.cancelTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SessionManager.isTimerSet()) {
            SessionManager.resetTimer(this);
        } else {
            SessionManager.setTimer(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageToUpload:
                Intent imageGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imageGalleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(imageGalleryIntent,"Select Picture"), REQUEST_LOAD_IMAGE);
                break;
            case R.id.buttonUploadImage:
                choice = RemoteDataSource.Type.IMAGE;
                if(selectedPath==null){
                    break;
                }else {
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
                if(selectedPath==null){
                    break;
                }else {
                    getJwtToken();
                    getNfcToken();
                    break;
                }


        }

    }
    private void getJwtToken(){
        jwtToken = "";
        jwtToken =  JSONWebToken.getInstance().getData();
        Log.d(this.getClass().getSimpleName() , jwtToken);
    }


    private void getNfcToken(){
        Intent startNFCReadingActivity = new Intent(this, NFCReaderActivity.class);
        startActivityForResult(startNFCReadingActivity, REQUEST_READ_NFC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            selectedPath = getPath(this, selectedImageUri);
            uploadImageName.setText(selectedPath);
            imageToUpload.setImageURI(selectedImageUri);


        } else if (requestCode == REQUEST_LOAD_VIDEO && resultCode == RESULT_OK && data != null) {
            Uri selectedVideoUri = data.getData();
            selectedPath = getPath(this, selectedVideoUri);
            uploadVideoName.setText(selectedPath);
            videoToUpload.setVisibility(View.GONE);
            videoToPreview.setVideoURI(selectedVideoUri);
            videoToPreview.setVisibility(View.VISIBLE);

        } else if (requestCode == REQUEST_READ_NFC) {
                if (resultCode == RESULT_OK) {
                    nfcToken = data.getStringExtra(Value.KEY_VALUE_LOGIN_INTENT_PASSWORD);
                    Log.d(this.getClass().getSimpleName() , "NFC token is "+nfcToken);
                    UploadHandler uploadHander = new UploadHandler(selectedPath, choice, this , jwtToken, nfcToken);
                    uploadHander.startUpload();
                }
        }

    }

    private void failNfcRead(){
        Toast.makeText(this, MESSAGE_NFC_READ_FAIL , Toast.LENGTH_SHORT).show();
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


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse(CONTENT_DOWNLOADS_PUBLIC_DOWNLOADS), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return EXTERNAL_STORAGE.equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return PROVIDERS_DOWNLOADS.equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return PROVIDERS_MEDIA.equals(uri.getAuthority());
    }

}
