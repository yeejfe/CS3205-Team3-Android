package cs3205.subsystem3.health.data.source.remote;

import java.io.InputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.core.Timestamp;
import cs3205.subsystem3.health.common.logger.Log;

/**
 * Created by Yee on 09/30/17.
 */

public class RemoteDataSource {
    public final static String SERVER3_UPLOAD_URL = "https://cs3205-3.comp.nus.edu.sg/upload/";
    public final static String SERVER3_SESSION_UPLOAD_URL = "https://cs3205-3.comp.nus.edu.sg/session/";

    public static final String TIMESTAMP = "?timestamp=";

    public static final String IMG_UPLOAD_URL = SERVER3_UPLOAD_URL + "/image";
    private final static String STEP_UPLOAD_URL = SERVER3_SESSION_UPLOAD_URL + "step" + TIMESTAMP;

    public static final String FRONT_SLASH = "/";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String X_NFC_TOKEN = "x-nfc-token";


    private static final String JSON = "application/json";

    private Client client;

    public RemoteDataSource() {
        client = ClientBuilder.newClient();
    }

    public Response buildFileUploadRequest(InputStream stream, String token, String hash) {
//        URL url = new URL(SERVER3_ADDRESS);
//        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//        conn.setRequestProperty("Authorization", "Bearer "+ token);
//        conn.setRequestProperty("x-nfc-token", hash);
//        OutputStream os = conn.getOutputStream();
//        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
        //   osw.write
        long time = Timestamp.getEpochTimeStamp();
        Invocation.Builder builder = client.target(IMG_UPLOAD_URL + FRONT_SLASH + time)
                // .queryParam() //type
                //  .queryParam() //epoch unixtime
                .request();
        return builder
                .header(X_NFC_TOKEN, hash)
                .header(AUTHORIZATION, BEARER + token)
                .post(Entity.entity(stream, JSON));
    }

    public Response buildStepUploadRequest(InputStream stepsData, String token, String hash) {
        Log.i("Upload", "Upload");
        Invocation.Builder builder = client.target(STEP_UPLOAD_URL + Timestamp.getEpochTimeStamp()).request();
        Response response = builder.header(AUTHORIZATION, BEARER + token).header(X_NFC_TOKEN, hash).post(
                        Entity.entity(stepsData, MediaType.APPLICATION_OCTET_STREAM));
        Log.i("Upload", response.toString());
        return response;
    }

    public void close() {
        if (client != null)
            client.close();
    }
}
