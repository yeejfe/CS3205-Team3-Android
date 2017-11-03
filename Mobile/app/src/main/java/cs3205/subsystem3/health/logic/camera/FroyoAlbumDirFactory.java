package cs3205.subsystem3.health.logic.camera;

import android.os.Environment;

import java.io.File;

public final class FroyoAlbumDirFactory extends AlbumStorageDirFactory {

    String NOMEDIA=".nomedia";

	@Override
	public File getAlbumStorageDir(String albumName) {
		// TODO Auto-generated method stub
		File folder = new File (
		  Environment.getExternalStoragePublicDirectory(
		    Environment.DIRECTORY_PICTURES
		  ), 
		  albumName
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
