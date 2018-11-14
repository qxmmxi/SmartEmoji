package utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

import common.SysConstant;

/**
 * created by shonary on 18/11/8
 * email： xiaonaxi.mail@gmail.com
 */
public class CommonUtil {

    public static boolean checkSDCard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getSDcardSavePath(String dirName,String fileName) {
        String filePath = Environment.getExternalStorageDirectory().toString() + File.separator
                + SysConstant.FilePath.APP_SAVE_PATH + File.separator + dirName + File.separator;
        String path = filePath + fileName;
        if(!TextUtils.isEmpty(path)) {
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            return path;
        }
        return "";
    }

}
