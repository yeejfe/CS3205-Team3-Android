package cs3205.subsystem3.health.ui.camera;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.common.utilities.LogoutHelper;
import cs3205.subsystem3.health.common.utilities.SessionManager;
import cs3205.subsystem3.health.logic.camera.AlbumStorageDirFactory;
import cs3205.subsystem3.health.logic.camera.BaseAlbumDirFactory;
import cs3205.subsystem3.health.logic.camera.FroyoAlbumDirFactory;
import cs3205.subsystem3.health.logic.camera.MetaInfoExtractor;


/**
 * Created by panjiyun.
 */

public class CameraActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_TAKE_VIDEO = 2;
    static final int REQUEST_DELETE = 3;


    public static final String JPEG = "JPEG_";
    public static final String UNDERLINE = "_";
    public static final String JPG = ".jpg";
    public static final String DATE_FORMAT = "yyyyMMdd_HHmmssSSS";


    public static final String FAIL_MESSAGE_1 = "failed to create directory";
    public static final String FAIL_MESSAGE_2 = "External storage is not mounted READ/WRITE.";
    public static final String GALLERY_REQUEST_TYPE = "gallery_request_type";

    public static final String MEDIA_SCANNER = "android.intent.action.MEDIA_SCANNER_SCAN_FILE";
    public static final String VID = "VID_";
    public static final String MP4 = ".mp4";

    public static final String DISPLAY_MESSAGE_VIDEO_RECORDED = "Video recorded:";
    public static final String DISPLAY_MESSAGE_PHOTO_DELETED = "Photo deleted:";
    public static final String DISPLAY_MESSAGE_PICTURE_TAKEN = "Picture taken:\n";

    private ImageView mImageView;
    private VideoView mVideoView;
    private TextView mPathName;


    private String mCurrentImagePathExternal;
    private String mCurrentImagePathInternal;
    private String mCurrentVideoPath;
    private String mDeletedImagePath;

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;


    public enum GalleryRequestType {
        UPLOAD,
        DELETE
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_camera);

        mImageView = (ImageView) findViewById(R.id.imageView1);
        mVideoView = (VideoView) findViewById(R.id.videoView1);
        mPathName = (TextView) findViewById(R.id.pathName);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

    }




    /*
    * Function of taking pictures
    * */

    public void onClick_TakePhoto(View view) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            File imageFileExternal = null;

            try {
                imageFileExternal = createImageFile();

            } catch (IOException e) {
                e.printStackTrace();
                imageFileExternal = null;
                mCurrentImagePathExternal = null;
                mCurrentImagePathInternal = null;
            }
            if (imageFileExternal != null) {
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFileExternal));
                startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


      /*
    * Function of recording videos
    * */


    public void onClick_TakeVideo(View view) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            File videoFile = null;

            try {
                videoFile = createVideoFile();

            } catch (IOException e) {
                e.printStackTrace();
                videoFile = null;
                mCurrentVideoPath = null;

            }
            if (videoFile != null) {
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
                startActivityForResult(takeVideoIntent, REQUEST_TAKE_VIDEO);
            }
        }

    }



        /*
    * Navigate to upload activity
    * */

    public void onClick_GoToUploadPage(View view) {
        Intent toUploadIntent = new Intent(this, UploadPageActivity.class);
        startActivity(toUploadIntent);
    }

     /*
    * Navigate to delete activity
    * */

    public void onClick_GoToCustomGallery(View view) {

        Intent toDeleteIntent = new Intent(this, CustomGallery.class);
        toDeleteIntent.putExtra(GALLERY_REQUEST_TYPE, GalleryRequestType.DELETE);
        startActivityForResult(toDeleteIntent, REQUEST_DELETE);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    try {
                        handleCameraPhoto();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case REQUEST_TAKE_VIDEO: {
                if (resultCode == RESULT_OK) {
                    handleCameraVideo();
                }
                break;
            }
            case REQUEST_DELETE: {
                try {
                    if (resultCode == RESULT_OK) {
                        handleReturnInfo(data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }


        }
    }


    private File createInternalImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String imageFileName = JPEG + timeStamp + UNDERLINE;

        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File storageDir = wrapper.getDir("Health", MODE_PRIVATE);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                JPG,         /* suffix */
                storageDir      /* directory */
        );

        mCurrentImagePathInternal = image.getAbsolutePath();
        return image;
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String imageFileName = JPEG + timeStamp + UNDERLINE;
        File storageDir = getAlbumDir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                JPG,         /* suffix */
                storageDir      /* directory */
        );

        mCurrentImagePathExternal = image.getAbsolutePath();
        return image;
    }

    private File createVideoFile() throws IOException {
        // Create   file name
        String timeStamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String imageFileName = VID + timeStamp + UNDERLINE;
        File storageDir = getAlbumDir();
        File video = File.createTempFile(
                imageFileName,  /* prefix */
                MP4,         /* suffix */
                storageDir      /* directory */
        );

        mCurrentVideoPath = video.getAbsolutePath();
        return video;
    }


    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getString(R.string.album_name));

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d(this.getClass().getSimpleName(), FAIL_MESSAGE_1);
                        return null;
                    }
                }
            }

        } else {
            Log.v(this.getClass().getSimpleName(), FAIL_MESSAGE_2);
        }

        return storageDir;
    }


    private void handleCameraPhoto() throws IOException {
        if (mCurrentImagePathExternal != null) {
            writeFromExternalToInternal();
            deleteExternalFile();
            setPic();
            displayPathName();
            mCurrentImagePathExternal = null;
        }
    }


    private void writeFromExternalToInternal() throws IOException {
        OutputStream fOut = null;
        File imageFileInternal = null;

        imageFileInternal = createInternalImageFile();
        imageFileInternal.createNewFile();
        fOut = new FileOutputStream(imageFileInternal);
        Bitmap image = BitmapFactory.decodeFile(mCurrentImagePathExternal);
        if (image != null) {
            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
        }
        fOut.close();
    }

    private void setPic() {

        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentImagePathInternal, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;


        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;


        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentImagePathInternal, bmOptions);
        System.out.println("mCurrentImagePathInternal: " + mCurrentImagePathInternal);


        mImageView.setImageBitmap(bitmap);
        mImageView.setVisibility(View.VISIBLE);
        mVideoView.setVisibility(View.INVISIBLE);
    }


    private void displayPathName() {
        mPathName.setText(DISPLAY_MESSAGE_PICTURE_TAKEN + MetaInfoExtractor.getFileName(mCurrentImagePathInternal));
    }


    private void deleteExternalFile() {
        if (mCurrentImagePathExternal != null) {
            File file = new File(mCurrentImagePathExternal);
            if (file.exists()) {
                file.delete();
            }
        }
    }


    private void handleCameraVideo() {
        galleryAddVid();
      //  Uri videoUri = intent.getData();
      //  mCurrentVideoPath = PathExtractor.getPath(this, videoUri);
        mPathName.setText("\n" + DISPLAY_MESSAGE_VIDEO_RECORDED + "\n" + MetaInfoExtractor.getFileName(mCurrentVideoPath));
        mImageView.setVisibility(View.INVISIBLE);

    }

    private void galleryAddVid() {
        Intent mediaScanIntent = new Intent(MEDIA_SCANNER);
        File f = new File(mCurrentVideoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void handleReturnInfo(Intent intent) {
        mDeletedImagePath = intent.getStringExtra(CustomGallery.SELECTED_IMAGE_PATH);
        mPathName.setText("\n" + DISPLAY_MESSAGE_PHOTO_DELETED + "\n" + MetaInfoExtractor.getFileName(mDeletedImagePath));
        mImageView.setVisibility(View.INVISIBLE);

    }


    /*
    *   Log out
    * */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                LogoutHelper.logout(this, AppMessage.TOAST_MESSAGE_LOGOUT_SUCCESS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    *   Session management
    * */


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

    


}
