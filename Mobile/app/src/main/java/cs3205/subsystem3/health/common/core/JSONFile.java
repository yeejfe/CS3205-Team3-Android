package cs3205.subsystem3.health.common.core;

import java.io.File;

/**
 * Created by Yee on 10/09/17.
 */

public class JSONFile {
    public static boolean fileExist(String filePath){
        return (new File(filePath)).exists();
    }
}
