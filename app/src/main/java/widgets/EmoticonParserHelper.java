package widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.EmoticonLruCache;
import entity.EmoticonEntity;
import entity.EmoticonEntityItem;
import utils.FileUtil;
import utils.ImageUtils;
import utils.ScreenUtil;


/**
 * created by shonary on 18/10/23
 * email： xiaonaxi.mail@gmail.com
 */
public class EmoticonParserHelper {

    private static final int EOF = -1;

    public static final int EMOJI_UNICODE = 0;
    public static final int EMOJI_IMAGE = 1;
    public static final int EMOJI_GIF = 2;

    private static EmoticonParserHelper mInstance = null;

    private EmoticonParserHelper() {
    }

    public static EmoticonParserHelper getInstance() {
        if (mInstance == null) {
            synchronized (EmoticonParserHelper.class) {
                if (mInstance == null) {
                    mInstance = new EmoticonParserHelper();
                }
            }
        }
        return mInstance;
    }

    public CharSequence emoCharsequence(Context context, CharSequence text) {
        return emoCharsequence(context, text, 0.8f);
    }

    public static boolean isGif(String connName) {
        List<EmoticonEntity> list = EmoticonHelper.getInstance().getEmojis();
        if (list == null) {
            return false;
        }
        try {
            JSONObject jsonObject = new JSONObject(connName);
            connName = jsonObject.optString("emotionTag");
            for (EmoticonEntity emoji : list) {
                if (emoji.type == EMOJI_GIF) {
                    for (EmoticonEntityItem item : emoji.items) {
                        if (item.connName.equals(connName)) {
                            return true;
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Drawable getDrawable(Context context, String connName) {
        if (TextUtils.isEmpty(connName)) {
            return null;
        }

        String fileName = changeToFileName(connName);
        if (TextUtils.isEmpty(fileName)) {
            fileName = connName;
        }

        return getDrawableByFileName(context, fileName);
    }

    public byte[] getBytesOfGif(String connName) {
        if (TextUtils.isEmpty(connName)) {
            return null;
        }

        String fileName = changeToFileName(connName);
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        return toByteArray(fileName);
    }

    public String getEmojiFileName(String connName) {
        if (TextUtils.isEmpty(connName)) {
            return null;
        }

        String fileName = changeToFileName(connName);
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        return fileName;
    }

    private Drawable getDrawableByFileName(Context context, String fileName) {

        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        Drawable drawable =  EmoticonLruCache.getInstance().get(fileName);
        if (drawable != null) {
            return drawable;
        }

        try {
            if (checkIsAssets(fileName,context) || fileName.contains("back")) {
                drawable = ImageUtils.getDrawableByAssetsName(context,fileName);
            } else {
                drawable = Drawable.createFromPath(fileName);
            }

            if (drawable == null) {
                return null;
            }

            EmoticonLruCache.getInstance().set(fileName, drawable);

            return drawable;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private CharSequence emoCharsequence(Context context, CharSequence text, float scale) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }

        Pattern pattern = EmoticonHelper.getInstance().getPattern();
        if (pattern == null) {
            return text;
        }

        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String item = matcher.group();
            Drawable drawable = getDrawable(context, item);
            if (drawable != null) {
                Drawable newDrawable = getNewDrawable(drawable);
                int size = (int) (getElementSize(context) * scale);
                newDrawable.setBounds(0, 0, size, size);
                ImageSpan imageSpan = new ImageSpan(newDrawable, ImageSpan.ALIGN_BOTTOM);
                builder.setSpan(imageSpan, matcher.start(), matcher.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return builder;
    }

    private String changeToFileName(String connName) {
        String fileName = null;

        List<EmoticonEntity> list = EmoticonHelper.getInstance().getEmojis();

        for (EmoticonEntity emoji : list) {
            for (EmoticonEntityItem item : emoji.items) {
                if (TextUtils.equals(item.connName, connName)) {
                    fileName = item.fileName;
                    break;
                }
            }
        }
        return fileName;
    }

    private boolean checkIsAssets(String fileName,Context context) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        boolean isAssets = true;
        String startPath = FileUtil.getDataFilePath(context);

        if(TextUtils.isEmpty(startPath)){
            startPath = "/data/";
        }
        if (fileName.startsWith(startPath)) {
            isAssets = false;
        }
        return isAssets;
    }

    private byte[] toByteArray(String fileName) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(fileName);
            outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int n = 0;
            while (EOF != (n = inputStream.read(buffer))) {
                outputStream.write(buffer, 0, n);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Drawable getNewDrawable(Drawable drawable) {
        return drawable.getConstantState().newDrawable();
    }

    private int getElementSize(Context context) {
        if (context != null) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int screenHeight = ScreenUtil.px2dp(context,dm.heightPixels);
            int screenWidth = ScreenUtil.px2dp(context,dm.widthPixels);
            int size = screenWidth / 6;
            if (screenWidth >= 800) {
                size = 60;
            } else if (screenWidth >= 650) {
                size = 55;
            } else if (screenWidth >= 600) {
                size = 50;
            } else if (screenHeight <= 400) {
                size = 20;
            } else if (screenHeight <= 480) {
                size = 25;
            } else if (screenHeight <= 520) {
                size = 30;
            } else if (screenHeight <= 570) {
                size = 35;
            } else if (screenHeight <= 640) {
                if (dm.heightPixels <= 960) {
                    size = 35;
                } else if (dm.heightPixels <= 1000) {
                    size = 45;
                } else if (dm.heightPixels <= 1280) {
                    size = 50;
                }
            }
            return size;
        }
        return 40;
    }

}
