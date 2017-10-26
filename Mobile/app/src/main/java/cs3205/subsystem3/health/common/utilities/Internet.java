package cs3205.subsystem3.health.common.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import cs3205.subsystem3.health.common.logger.Log;


/**
 * Created by danwen on 26/10/17.
 */

public class Internet {

    public static boolean isConnected(Context context) {

        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        Log.d("Internet", "is wifi connected: " + isWifiConn);
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();
        Log.d("Internet", "is mobile network connected: " + isMobileConn);

        return isMobileConn || isWifiConn;
    }
}
