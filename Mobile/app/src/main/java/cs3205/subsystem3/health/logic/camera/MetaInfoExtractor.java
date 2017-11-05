package cs3205.subsystem3.health.logic.camera;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by panjiyun.
 */

public final class MetaInfoExtractor {

    public static final String TOAST_MESSAGE_FILE_NOT_FROM_APP = "File not from Health App!";
    public static final String DATE_FORMAT = "yyyyMMdd_HHmmssSSS";

    public static long getEpochTimeStamp(Context ctx, String path) {
        String str = extractTimeString(ctx, path);
        long epoch = 0;

        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        try {
            Date date = df.parse(str);
            epoch = date.getTime();
            System.out.println(epoch);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("MetaInfoExtractor", "error in parse date");
        }
        return epoch;
    }

    public static String getFileName(String path) {
        String[] arr = path.split("/");
        String fileName = arr[arr.length - 1];

        return fileName;
    }

    private static String extractTimeString(Context ctx, String path) {
        String str = "";
        try {
            String[] arr = path.split("/");
            String[] arr2 = arr[arr.length - 1].split("_");
            str = arr2[1] + "_" + arr2[2];
            if(arr2.length!=4||!(arr2[0].equals("VID")||arr2[0].equals("JPEG"))){
                throw new IndexOutOfBoundsException();
            }
        }catch(IndexOutOfBoundsException e){
            Log.d("MetaInfoExtractor", "wrong format of file name");
            System.out.println("Wrong format of file name.");
            Toast.makeText(ctx, TOAST_MESSAGE_FILE_NOT_FROM_APP, Toast.LENGTH_SHORT).show();

        }
        return str;
    }

    public static String getExtension(String path){
        String[] arr = getFileName(path).split("\\.");
        return arr[arr.length-1];
    }


}
