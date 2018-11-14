package common;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.LruCache;

/**
 * created by shonary on 18/10/22
 * email： xiaonaxi.mail@gmail.com
 */
public class EmoticonLruCache {

    private static final int MAX_MEMORY = (int) Runtime.getRuntime().maxMemory();

    private LruCache mCache = null;

    private static EmoticonLruCache mInstance = null;

    private EmoticonLruCache() {
        if (mCache == null) {
            mCache = new LruCache<String, Drawable>(MAX_MEMORY / 8) {
                @Override
                protected int sizeOf(String key, Drawable value) {
                    Bitmap bitmap = ((BitmapDrawable) value).getBitmap();
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }

                @Override
                protected void entryRemoved(boolean evicted, String key, Drawable oldValue, Drawable newValue) {
                    super.entryRemoved(evicted, key, oldValue, newValue);
                }
            };
        }
    }

    public static synchronized EmoticonLruCache getInstance() {
        if (mInstance == null) {
            synchronized (EmoticonLruCache.class) {
                if (mInstance == null) {
                    mInstance = new EmoticonLruCache();
                }
            }
        }
        return mInstance;
    }

    public void set(String path, Drawable bitmap) {
        if (TextUtils.isEmpty(path) || bitmap == null) {
            return;
        }
        Drawable drawable = get(path);
        if(drawable != null){
            return;
        }
        mCache.put(path, bitmap);
    }

    public Drawable get(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        return (Drawable) mCache.get(path);
    }

    public void remove(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        mCache.remove(path);
    }

    public void clear() {
        mCache.evictAll();
    }

}
