package common;


import java.io.File;

import app.APP;
import utils.FileUtil;

/**
 * created by shonary on 18/10/23
 * email： xiaonaxi.mail@gmail.com
 */
public class EmoticonConstant {

    public static final String EMOJI_DIR_PATH =  FileUtil.getDataFilePath(APP.getContext()) + SysConstant.FilePath.APP_SAVE_PATH
            + File.separator + "emoji";

    public static final String EMOJI_ZIP_FILE = "group%d-%d.zip";

    public static final String EMOJI_DOWNLOAD_DIR = EMOJI_DIR_PATH + File.separator;

    public static final String EMOJI_DOWNLOAD_FILE = EMOJI_DOWNLOAD_DIR  + EMOJI_ZIP_FILE;

    public static final String EMOJI_ASSETS_TABCONFIGS_PATH = "emoji/tabconfigs.json";

    public static final String EMOJI_DATA_TABCONFIGS_PATH = EMOJI_DIR_PATH + File.separator + "tabconfigs.json";

    public static final String EMOJI_ASSETS_PATH = "emoji/group%d-%d/%s";

    public static final String EMOJI_DATA_PATH = EMOJI_DIR_PATH + File.separator + "group%d-%d/%s";

    public static final String EMOJI_ASSETS_GROUP_FILE_PATH = "emoji/group%d-%d/group.json";

    public static final String EMOJI_DATA_GROUP_FILE_PATH = EMOJI_DIR_PATH + File.separator + "group%d-%d/group.json";

}
