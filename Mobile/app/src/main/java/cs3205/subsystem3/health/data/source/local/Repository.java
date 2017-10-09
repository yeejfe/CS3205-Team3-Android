package cs3205.subsystem3.health.data.source.local;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cs3205.subsystem3.health.common.core.JSONFileReader;
import cs3205.subsystem3.health.common.core.JSONFileWriter;
import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.common.utilities.JSONUtil;
import cs3205.subsystem3.health.model.Steps;

import static cs3205.subsystem3.health.common.core.JSONFileWriter.FOLDER;
import static cs3205.subsystem3.health.common.core.JSONFileWriter.FRONT_SLASH;

/**
 * For local repository
 * Created by Yee on 10/08/17.
 */

public class Repository {
    public static Steps getFile(String dirPath, String fileName) {
        File file = new File(dirPath, fileName);
        Steps data = new Steps(0);

        if (file.exists()) {
            try {
                JSONObject stepsJSON = JSONFileReader.toJSONObj(file.getAbsolutePath());
                return JSONUtil.JSONtoSteps(stepsJSON);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            JSONFileWriter.createFile(dirPath, fileName, JSONUtil.stepsDataToJSON(data));
        }

        return data;
    }

    public static void writeFile(String dirPath, String fileName, Steps data) {
        dirPath += (FRONT_SLASH + FOLDER);

        String filePath = dirPath + FRONT_SLASH + fileName;

        if(data.getTimestamp() == 0)
            data.setTimestamp(Timestamp.getEpochTimeStamp());

        JSONObject jsonObject = JSONUtil.stepsDataToJSON(data);

        JSONFileWriter.toFile(filePath, jsonObject);
    }

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
