package cs3205.subsystem3.health.common.utilities;

/**
 * Created by danwen on 21/10/17.
 */

public class JSONWebToken {
    private String data;

    public String getData() {return data;}

    public void setData(String data) {this.data = data;}

    private static final JSONWebToken token = new JSONWebToken();
    public static JSONWebToken getInstance() {return token;}
}
