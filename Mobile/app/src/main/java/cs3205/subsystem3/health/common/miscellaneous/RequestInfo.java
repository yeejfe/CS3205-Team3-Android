package cs3205.subsystem3.health.common.miscellaneous;

/**
 * Created by danwen on 11/10/17.
 */

public class  RequestInfo {
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String CHALLENGE_RESPONSE_PREFIX = "Basic ";
    public static final String HEADER_NFC_RESPONSE = "X-NFC-Response";
    public static final String HEADER_NFC_CHALLENGE = "X-NFC-Challenge";
    public static final String HEADER_AUTHENTICATE = "WWW-Authenticate";
    public static final String HEADER_REFRESHED_JWT = "Set-Authorization";
    public static final String HEADER_GRANT_TYPE = "grant_type";
    public static final String HEADER_USERNAME = "username";
    public static final String HEADER_TIMEOUT = "X-Timeout";
    public static final String GRANT_TYPE_PASSWORD = "PASSWORD";
    public static final String URL_HEART_RATE_UPLOAD = "https://cs3205-3.comp.nus.edu.sg/session/heart";
    public static final String URL_LOGIN = "https://cs3205-3.comp.nus.edu.sg/oauth/token";
    public static final String QUERY_PARAMETER_TIMESTAMP = "timestamp";

}
