package app;

import android.app.Application;
import android.content.Context;
import network.LoadManager;
import widgets.EmoticonHelper;

/**
 * created by shonary on 18/11/2
 * email： xiaonaxi.mail@gmail.com
 */
public class APP extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        EmoticonHelper.getInstance().initConfigs(mContext);
        LoadManager.getIntance().requestEmotion(mContext);

    }
}