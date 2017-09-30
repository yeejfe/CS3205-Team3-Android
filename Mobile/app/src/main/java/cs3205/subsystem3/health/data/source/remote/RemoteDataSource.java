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
    public static final String SERVER3_ADDRESS = "https://cs3205-3.comp.nus.edu.sg/upload";
    private static final String JSON = "application/json";

    private Client client;

    public RemoteDataSource() {
        client = ClientBuilder.newClient();
    }

    public Response buildFileUploadRequest(InputStream stream){
        Invocation.Builder builder = client.target(SERVER3_ADDRESS)
                // .queryParam() //type
                //  .queryParam() //epoch unixtime
                .request();
        return  builder.post(Entity.entity(stream, JSON));
    }

    public void close(){
        if (client != null)
            client.close();
    }
}
