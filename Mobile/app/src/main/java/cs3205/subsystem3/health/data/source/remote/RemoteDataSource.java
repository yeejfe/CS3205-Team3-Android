package cs3205.subsystem3.health.data.source.remote;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.utilities.Crypto;

/**
 * Created by Yee on 09/30/17.
 */

public class RemoteDataSource {

    public static final String TIMESTAMP = "timestamp";

    public enum Type {
        STEPS,
        IMAGE,
        VIDEO
    }

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String X_NFC_TOKEN = "X-NFC-Token";


    public static final String SERVER3_ENDPOINT_STEPS = "https://cs3205-3.comp.nus.edu.sg/session/step";
    public static final String SERVER3_ENDPOINT_IMAGE = "https://cs3205-3.comp.nus.edu.sg/session/image";
    public static final String SERVER3_ENDPOINT_VIDEO = "https://cs3205-3.comp.nus.edu.sg/session/video";

    private Client client;

    public RemoteDataSource() {
        client = ClientBuilder.newClient();
    }

    public Response buildFileUploadRequest(InputStream stream, String jwtToken, String nfcToken, Long time, Type type) throws NoSuchAlgorithmException, InvalidKeyException {
        Invocation.Builder builder = null;

        if (type.equals(Type.STEPS)) {
            builder = client.target(SERVER3_ENDPOINT_STEPS).queryParam(TIMESTAMP, time)
                    .request();
        } else if (type.equals(Type.IMAGE)) {
            builder = client.target(SERVER3_ENDPOINT_IMAGE).queryParam(TIMESTAMP, time)
                    .request();
        } else {
            builder = client.target(SERVER3_ENDPOINT_VIDEO).queryParam(TIMESTAMP, time)
                    .request();
        }

       return builder
               .header(X_NFC_TOKEN,Crypto.generateNfcAuthToken(nfcToken.getBytes()))
               .header(AUTHORIZATION, BEARER + jwtToken)
               .post(Entity.entity(stream, MediaType.APPLICATION_OCTET_STREAM));
    }

    public void close() {
        if (client != null)
            client.close();
    }
}
