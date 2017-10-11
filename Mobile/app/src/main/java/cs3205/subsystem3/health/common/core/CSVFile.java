package cs3205.subsystem3.health.common.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import cs3205.subsystem3.health.common.logger.Log;

/**
 * Created by Yee on 10/11/17.
 */

public class CSVFile extends FileHelper {
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
