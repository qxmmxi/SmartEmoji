package common;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * created by shonary on 18/10/22
 * email： xiaonaxi.mail@gmail.com
 */
public class SPreferences {

    public static boolean saveIntegerExtra(Context context, String spName, String spKey, int integerExtra) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sp.edit().putInt(spKey, integerExtra).commit();
    }

    public static boolean saveStringExtra(Context context, String spName, String spKey, String stringExtra) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sp.edit().putString(spKey, stringExtra).commit();
    }

    public static int getIntegerExtra(Context context, String spName, String spKey) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sp.getInt(spKey, -1);
    }
}
