package cs3205.subsystem3.health.common.core;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import cs3205.subsystem3.health.BuildConfig;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.logger.Tag;

/**
 * Created by Yee on 10/09/17.
 */

public class JSONFileWriter extends JSONFile {
    public static final String FOLDER = "/steps";
    public static final String FRONT_SLASH = "/";

    public static void createFile(String extDirPath, String fileName, JSONObject jsonObject) {
        String dirPath = extDirPath + FRONT_SLASH + FOLDER;
        File dir = new File(dirPath);
        Log.d("Hello", dir.getAbsolutePath());
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, fileName);

        if (!file.exists())
            toFile(file.getAbsolutePath(), jsonObject);
    }

    public static boolean toFile(String filePath, JSONObject jsonObject) {
        Writer output = null;
        File file = new File(filePath);
        try {
            output = new BufferedWriter(new FileWriter(file, false));
            if (BuildConfig.DEBUG)
                Log.i(Tag.STEP_SENSOR, jsonObject.toString());
            output.write(jsonObject.toString());
            output.close();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private static void saveToFile(String extDirPath, int steps) {
        String dirPath = extDirPath + FOLDER;
        File dir = new File(dirPath);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, String.valueOf(Timestamp.getEpochTimeStamp()));

        try {
            FileOutputStream os;

            if (!file.exists()) {
                os = new FileOutputStream(file);
            } else {
                os = new FileOutputStream(file, true);
            }
            PrintWriter pw = new PrintWriter(os);
            pw.print(Timestamp.getEpochTimeStamp() + ",");
            pw.println(steps);
            pw.flush();
            pw.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("File Permission", "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
