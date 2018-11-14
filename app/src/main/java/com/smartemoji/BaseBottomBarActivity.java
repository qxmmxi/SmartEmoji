package com.smartemoji;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import common.SysConstant;
import utils.ImageUtils;
import widgets.AndroidSmartInputorView;
import widgets.CustomToast;
import widgets.EmoticonParserHelper;
import widgets.ResizeLayoutView;


/**
 * created by shonary on 18/10/29
 * email： xiaonaxi.mail@gmail.com
 */
public class BaseBottomBarActivity extends Activity implements AndroidSmartInputorView.BottomBarOnClickListener {

    AndroidSmartInputorView bottomBarView;
    TextView textView;
    private ResizeLayoutView mResizeLayout = null;
    private int mRootBottom = Integer.MIN_VALUE;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        bottomBarView = findViewById(R.id.bottom_bar);
        textView = findViewById(R.id.show_words);
        imageView = findViewById(R.id.image);
//        textView.setText(StringUtils.getEmojiByUnicode(0x1F60A)+"hello"+StringUtils.getEmojiByUnicode("0x1F60B"));
        mResizeLayout = findViewById(R.id.resize_layout);
        findViewById(R.id.container).setOnTouchListener(mOnTouchListener);
        bottomBarView.setHomeView(this);
        bottomBarView.initSoftInputMethod();
        bottomBarView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        bottomBarView.enableInputView(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        mResizeLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mResizeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        } else {
            mResizeLayout.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
        }
    }

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            mResizeLayout.getGlobalVisibleRect(r);
            if (mRootBottom == Integer.MIN_VALUE) {
                mRootBottom = r.bottom;
                return;
            }
            if (r.bottom < mRootBottom) {
                if (bottomBarView != null) {
                    bottomBarView.setKeyboardPannel(mRootBottom, r.bottom);
                }
            }
        }
    };

    public void setSoftInputMode(int mode) {
        getWindow().setSoftInputMode(mode);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (bottomBarView != null && !bottomBarView.isBottomPannel()) {
                this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (bottomBarView != null) {
                        bottomBarView.pannelBottomDown();
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    closeBottomAllPanel();
                    break;
            }
            return false;
        }
    };

    public void closeBottomAllPanel() {
        if (bottomBarView != null) {
            bottomBarView.pannelBottomDown();
            bottomBarView.dismissShortCutPopuWidow();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
            case SysConstant.CallBack.TAKE_PHOTO:
                if (resultCode == RESULT_OK&&null!=data) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageView.setImageBitmap(imageBitmap);
                }
                break;
            case SysConstant.CallBack.PICK_PICTURE:
                if (resultCode == RESULT_OK&&null!=data) {
                    Uri selectImageUri = data.getData();
                    try {
                        Bitmap bitmap = ImageUtils.getBitmapFormUri(this, selectImageUri);
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case SysConstant.CallBack.TAKE_PHOTO_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CustomToast.getInstance(this).show(getString(R.string.take_photo_permission));
                }
                break;
        }
    }

    @Override
    public void onSendClick(String str) {
        textView.setText(EmoticonParserHelper.getInstance().emoCharsequence(this,str));
    }
}
