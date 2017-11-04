package cs3205.subsystem3.health.logic.camera;

import java.io.File;

/**
 * Created by panjiyun.
 */


public abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}
