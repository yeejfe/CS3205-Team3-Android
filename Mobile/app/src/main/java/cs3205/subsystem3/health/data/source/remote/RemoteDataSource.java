package cs3205.subsystem3.health.data.source.remote;

import java.io.InputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.logger.Log;

/**
 * Created by Yee on 09/30/17.
 */

public class RemoteDataSource {

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String X_NFC_TOKEN = "X-NFC-Token";


    public static final String SERVER3_ENDPOINT_STEPS = "https://cs3205-3.comp.nus.edu.sg/session/step";
    public static final String SERVER3_ENDPOINT_IMAGE = "https://cs3205-3.comp.nus.edu.sg/session/image";
    public static final String SERVER3_ENDPOINT_VIDEO = "https://cs3205-3.comp.nus.edu.sg/session/video";

    private static final String JSON = "application/json";

    private Client client;

    public RemoteDataSource() {
        client = ClientBuilder.newClient();
    }

    public Response buildFileUploadRequest(InputStream stream, String token, String hash, String choice) {

        long time = System.currentTimeMillis() / 1000;
        Invocation.Builder builder = null;

        if (choice.equals("steps")) {
            builder = client.target(SERVER3_ENDPOINT_STEPS).queryParam("timestamp", time)
                    .request();
        } else if (choice.equals("image")) {


            builder = client.target(SERVER3_ENDPOINT_IMAGE).queryParam("timestamp", time)
                    .request();

        } else {
            builder = client.target(SERVER3_ENDPOINT_VIDEO).queryParam("timestamp", time)
                    .request();
        }

        return builder
                .header(X_NFC_TOKEN, hash)
                .header(AUTHORIZATION, BEARER + token)
                .post(Entity.entity(stream, MediaType.APPLICATION_OCTET_STREAM));
    }

    public Response buildStepUploadRequest(InputStream stepsData, String token, String hash) {
        Log.i("Upload", "Upload");

        return buildFileUploadRequest(stepsData, token, hash, "steps");
    }

    public void close() {
        if (client != null)
            client.close();
    }
}
