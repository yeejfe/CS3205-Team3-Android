package cs3205.subsystem3.health.ui.camera;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import cs3205.subsystem3.health.R;

/**
 * Created by panjiyun.
 */

public class CustomGallery extends AppCompatActivity {

    public static final String SELECTED_IMAGE_PATH = "selected_image_path";
    private ImageAdapter imageAdapter;
    private TextView textForNumImages;
    private CameraActivity.GalleryRequestType requestType;

    ArrayList<String> f = new ArrayList<String>();// list of file paths
    File[] listFile;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        requestType = (CameraActivity.GalleryRequestType) intent.getSerializableExtra(CameraActivity.GALLERY_REQUEST_TYPE);

        setContentView(R.layout.activity_custom_gallery);
        getFromSdcard();
        textForNumImages = (TextView) findViewById(R.id.TextForNumImages);
        textForNumImages.setText("Number of pictures: " + f.size());

        GridView imagegrid = (GridView) findViewById(R.id.ImageGrid);
        imageAdapter = new ImageAdapter();
        imagegrid.setAdapter(imageAdapter);


        imagegrid.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(CustomGallery.this, "image " + (String.valueOf(position + 1)) + " is selected! ", Toast.LENGTH_SHORT).show();

                if (requestType.equals(CameraActivity.GalleryRequestType.UPLOAD)) {
                    Intent intent = new Intent();
                    intent.putExtra(SELECTED_IMAGE_PATH, listFile[position].getPath().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    File fdelete = new File(listFile[position].getPath());
                    Intent intent = new Intent();

                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            Toast.makeText(CustomGallery.this, "image " + (String.valueOf(position + 1)) + " is deleted! ", Toast.LENGTH_SHORT).show();
                            intent.putExtra(SELECTED_IMAGE_PATH, listFile[position].getPath().toString());
                            setResult(RESULT_OK, intent);

                        } else {
                            Toast.makeText(CustomGallery.this, "fail to delete image " + (String.valueOf(position + 1)) + "! ", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_CANCELED, intent);
                        }
                    } else {
                        Toast.makeText(CustomGallery.this, "image " + (String.valueOf(position + 1)) + " does not exist! ", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED, intent);
                    }

                    finish();
                }

            }

        });


    }

    public void getFromSdcard() {

        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("Health", MODE_PRIVATE);

        System.out.println(" folder path  " + file.getPath());

        if (file.isDirectory()) {
            listFile = file.listFiles();
            for (int i = 0; i < listFile.length; i++) {
                System.out.println(" name  " + listFile[i].getPath());
                f.add(listFile[i].getAbsolutePath());
            }
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return f.size();
        }

        public Object getItem(int position) {
            return f.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(
                        R.layout.galleryitem, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ImageGridHandler handler = new ImageGridHandler(getBaseContext(), holder.imageview, position);
            handler.execute();
            return convertView;
        }
    }

    class ViewHolder {
        ImageView imageview;


    }


    public class ImageGridHandler extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private Context context;
        private int position;

        public ImageGridHandler(Context context, ImageView img, int position) {
            imageViewReference = new WeakReference<ImageView>(img);
            this.context = context;
            this.position = position;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            File file = new File(f.get(position));
            if (file.exists()) {
                return BitmapFactory.decodeFile(f.get(position));
            } else {
                return null;
            }

        }

        @Override
        protected void onPostExecute(Bitmap result) {
            final ImageView imageView = imageViewReference.get();
            if (result != null) {
                try {
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(result, 500, 500, false));
                } catch (NullPointerException e) {
                    return;
                }
            }
        }
    }

}