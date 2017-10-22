package cs3205.subsystem3.health.ui.camera;

import android.content.ContextWrapper;
import android.content.Intent;
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
import java.io.FileNotFoundException;
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

public class CameraActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_TAKE_VIDEO = 2;
    static final int REQUEST_TAKE_PHOTO_TEST = 3;


    public static final String CAMERA_DIR = "/dcim/";
    public static final String JPEG = "JPEG_";
    public static final String UNDERLINE = "_";
    public static final String JPG = ".jpg";
    public static final String AUTHORITIES = "com.example.panjiyun.healthcamera.fileprovider";

    private ImageView mImageView;
    private VideoView mVideoView;
    private Uri mVideoUri;
    private TextView mPathName;


    private String mCurrentImagePath;
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mImageView = (ImageView)findViewById(R.id.imageView1);
        mVideoView = (VideoView) findViewById(R.id.videoView1);
        mPathName = (TextView)findViewById(R.id.pathName);




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

    }




    /*
    * Function of taking pictures
    * */

    public void onClick_TakePhoto(View view){
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = createImageFile();
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            } catch (IOException e) {
                e.printStackTrace();
                imageFile = null;
                mCurrentImagePath = null;
            }
            if (imageFile != null) {
                // Uri photoURI = FileProvider.getUriForFile(this,AUTHORITIES,imageFile);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    public void onClick_TakePhotoForTest(View view){
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = wrapper.getDir("Health Internal Images", MODE_PRIVATE);
            imageFile = new File(imageFile, getUniqueName()+".jpg");
            OutputStream stream = null;
            try {
                stream = new FileOutputStream(imageFile);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                mCurrentImagePath = imageFile.getAbsolutePath();

            }catch(FileNotFoundException e){
                e.printStackTrace();
                imageFile = null;
                mCurrentImagePath = null;
                Log.d("Take Photo","File not found");
            }

            if (imageFile != null) {
                // Uri photoURI = FileProvider.getUriForFile(this,AUTHORITIES,imageFile);
                startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO_TEST);
            }

        }

    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG + timeStamp + UNDERLINE;
        File storageDir = getAlbumDir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                JPG,         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentImagePath = image.getAbsolutePath();
        return image;
    }

    private String getUniqueName(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        String imageFileName = JPEG + timeStamp;
        return imageFileName;
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getString(R.string.album_name));

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:{
                if (resultCode == RESULT_OK){
                    handleCameraPhoto();
                }
                break;
            }
            case REQUEST_TAKE_VIDEO:{
                if (resultCode == RESULT_OK) {
                    handleCameraVideo(data);
                }
                break;
            }
            case REQUEST_TAKE_PHOTO_TEST:{
                if (resultCode == RESULT_OK){
                    displayPathName();
                    simpleDisplayImage();
                }
                break;
            }


        }
    }

    private void handleCameraPhoto() {
        if (mCurrentImagePath != null) {
            setPic();
            displayPathName();
            galleryAddPic();
            mCurrentImagePath = null;
        }
    }

    private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentImagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentImagePath, bmOptions);

		/* Associate the Bitmap to the ImageView */
        mImageView.setImageBitmap(bitmap);

        mImageView.setVisibility(View.VISIBLE);
        mVideoView.setVisibility(View.INVISIBLE);
    }

    private void displayPathName(){
        mPathName.setText(mCurrentImagePath);
    }

    private void simpleDisplayImage(){
        File imgFile = new  File(mCurrentImagePath);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            mImageView = (ImageView) findViewById(R.id.imageView1);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(myBitmap);

        }
        else{
            Log.d("Take Photo","Image taken not found");
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentImagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }



    /*
    * Function of recording videos
    * */

    public void onClick_TakeVideo(View view){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_TAKE_VIDEO);
        }

    }

    private void handleCameraVideo(Intent intent) {
       /* mVideoUri = intent.getData();
        mVideoView.setVideoURI(mVideoUri);
        mVideoView.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.INVISIBLE);*/
    }

    public void onClick_GoToUploadPage(View view){
        Intent toUploadIntent = new Intent(this, UploadPageActivity.class);
        startActivity(toUploadIntent);
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


}
