package cs3205.subsystem3.health.common.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Yee on 10/09/17.
 */

public class JSONFileReader extends FileHelper {
    public static JSONObject toJSONObj(String filePath) throws IOException {
        JSONObject newJSONObj = new JSONObject();
        try {
            newJSONObj = new JSONObject(toString(filePath));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newJSONObj;
    }

    public static String toString(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        BufferedReader buf = new BufferedReader(new InputStreamReader(input));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();
        while (line != null) {
            sb.append(line).append("\n");
            line = buf.readLine();
        }
        String fileAsString = sb.toString();

        //String contents = new String(Files.readAllBytes(Paths.get("manifest.mf")));
        //String fileString = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);

        return fileAsString;
    }
}
