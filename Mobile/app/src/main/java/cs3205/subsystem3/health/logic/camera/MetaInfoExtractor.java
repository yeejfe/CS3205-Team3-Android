package cs3205.subsystem3.health.logic.camera;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by panjiyun.
 */

public final class MetaInfoExtractor {

    public static long getEpochTimeStamp(String path) {
        String str = extractTimeString(path);
        long epoch = 0;

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
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

    private static String extractTimeString(String path) {
        String[] arr = path.split("/");
        String[] arr2 = arr[arr.length - 1].split("_");
        String str = arr2[1] + "_" + arr2[2];
        return str;
    }


}
