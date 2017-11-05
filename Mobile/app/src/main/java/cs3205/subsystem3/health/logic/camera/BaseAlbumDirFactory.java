package cs3205.subsystem3.health.logic.camera;

import android.os.Environment;

import java.io.File;

/**
 * Created by panjiyun.
 */

public final class BaseAlbumDirFactory extends AlbumStorageDirFactory {

    // Standard storage location for digital camera files
    private static final String CAMERA_DIR = "/dcim/";


    @Override
    public File getAlbumStorageDir(String albumName) {
        File folder = new File(
                Environment.getExternalStorageDirectory()
                        + CAMERA_DIR
                        + albumName
        );

        return folder;
    }
}
