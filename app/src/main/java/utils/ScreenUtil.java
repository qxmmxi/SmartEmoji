package utils;

import android.content.Context;

/**
 * created by shonary on 18/10/27
 * email： xiaonaxi.mail@gmail.com
 */
public class ScreenUtil {

    public static int dp2px(Context context,int dip) {
        if(context != null){
            float density = context.getResources().getDisplayMetrics().density;
            return (int) (dip * density + 0.5);
        }
        return dip;
    }

    public static int px2dp(Context context,int px) {
        if(context != null){
            float density = context.getResources().getDisplayMetrics().density;
            return (int) ((px - 0.5) / density);
        }
        return px;
    }
}