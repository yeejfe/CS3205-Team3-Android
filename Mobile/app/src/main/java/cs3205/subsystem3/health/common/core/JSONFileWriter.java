package cs3205.subsystem3.health.common.core;

import android.content.Context;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import cs3205.subsystem3.health.common.crypto.Encryption;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.utilities.Crypto;
import cs3205.subsystem3.health.common.utilities.CryptoException;
import cs3205.subsystem3.health.data.source.local.SessionDB;
import cs3205.subsystem3.health.model.Session;

/**
 * Created by Yee on 10/09/17.
 */

public class JSONFileWriter extends FileHelper {
    public static void createFile(String extDirPath, String fileName, JSONObject jsonObject) {
        String dirPath = extDirPath + FRONT_SLASH + FOLDER;
        File dir = new File(dirPath);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, fileName);

        if (!file.exists())
            toFile(null, file.getAbsolutePath(), jsonObject, null, false);
    }

    public static boolean toFile(Context context, String filePath, JSONObject jsonObject, Session session, boolean encrypt) {
        Writer output = null;
        File file = new File(filePath);
        try {
            output = new BufferedWriter(new FileWriter(file, false));
            String key = Encryption.getInstance().getKey();

            Log.d("JSONFileWriter", jsonObject.toString());

            if (key != null && encrypt == true && session != null && context != null) {
                byte[] encodedEncrypted = Encryption.getInstance().e(jsonObject.toString(), key);

                //generate hash
                String hash = null;
                try {
                    byte[] hashBytes = Crypto.generateHash(encodedEncrypted);
                    hash = new String(Base64.encode(hashBytes, Base64.NO_WRAP));
                } catch (CryptoException e) {
                    e.printStackTrace();
                }

                output.write(new String(encodedEncrypted));

                session.setLastModified(String.valueOf(file.lastModified()));
                session.setHash(hash);

                SessionDB db = new SessionDB(context);
                db.insertSession(session);
                db.close();
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
