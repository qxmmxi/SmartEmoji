package utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * created by shonary on 18/10/22
 * email： xiaonaxi.mail@gmail.com
 */
public class NetworkUtil {

    public static boolean isWifi(Context ctx){
        NetState state = getNetworkClass(ctx);
        return state != null && state == NetState.NET_WIFI;
    }

    public static boolean isNetWorkAvalible(Context context) {
        try {
            NetworkInfo ni = getNetworkInfo(context);
            if (null != ni && ni.isConnected()) {
                return true;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static NetworkInfo getNetworkInfo(Context context){
        try {
            if (context == null) {
                return null;
            }
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return null;
            }
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String getNetIdentity(Context ctx){
        NetState netWorkDes;
        String netKey = null;
        try {
            NetworkInfo ni = getNetworkInfo(ctx);
            if (ni != null && ni.isConnected()) {
                switch (ni.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        netWorkDes = NetState.NET_WIFI;
                        netKey = netWorkDes.getDesc() + "_" + ni.getExtraInfo();
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                        netWorkDes = NetworkUtil.getMobileClass(ni.getSubtype());
                        netKey = netWorkDes.getDesc() + "_" + ni.getTypeName() + "_" + ni.getExtraInfo();
                        break;
                    default:
                        netWorkDes = NetState.NETWORK_UNKNOWN;
                        netKey = netWorkDes.getDesc() + "_" + ni.getTypeName() + "_" + ni.getExtraInfo();
                        break;
                }
            }
            return netKey;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static NetState getNetworkClass(Context ctx) {
        NetState netWorkDes = NetState.NET_NO;
        try {
            NetworkInfo ni = getNetworkInfo(ctx);
            if (ni != null && ni.isConnected()) {
                switch (ni.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        netWorkDes = NetState.NET_WIFI;
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                        netWorkDes = NetworkUtil.getMobileClass(ni.getSubtype());
                        break;
                    default:
                        netWorkDes = NetState.NETWORK_UNKNOWN;
                        break;
                }
            }
            return netWorkDes;
        }catch (Exception e){
            e.printStackTrace();
            return netWorkDes;
        }
    }

    private static NetState getMobileClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NetState.NETWORK_2_G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NetState.NETWORK_3_G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NetState.NETWORK_4_G;
            default:
                return NetState.NETWORK_UNKNOWN;
        }
    }

    public enum NetState {
        NET_NO("No network available"),
        NETWORK_UNKNOWN("unknown"),
        NETWORK_2_G("2g"),
        NETWORK_3_G("3g"),
        NETWORK_4_G("4g"),
        NET_WIFI("wifi");
        private String desc;
        NetState(String des) {
            this.desc = des;
        }

        public String getDesc() {
            return desc;
        }
    }
}
