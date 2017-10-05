package cs3205.subsystem3.health.data.source.remote;

import java.io.InputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

/**
 * Created by Yee on 09/30/17.
 */

public class RemoteDataSource {
    public static final String SERVER3_ADDRESS = "https://cs3205-3.comp.nus.edu.sg/upload/image";
    private static final String JSON = "application/json";

    private Client client;

    public RemoteDataSource() {
        client = ClientBuilder.newClient();
    }

    public Response buildFileUploadRequest(InputStream stream, String token, String hash){
//        URL url = new URL(SERVER3_ADDRESS);
//        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//        conn.setRequestProperty("Authorization", "Bearer "+ token);
//        conn.setRequestProperty("x-nfc-token", hash);
//        OutputStream os = conn.getOutputStream();
//        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
     //   osw.write
        long time = System.currentTimeMillis()/1000;
        Invocation.Builder builder = client.target(SERVER3_ADDRESS+"/"+time)
                // .queryParam() //type
                //  .queryParam() //epoch unixtime
                .request();
        return  builder
                .header("x-nfc-token", hash)
                .header("Authorization", "Bearer "+token)
                .post(Entity.entity(stream, JSON));
    }

    public void close(){
        if (client != null)
            client.close();
    }
}
