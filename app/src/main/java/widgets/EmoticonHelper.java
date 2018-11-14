package widgets;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import common.EmoticonConstant;
import common.EmoticonLruCache;
import entity.ConfigItem;
import entity.EmoticonConfigs;
import entity.EmoticonEntity;
import entity.EmoticonEntityItem;
import entity.GroupConfig;
import network.NetBuilder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.FileUtil;
import utils.ImageUtils;
import utils.MD5Util;
import utils.NetworkUtil;
import utils.ZipUtils;

/**
 * created by shonary on 18/10/20
 * email： xiaonaxi.mail@gmail.com
 */
public class EmoticonHelper {

    private boolean mIsStartingDownload = false;

    private volatile List<EmoticonConfigs.TabConfig> mLocalTabConfigs = new ArrayList<>();

    private List<EmoticonConfigs.TabConfig> mAssetsTabConfigs = null;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private List<EmoticonEntity> mEmojiList = new ArrayList<>();

    private Pattern mPattern = null;

    private static EmoticonHelper mInstance;

    private final byte[] mLock = new byte[1];

    private EmoticonHelper() {
    }

    public static EmoticonHelper getInstance() {
        if (mInstance == null) {
            synchronized (EmoticonHelper.class) {
                if (mInstance == null) {
                    mInstance = new EmoticonHelper();
                }
            }
        }
        return mInstance;
    }

    public void initConfigs(final Context context) {
        // 若已经初始化出Emoji列表，则不用初始化
        if (mEmojiList != null && mEmojiList.size() > 0) {
            return;
        }
        // 获取本地的表情配置
        List<EmoticonConfigs.TabConfig> localTabConfigs = readConfigFromLocal();
        if (localTabConfigs == null || localTabConfigs.size() == 0) {
            // 若本地配置为空的话，则使用Assests中的配置
            localTabConfigs = readConfigFromAssests(context);
            mAssetsTabConfigs = localTabConfigs;
        }
        syncEmojis(context, localTabConfigs, false);
    }


    public void reqEmojiConfigsFromServer(final Context context) {
        if (context == null) {
            return;
        }

        if (!mIsStartingDownload) {

            mIsStartingDownload = true;

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Call<EmoticonConfigs> call = NetBuilder.getIntance().getApi().ExpressionConfig();
                    call.enqueue(new Callback<EmoticonConfigs>() {
                        @Override
                        public void onResponse(Call<EmoticonConfigs> call, Response<EmoticonConfigs> response) {
                            dealWithEmotionConfig(context, response.body());
                        }

                        @Override
                        public void onFailure(Call<EmoticonConfigs> call, Throwable t) {
                            mIsStartingDownload = false;
                        }
                    });
                }

            });
        }
    }


    public boolean isExistLocalConfigs() {
        String json = loadJSONFromLocal(EmoticonConstant.EMOJI_DATA_TABCONFIGS_PATH);
        if (TextUtils.isEmpty(json)) {
            return false;
        }
        return true;
    }

    private void dealWithEmotionConfig(Context context, EmoticonConfigs emojiConfigMeta) {
        if (emojiConfigMeta == null) {
            mIsStartingDownload = false;
            return;
        }

        List<EmoticonConfigs.TabConfig> tabConfigs = emojiConfigMeta.getData();
        tabConfigs = clearListNull(tabConfigs);
        if (tabConfigs == null) {
            mIsStartingDownload = false;
            return;
        }

        try {
            List<EmoticonConfigs.TabConfig> needUpdates = new ArrayList<>();
            Iterator iterator = tabConfigs.iterator();
            synchronized (mLock) {
                while (iterator.hasNext()) {
                    EmoticonConfigs.TabConfig config = (EmoticonConfigs.TabConfig) iterator.next();
                    if (isGroupConfigChanged(config)) {
                        needUpdates.add(config);
                    }
                }
            }

            if (needUpdates.size() != 0) {
                downloadEmoji(context, needUpdates);

            } else {
                syncEmojis(context, tabConfigs, true);
                mIsStartingDownload = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mIsStartingDownload = false;
        }
    }

    private List<EmoticonConfigs.TabConfig> clearListNull(List<EmoticonConfigs.TabConfig> list) {
        if (list == null) {
            return null;
        }
        List<EmoticonConfigs.TabConfig> nullArr = new ArrayList<>();
        nullArr.add(null);
        list.removeAll(nullArr);
        return list;
    }

    private volatile int downloadCount = 0;

    private void downloadEmoji(final Context context, final List<EmoticonConfigs.TabConfig> needUpdates) {
        final int needDownloadCount = needUpdates.size();
        if (NetworkUtil.isWifi(context)) {

            final HashMap<String,String> path = new HashMap<>();

            for (final EmoticonConfigs.TabConfig config : needUpdates) {
                if (config == null) {
                    continue;
                }
                String filePath = String.format(EmoticonConstant.EMOJI_DOWNLOAD_FILE, config.groupId, config.groupVersion);
                path.put(config.downloadUrl,filePath);
                new AsyncTask<Void, Long, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        Call<ResponseBody> request = NetBuilder.getIntance().getApi().downloadFileWithDynamicUrlAsync(config.downloadUrl);
                        request.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(final Call<ResponseBody> call, final Response<ResponseBody> response) {
                                downloadCount++;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String url = call.request().url().toString();
                                        writeResponseBodyToDisk(response.body(), path.get(url));
                                        handleDownloadFile(context, config, path.get(url));

                                        if (downloadCount == needDownloadCount) {
                                            downloadCount = 0;
                                            mIsStartingDownload = false;
                                        }
                                    }
                                }).start();
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                downloadCount++;
                                if (downloadCount == needDownloadCount) {
                                    downloadCount = 0;
                                    mIsStartingDownload = false;
                                }
                            }
                        });

                        return null;
                    }
                }.execute();

            }

        }
    }

    private  boolean writeResponseBodyToDisk(ResponseBody body, String filePath) {
        try {
            File dirPath = new File(EmoticonConstant.EMOJI_DOWNLOAD_DIR);
            if(!dirPath.exists()){
                dirPath.mkdirs();
            }
            File futureStudioIconFile = new File(filePath);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long downloadedSize = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    downloadedSize += read;
                }
                outputStream.flush();
                return true;
            } catch (Exception e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void handleDownloadFile(Context context, EmoticonConfigs.TabConfig config, String fileName) {
        try {
            int index = fileName.lastIndexOf(".");
            if (index > 0) {
                String savedDirPath = fileName.substring(0, index);
                ZipUtils.unZipFolder(fileName, savedDirPath);

                List<EmoticonConfigs.TabConfig> list = new ArrayList<>();
                list.add(config);
                syncEmojis(context, list, true);
            }
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * use md5 check file,your can choose use it or not.
     * @param config
     * @param fileName
     * @return
     */
    private boolean isDownloadFileRight(EmoticonConfigs.TabConfig config, String fileName) {
        if (config == null || TextUtils.isEmpty(fileName)) {
            return false;
        }
        String name = String.format(EmoticonConstant.EMOJI_DOWNLOAD_FILE, config.groupId, config.groupVersion);

        if (TextUtils.equals(fileName, name)) {
            String md5Server = config.md5.toLowerCase(Locale.US);
            String md5Local = MD5Util.getFileMD5(fileName, true);

            if (TextUtils.equals(md5Local, md5Server)) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    private synchronized boolean replaceTabConfig(List<EmoticonConfigs.TabConfig> configs) {
        if (configs == null || configs.size() == 0) {
            return false;
        }
        Gson gson = new Gson();
        String configJson = gson.toJson(configs);
        FileOutputStream fileOutputStream = null;
        try {
            File mgjFile = new File(EmoticonConstant.EMOJI_DIR_PATH);
            if (!mgjFile.exists()) {
                mgjFile.mkdirs();
            }
            File configFile = new File(EmoticonConstant.EMOJI_DATA_TABCONFIGS_PATH);
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            fileOutputStream = new FileOutputStream(EmoticonConstant.EMOJI_DATA_TABCONFIGS_PATH);
            fileOutputStream.write(configJson.getBytes(Charset.forName("UTF-8")));
            fileOutputStream.flush();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isGroupConfigChanged(EmoticonConfigs.TabConfig tabConfig) {
        if (tabConfig == null) {
            return false;
        }

        if (mLocalTabConfigs == null || mLocalTabConfigs.size() == 0) {
            return true;
        }

        if (mAssetsTabConfigs != null && mAssetsTabConfigs.size() > 0) {
            for (EmoticonConfigs.TabConfig config : mAssetsTabConfigs) {
                if (config.groupId == tabConfig.groupId) {
                    if (config.groupVersion >= tabConfig.groupVersion) {
                        return false;
                    }
                }
            }
        }

        for (EmoticonConfigs.TabConfig config : mLocalTabConfigs) {
            if (config.groupId == tabConfig.groupId) {
                if (config.groupVersion >= tabConfig.groupVersion) {
                    return false;
                }
            }
        }

        return true;
    }

    private List<EmoticonConfigs.TabConfig> readConfigFromLocal() {
        String json = loadJSONFromLocal(EmoticonConstant.EMOJI_DATA_TABCONFIGS_PATH);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<EmoticonConfigs.TabConfig>>() {
            }.getType();
            List<EmoticonConfigs.TabConfig> tabConfigs = gson.fromJson(json, type);
            if (tabConfigs == null) {
                return null;
            }
            return tabConfigs;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        } catch (ArrayStoreException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<EmoticonConfigs.TabConfig> readConfigFromAssests(Context context) {
        String json = loadJSONFromAsset(context, EmoticonConstant.EMOJI_ASSETS_TABCONFIGS_PATH);
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<EmoticonConfigs.TabConfig>>() {
        }.getType();
        List<EmoticonConfigs.TabConfig> tabConfigs = gson.fromJson(json, type);
        if (tabConfigs == null) {
            return null;
        }

        return tabConfigs;
    }

    public List<EmoticonEntity> getEmojis() {
        return mEmojiList;
    }

    public Pattern getPattern() {
        return mPattern;
    }

    private synchronized void syncEmojis(final Context context, final List<EmoticonConfigs.TabConfig> configs, boolean isReplace) {
        if (context == null || configs == null || configs.size() == 0) {
            return;
        }

        StringBuilder patternString = new StringBuilder();
        patternString.append('(');

        List<EmoticonEntity> list = new ArrayList<>();
        List<EmoticonConfigs.TabConfig> localTabConfigs = new ArrayList<>();

        if (mLocalTabConfigs == null || mLocalTabConfigs.size() == 0) {
            localTabConfigs = configs;
        } else {
            localTabConfigs.addAll(mLocalTabConfigs);
            for (EmoticonConfigs.TabConfig config : configs) {
                if (localTabConfigs.contains(config)) {
                    continue;
                }
                localTabConfigs.add(config);
            }

            Collections.sort(localTabConfigs, new Comparator<EmoticonConfigs.TabConfig>() {
                @Override
                public int compare(EmoticonConfigs.TabConfig lhs, EmoticonConfigs.TabConfig rhs) {
                    if (lhs.tabId > rhs.tabId) {
                        return 1;
                    }

                    if (lhs.tabId < rhs.tabId) {
                        return -1;
                    }

                    return 0;
                }
            });
        }

        for (EmoticonConfigs.TabConfig config : localTabConfigs) {
            EmoticonEntity emoji = readGroupConfig(context, config, patternString);
            if (emoji != null) {
                list.add(emoji);
            }
        }
        String pattern = "";
        if (patternString != null) {
            pattern = patternString.toString();
            if (!TextUtils.isEmpty(pattern) && pattern.endsWith("|")) {
                patternString.replace(patternString.length() - 1, patternString.length(), ")");
                pattern = patternString.toString();
            } else {
                pattern = "()";
            }
        } else {
            pattern = "()";
        }

        synchronized (mLocalTabConfigs) {
            if (isReplace) {
                replaceTabConfig(localTabConfigs);
            }
            mLocalTabConfigs = localTabConfigs;
            mEmojiList = list;
            try {
                mPattern = Pattern.compile(pattern);
            } catch (Exception e) {
                e.printStackTrace();
                mPattern = null;
            }
        }
    }

    private synchronized EmoticonEntity readGroupConfig(Context context, EmoticonConfigs.TabConfig tabConfig, StringBuilder patternString) {
        try {
            String config = "";
            boolean isAssets = false;
            String filePath = String.format(EmoticonConstant.EMOJI_DATA_GROUP_FILE_PATH, tabConfig.groupId, tabConfig.groupVersion);
            File file = new File(filePath);
            if (!file.exists()) {
                filePath = String.format(EmoticonConstant.EMOJI_ASSETS_GROUP_FILE_PATH, tabConfig.groupId, tabConfig.groupVersion);
                config = loadJSONFromAsset(context, filePath);
                isAssets = true;
            } else {
                config = loadJSONFromLocal(filePath);
            }
            if (TextUtils.isEmpty(config)) {
                return null;
            }
            Gson gson = new Gson();
            GroupConfig groupConfig = gson.fromJson(config, GroupConfig.class);
            if (groupConfig == null) {
                return null;
            }
            return changeGroupConfigToEmoji(context, groupConfig, patternString, isAssets);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (IncompatibleClassChangeError e) {
            return null;
        }
    }

    private EmoticonEntity changeGroupConfigToEmoji(Context context, GroupConfig groupConfig,
                                                    StringBuilder patternString, boolean isFromAssets) {
        EmoticonEntity emoji = new EmoticonEntity();
        emoji.groupId = groupConfig.groupId;
        emoji.version = groupConfig.version;
        emoji.type = groupConfig.type;
        if (isFromAssets) {
            String assetsPath = String.format(EmoticonConstant.EMOJI_ASSETS_PATH, groupConfig.groupId, groupConfig.version, groupConfig.icon);
            emoji.icon = assetsPath;
        } else {
            emoji.icon = String.format(EmoticonConstant.EMOJI_DATA_PATH, groupConfig.groupId, groupConfig.version, groupConfig.icon);
        }

        emoji.title = groupConfig.title;
        emoji.items = new ArrayList<>();
        for (ConfigItem item : groupConfig.emotions) {
            EmoticonEntityItem emojiItem = new EmoticonEntityItem();
            emojiItem.id = item.id;
            emojiItem.connName = item.tag;
            if (isFromAssets) {
                emojiItem.fileName = item.file;
            } else {
                emojiItem.fileName = String.format(EmoticonConstant.EMOJI_DATA_PATH, groupConfig.groupId, groupConfig.version, item.file);
            }
            emojiItem.groupId = emoji.groupId;
            emojiItem.type = emoji.type;
            emoji.items.add(emojiItem);

            patternString.append(Pattern.quote(emojiItem.connName));
            patternString.append('|');

            try {
                Drawable drawable = null;
                if (isFromAssets) {
                    String assetsPath = String.format(EmoticonConstant.EMOJI_ASSETS_PATH, groupConfig.groupId, groupConfig.version, emojiItem.fileName);
                    drawable = ImageUtils.getDrawableByAssetsName(context, assetsPath);
                } else {
                    drawable = Drawable.createFromPath(emojiItem.fileName);
                }
                if (drawable != null) {
                    EmoticonLruCache.getInstance().set(emojiItem.fileName, drawable);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return emoji;
    }

    private String loadJSONFromLocal(String filePath) {
        try {
            byte[] buffer = FileUtil.getFileContent(filePath);
            if (buffer == null) {
                return null;
            }

            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String loadJSONFromAsset(Context context, String filePath) {
        if (context == null) {
            return null;
        }
        InputStream inputStream = null;
        try {
            AssetManager assetManager = context.getAssets();
            inputStream = assetManager.open(filePath);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
