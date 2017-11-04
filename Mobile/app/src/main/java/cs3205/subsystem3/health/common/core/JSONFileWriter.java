package cs3205.subsystem3.health.common.core;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import cs3205.subsystem3.health.common.crypto.Encryption;
import cs3205.subsystem3.health.common.logger.Log;

/**
 * Created by Yee on 10/09/17.
 */

public class JSONFileWriter extends FileHelper {
    private static String TAG = "JSONFileWriter";

    public static void createFile(String extDirPath, String fileName, JSONObject jsonObject) {
        String dirPath = extDirPath + FRONT_SLASH + FOLDER;
        File dir = new File(dirPath);
        Log.d(TAG, dir.getAbsolutePath());
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
            String key = Encryption.getInstance().getKey();
            if (key != null) {
                output.write(new String(Encryption.getInstance().e(jsonObject.toString(), key)));
            } else {
                output.write(jsonObject.toString());
            }
            output.close();
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
