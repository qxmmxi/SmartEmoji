package common;


import com.smartemoji.BuildConfig;

/**
 * created by shonary on 18/10/21
 * email： xiaonaxi.mail@gmail.com
 */
public class SysConstant {

    public static final class URL {
        public static final String  EXPRESSION_CONFIG_URL = BuildConfig.BASE_URL;
    }

    public static final class Other {
        public static final int MAX_SEND_TEXT_LENGTH = 300;
        public static final String IMAGE_JPG_FORMAT = ".jpg";
    }

    public static final class CallBack {
        public static final int TAKE_PHOTO_PERMISSION = 1110;
        public static final int TAKE_PHOTO = 1111;
        public static final int PICK_PICTURE = 1112;
    }

    public static final class FilePath{
        public static final String APP_SAVE_PATH = "SMART-BOTTOMBAR";
        public static final String IMAGE_SAVE_PATH = "IMAGE";
    }
}
