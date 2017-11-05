package cs3205.subsystem3.health.model;

import android.util.Base64;

import cs3205.subsystem3.health.BuildConfig;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.utilities.Crypto;
import cs3205.subsystem3.health.common.utilities.CryptoException;

/**
 * Created by Yee on 09/18/17.
 */

public class Session {
    private String TAG = this.getClass().getName();

    private String title;
    private String filename;
    private String hash;
    private String lastModified;

    public Session(String title, String filename, String hash, String lastModified){
        this.title = title;
        this.filename = filename;
        this.hash = hash;
        this.lastModified = lastModified;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public boolean checkHashAndModified(byte[] contents, String lastModified){
        boolean equals = false;
        try {
            byte[] hashBytes = Crypto.generateHash(contents);
            String hash = new String(Base64.encode(hashBytes, Base64.NO_WRAP));

            if(this.getHash().equals(hash)) {
                equals = true;
            }
        } catch (CryptoException e) {
            e.printStackTrace();
        }

        if(BuildConfig.DEBUG)
            Log.d(TAG, String.valueOf(equals));

        return equals;
    }
}
