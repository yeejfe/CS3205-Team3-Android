package cs3205.subsystem3.health.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;

/**
 * Created by panjiyun on 5/10/17.
 */

public class Uploader {

    String path;

    public Uploader(String path) {
        this.path = path;
    }



    public boolean upload() {

        File f = new File(path);
        long length = f.length() / (1024 * 1024);  // length is expressed in MB
        if (length < 10.00) {
            InputStream stream = null;
            try {
                stream = new FileInputStream(f);
            } catch (Exception e) {
                e.printStackTrace();
            }

            RemoteDataSource rDS = new RemoteDataSource();
            Response response = rDS.buildFileUploadRequest(stream);
            rDS.close();
            // Check Response

            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {

                return true;

            } else {

                return false;
            }
        } else {

            return false;
        }



    }

}
