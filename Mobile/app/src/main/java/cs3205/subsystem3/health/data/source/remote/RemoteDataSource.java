package cs3205.subsystem3.health.data.source.remote;

import android.util.Base64;

import java.io.InputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.common.miscellaneous.RequestInfo;
import cs3205.subsystem3.health.common.utilities.Crypto;
import cs3205.subsystem3.health.common.utilities.CryptoException;
import cs3205.subsystem3.health.common.utilities.JSONWebToken;

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

    public static final String SERVER3_ENDPOINT_STEPS = "https://cs3205-3.comp.nus.edu.sg/session/step";
    public static final String SERVER3_ENDPOINT_IMAGE = "https://cs3205-3.comp.nus.edu.sg/session/image";
    public static final String SERVER3_ENDPOINT_VIDEO = "https://cs3205-3.comp.nus.edu.sg/session/video";

    private Client client;
    private String TAG = this.getClass().getName();

    public RemoteDataSource() {
        client = ClientBuilder.newClient();
    }

    public Response buildFileUploadRequest(InputStream stream, String jwtToken, String nfcToken, Long time, Type type) throws CryptoException, RuntimeException {
        String serverEndPoint;

        if (type.equals(Type.STEPS)) {
            serverEndPoint = SERVER3_ENDPOINT_STEPS;
        } else if (type.equals(Type.IMAGE)) {
            serverEndPoint = SERVER3_ENDPOINT_IMAGE;
        } else {
            serverEndPoint = SERVER3_ENDPOINT_VIDEO;
        }

        Invocation.Builder challengeRequestBuilder = client.target(serverEndPoint).request();

        Response challengeResponse = challengeRequestBuilder.header(RequestInfo.HEADER_AUTHORIZATION, RequestInfo.JWT_TOKEN_PREFIX + jwtToken).post(null);
        byte[] nfcChallenge = null;
        if (challengeResponse.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
            nfcChallenge = Base64.decode(challengeResponse.getHeaderString(RequestInfo.HEADER_NFC_CHALLENGE), Base64.NO_WRAP);
            if (nfcChallenge == null) {
                return null;
            }
        } else {
            return null;
        }

        Invocation.Builder uploadRequestBuilder = client.target(serverEndPoint).queryParam(TIMESTAMP, time).request();

        Response uploadResponse = uploadRequestBuilder
                .header(RequestInfo.HEADER_AUTHORIZATION, RequestInfo.JWT_TOKEN_PREFIX + JSONWebToken.getInstance().getData())
                .header(RequestInfo.HEADER_NFC_RESPONSE, Base64.encodeToString(Crypto.generateNfcResponse(nfcToken,
                        nfcChallenge), Base64.NO_WRAP))
                .post(Entity.entity(stream, MediaType.APPLICATION_OCTET_STREAM));

        if (uploadResponse != null && uploadResponse.getStatus() == Response.Status.CREATED.getStatusCode()) {
            String newJwToken = uploadResponse.getHeaderString(RequestInfo.HEADER_REFRESHED_JWT);
            JSONWebToken.getInstance().setData(newJwToken);
        }

        return uploadResponse;
    }

    public void close() {
        if (client != null)
            client.close();
    }
}
