package cs3205.subsystem3.health.logic.camera;

import android.os.Environment;

import java.io.File;

public final class BaseAlbumDirFactory extends AlbumStorageDirFactory {

	// Standard storage location for digital camera files
	private static final String CAMERA_DIR = "/dcim/";
	String NOMEDIA=".nomedia";

	@Override
	public File getAlbumStorageDir(String albumName) {
		File folder = new File (
				Environment.getExternalStorageDirectory()
				+ CAMERA_DIR
				+ albumName
		);
/*		if(folder.mkdir()) {
			try {
                File nomediaFile = new File(Environment.getExternalStorageDirectory() +"/"+ albumName +"/"+ NOMEDIA);
				if (!nomediaFile.exists()) {
					nomediaFile.createNewFile();
				}
			}catch(IOException e){
				System.out.println("Error in creating nomediaFile");
			}
		}*/
		return folder;
	}
}
