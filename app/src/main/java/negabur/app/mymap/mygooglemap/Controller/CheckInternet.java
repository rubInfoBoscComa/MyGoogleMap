package negabur.app.mymap.mygooglemap.Controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Ruben on 12/3/15.
 */
public class CheckInternet{

    //Constructor per defecte
    public CheckInternet() {
    }

    /**
     * Mètode que comprova si s'està connectat a la xarxa Wifi
     *
     * @return true o false
     */
    protected static Boolean connexioWifi(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (info != null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Mètode que comprova si s'està connectat a la xarxa Mòvil
     *
     * @return true o false
     */
    protected static Boolean connexioMovil(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (info != null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }
}

