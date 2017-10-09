package cs3205.subsystem3.health.data.source.local;

import java.io.File;
import java.util.ArrayList;

/**
 * For local repository
 * Created by Yee on 10/08/17.
 */

public class Repository {
    public static ArrayList<String> getFiles(String dirPath) {
        ArrayList<String> arrayListOfFiles = new ArrayList<String>();
        File f = new File(dirPath);
        f.mkdirs();
        File[] listFiles = f.listFiles();
        if (listFiles.length == 0) {
            return new ArrayList<String>();
        } else {
            for (int i = 0; i < listFiles.length; i++)
                arrayListOfFiles.add(listFiles[i].getAbsolutePath());
        }
        return arrayListOfFiles;
    }
}
