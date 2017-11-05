package cs3205.subsystem3.health.logic.camera;

import android.os.Environment;

import java.io.File;

/**
 * Created by panjiyun.
 */

public final class FroyoAlbumDirFactory extends AlbumStorageDirFactory {


    @Override
    public File getAlbumStorageDir(String albumName) {

        File folder = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                albumName
        );

        return folder;
    }
}
