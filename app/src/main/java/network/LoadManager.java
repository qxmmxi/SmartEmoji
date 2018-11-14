package network;

import android.content.Context;

import utils.NetworkUtil;
import widgets.EmoticonHelper;

/**
 * created by shonary on 18/10/25
 * email： xiaonaxi.mail@gmail.com
 */
public class LoadManager {

    static LoadManager loadManager =null;
    static Object object = new Object();

    public  static LoadManager getIntance(){
        if (loadManager == null){
            synchronized (object){
                if (loadManager == null){
                    loadManager = new LoadManager();
                }
            }
        }
        return loadManager;
    }

    public void requestEmotion(Context ctx){
        if (NetworkUtil.isWifi(ctx)) {
            EmoticonHelper.getInstance().reqEmojiConfigsFromServer(ctx);
        }
    }
}
