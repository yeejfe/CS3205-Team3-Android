package cs3205.subsystem3.health.common.miscellaneous;

/**
 * Created by danwen on 11/10/17.
 */

public class RequestInfo {
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_NFC_TOKEN_HASH = "x-nfc-token";
    public static final String HEADER_GRANT_TYPE = "grant_type";
    public static final String HEADER_USERNAME = "username";
    public static final String GRANT_TYPE_PASSWORD = "password";
    public static final String HEADER_PASSWORD_HASH = "passhash";
    public static final String URL_HEART_RATE_UPLOAD = "https://cs3205-3.comp.nus.edu.sg/session/heart";
    public static final String URL_LOGIN = "https://cs3205-3.comp.nus.edu.sg/oauth/token";
    public static final String QUERY_PARAMETER_TIMESTAMP = "timestamp";

}
