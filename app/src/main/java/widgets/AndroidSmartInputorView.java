package widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import common.SPreferences;
import common.SysConstant;
import entity.EmoticonEntityItem;
import com.smartemoji.BaseBottomBarActivity;
import com.smartemoji.R;


/**
 * created by shonary on 18/11/3
 * email： xiaonaxi.mail@gmail.com
 */
public class AndroidSmartInputorView extends LinearLayout implements View.OnClickListener, TextWatcher,
        View.OnFocusChangeListener, EmoticonView.EventListener {

    private static final int SOFT_KEYBOARD_MODE_NO_PANET = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
    private static final int SOFT_KEYBOARD_MODE_HAS_PANET = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
    private static final String KEYBOARD_SP_NAME = "keyboard_config.ini";
    private static final String DEFAULT_INPUT_METHOD = "default_inputmethod";

    private Context mContext = null;
    private View rootView = null;
    private ConstraintLayout mBottomLayout = null;
    private CustomEditView mEdit = null;
    private TextView mSendBtn = null;
    private ImageView mAddMoreBtn = null;
    private ImageView mAddEmoBtn = null;
    private EmoticonView mEmoGridView = null;
    private CustomPanelView mMorePanelView = null;
    private InputMethodManager inputManager = null;
    private PopupWindow mShortCutPopupWindow;
    private boolean isPannelDown = false;
    private volatile boolean isShowSoftKeyBoard = false;
    private boolean isShowPanelView = false;
    private volatile boolean isShowEmoView = false;
    private int keyboardHeight = 0;
    private SwitchInputMethodReceiver mInputMethodReceiver;
    private String mCurrentInputMethod;
    private BottomBarOnClickListener bottomBarOnClickListener;

    public AndroidSmartInputorView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public AndroidSmartInputorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AndroidSmartInputorView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setMinimumHeight(45);
        this.setOrientation(VERTICAL);
        this.setLayoutParams(params);

        LayoutInflater inflater = LayoutInflater.from(getContext());

        rootView = inflater.inflate(R.layout.smart_inputor_layout, this,true);
        mSendBtn = rootView.findViewById(R.id.send_btn);
        mEdit = rootView.findViewById(R.id.edit_text);
        mAddMoreBtn = rootView.findViewById(R.id.show_add_more_btn);
        mAddEmoBtn = rootView.findViewById(R.id.show_emo_btn);
        mBottomLayout =  rootView.findViewById(R.id.pannel_container);
        mEmoGridView = rootView.findViewById(R.id.emo_gridview);
        mMorePanelView = rootView.findViewById(R.id.more_panel);

        inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        mAddMoreBtn.setOnClickListener(this);
        mAddEmoBtn.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mEdit.setOnClickListener(this);
        mEdit.addTextChangedListener(this);
        mEdit.setOnFocusChangeListener(this);
        mEmoGridView.setEventListener(this);

        initMessageEdit();

        mEdit.setOnFocusChangeListener(mEditOnFocusChangeListener);

        mBottomLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    public String getMessageEditText(){
        return mEdit.getText().toString().trim();
    }

    public void setMessageEditText(CharSequence editText){
        if(mEdit != null){
            mEdit.setText(editText);
            mEdit.setSelection(editText.length());
        }
    }

    private void initMessageEdit() {
        if (mEdit != null) {
            mEdit.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    temp = s;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        int selectionStart = mEdit.getSelectionStart();
                        int selectionEnd = mEdit.getSelectionEnd();
                        int textLen = temp.length();
                        int diff = textLen - SysConstant.Other.MAX_SEND_TEXT_LENGTH;
                        if (diff > 0) {
                            CustomToast.getInstance(getContext()).show(getResources().getString(R.string.message_too_long));
                            s.delete(selectionStart - diff, selectionEnd);
                            mEdit.setText(s);
                            mEdit.setSelection(s.length());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        onTextChangedAction(s);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (mEmoGridView.getVisibility() == View.VISIBLE) {
                mEmoGridView.setVisibility(View.GONE);
            }
        }
    }


    private OnFocusChangeListener mEditOnFocusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                if (((mEmoGridView.getVisibility() == View.VISIBLE) || mMorePanelView.getVisibility()== VISIBLE )
                        && keyboardHeight > 0) {
                    setSoftInputMode(SOFT_KEYBOARD_MODE_HAS_PANET);
                    inputManager.showSoftInput(mEdit, 0);
                    isShowSoftKeyBoard = true;
                } else {
                    isPannelDown = false;
                    if (keyboardHeight <= 0) {
                        isShowPanelView = false;
                        isShowEmoView = false;
                        isShowSoftKeyBoard = true;
                        mMorePanelView.setVisibility(GONE);
                        mEmoGridView.setVisibility(GONE);
                    }
                    setSoftInputMode(SOFT_KEYBOARD_MODE_NO_PANET);
                }
            }else{
                isShowSoftKeyBoard = false;
            }
        }
    };

    private void setSoftInputMode(int mode){
        getHomeView().setSoftInputMode(mode);
    }


    private void onTextChangedAction(CharSequence s) {
        if (s.length() > 0) {
            onTextAdd();
        } else {
            onTextRemove();
        }
    }

    private void onTextAdd() {
        mSendBtn.setVisibility(View.VISIBLE);
    }


    private void onTextRemove() {
        mSendBtn.setVisibility(View.GONE);

    }

    private void clearEditTextFocus() {
        mBottomLayout.setFocusable(true);
        mBottomLayout.setFocusableInTouchMode(true);
        mEdit.clearFocus();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.show_emo_btn) {
            showEmoje();
        } else if (id == R.id.send_btn) {
            String text = getMessageEditText();
            bottomBarOnClickListener.onSendClick(text);
            mEdit.setText("");
        }else if (id == R.id.show_add_more_btn){
            showAddMore();
        }
    }

    public interface BottomBarOnClickListener {
        void onSendClick(String str);
    }

    private void showAddMore() {
        isPannelDown = false;
        mAddEmoBtn.setVisibility(View.VISIBLE);
        mEdit.setVisibility(View.VISIBLE);
        if (!isShowPanelView) {
            if (mEdit.hasFocus()) {
                mEdit.clearFocus();
            }
            if (mMorePanelView.getVisibility() == View.VISIBLE) {
                setSoftInputMode(SOFT_KEYBOARD_MODE_NO_PANET);
                inputManager.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
                setSoftInputMode(SOFT_KEYBOARD_MODE_HAS_PANET);
                isShowPanelView = true;
                isShowEmoView = false;
            } else if (mMorePanelView.getVisibility() == GONE) {
                if (mEdit.hasFocus()){
                    mEdit.clearFocus();
                }
                inputManager.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
                mMorePanelView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mEmoGridView.setVisibility(GONE);
                        isShowEmoView = false;

                        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                        mMorePanelView.setVisibility(View.VISIBLE);
                        isShowPanelView = true;
                    }
                }, 35);
            }
        } else {
            if (isShowSoftKeyBoard) {
                if (mEdit.hasFocus()) {
                    mEdit.clearFocus();
                }
                setSoftInputMode(SOFT_KEYBOARD_MODE_NO_PANET);
                inputManager.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
                setSoftInputMode(SOFT_KEYBOARD_MODE_HAS_PANET);
                isShowPanelView = true;
                isShowSoftKeyBoard = false;
            } else {
                setSoftInputMode(SOFT_KEYBOARD_MODE_NO_PANET);
                mEdit.requestFocus();
                onTextChangedAction(mEdit.getText());
            }
        }
    }

    private void showEmoje() {
        isPannelDown = false;
        if (!isShowEmoView) {
            if (mEmoGridView.getVisibility() == View.VISIBLE) {
                isShowSoftKeyBoard = false;
                if (mEdit.hasFocus()){
                    mEdit.clearFocus();
                }
                setSoftInputMode(SOFT_KEYBOARD_MODE_NO_PANET);
                inputManager.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
                setSoftInputMode(SOFT_KEYBOARD_MODE_HAS_PANET);

                isShowEmoView = true;
            } else if (mEmoGridView.getVisibility() == GONE) {
                if (mEdit.hasFocus()){
                    mEdit.clearFocus();
                }

                inputManager.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
                mEmoGridView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMorePanelView.setVisibility(GONE);
                        isShowPanelView = false;

                        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                        mEmoGridView.setVisibility(View.VISIBLE);
                        isShowEmoView = true;
                    }
                }, 35);
            }
        } else {
            if (isShowSoftKeyBoard) {
                if (mEdit.hasFocus()){
                    mEdit.clearFocus();
                }
                setSoftInputMode(SOFT_KEYBOARD_MODE_NO_PANET);
                inputManager.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
                setSoftInputMode(SOFT_KEYBOARD_MODE_HAS_PANET);
                isShowEmoView = true;
                isShowSoftKeyBoard = false;
            } else {
                mEdit.requestFocus();
                onTextChangedAction(mEdit.getText());
            }
        }

    }

    private void showKeyBoard() {
        setSoftInputMode(SOFT_KEYBOARD_MODE_HAS_PANET);
        mEdit.setVisibility(View.VISIBLE);
        onTextChangedAction(mEdit.getText());
        mEdit.requestFocus();
        inputManager.toggleSoftInputFromWindow(mEdit.getWindowToken(), 0, 0);
    }


    @Override
    public void onBackspace() {
        mEdit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
    }

    @Override
    public void onEmojiSelected(String connName) {
        try {
            CharSequence charSequence = EmoticonParserHelper.getInstance()
                    .emoCharsequence(getContext(), mEdit.getText().append(connName));
            mEdit.setText(charSequence);
            mEdit.setSelection(mEdit.getText().length());
        } catch (Exception e) {
        }
    }

    @Override
    public void onGifSelected(EmoticonEntityItem item) {

    }

    @Override
    public void onImageSelected(final EmoticonEntityItem item) {
    }


    private void initSoftInput() {

        setSoftInputMode(SOFT_KEYBOARD_MODE_NO_PANET);
        inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        mInputMethodReceiver = new SwitchInputMethodReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.INPUT_METHOD_CHANGED");
        getContext().registerReceiver(mInputMethodReceiver, filter);
        try {
            mCurrentInputMethod = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            keyboardHeight = SPreferences.getIntegerExtra(getContext(), KEYBOARD_SP_NAME, mCurrentInputMethod);

            if (keyboardHeight > 0) {
                LayoutParams paramEmoLayout = (LayoutParams) mEmoGridView.getLayoutParams();
                paramEmoLayout.height = keyboardHeight;
                mEmoGridView.setLayoutParams(paramEmoLayout);
                LayoutParams paramAddMoreLayout = (LayoutParams) mMorePanelView.getLayoutParams();
                paramAddMoreLayout.height = keyboardHeight;
                mMorePanelView.setLayoutParams(paramAddMoreLayout);
            }
        }
    }

    private class SwitchInputMethodReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.INPUT_METHOD_CHANGED")) {
                try {
                    mCurrentInputMethod = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null == mCurrentInputMethod) {
                        return;
                    }
                    SPreferences.saveStringExtra(context, KEYBOARD_SP_NAME, DEFAULT_INPUT_METHOD, mCurrentInputMethod);
                    int height = SPreferences.getIntegerExtra(context, KEYBOARD_SP_NAME, mCurrentInputMethod);
                    if (keyboardHeight != height && height>0) {
                        keyboardHeight = height;
                        mEmoGridView.setVisibility(GONE);
                        mMorePanelView.setVisibility(GONE);
                        mEdit.requestFocus();

                        if (keyboardHeight != 0 && mEmoGridView.getLayoutParams().height != keyboardHeight) {
                            LayoutParams params = (LayoutParams) mEmoGridView.getLayoutParams();
                            params.height = keyboardHeight;
                            mEmoGridView.setLayoutParams(params);
                        }
                        if (keyboardHeight != 0 && mMorePanelView.getLayoutParams().height != keyboardHeight) {
                            LayoutParams paramAddMoreLayout = (LayoutParams) mMorePanelView.getLayoutParams();
                            paramAddMoreLayout.height = keyboardHeight;
                            mMorePanelView.setLayoutParams(paramAddMoreLayout);
                        }

                    } else {
                        mEdit.requestFocus();
                    }
                }
                mEmoGridView.setVisibility(View.GONE);
                mMorePanelView.setVisibility(GONE);
                isShowEmoView = false;
                isShowPanelView = false;
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        }
    }



    private BaseBottomBarActivity homeViewAT;

    public void setHomeView(BaseBottomBarActivity view){
        this.homeViewAT =  view;
        this.mContext = view;

        if (view instanceof BottomBarOnClickListener) {
            bottomBarOnClickListener =  view;
        }
    }

    public BaseBottomBarActivity getHomeView(){
        return homeViewAT;
    }

    public void setKeyboardPannel(int rootBottom, int bottom) {
        keyboardHeight = rootBottom - bottom;
        SPreferences.saveIntegerExtra(getContext(), KEYBOARD_SP_NAME, mCurrentInputMethod, keyboardHeight);
        LayoutParams emoParams = (LayoutParams) mEmoGridView.getLayoutParams();
        emoParams.height = keyboardHeight;
        mEmoGridView.setLayoutParams(emoParams);
        LayoutParams paramAddMoreLayout = (LayoutParams) mMorePanelView.getLayoutParams();
        paramAddMoreLayout.height = keyboardHeight;
        mMorePanelView.setLayoutParams(paramAddMoreLayout);
    }

    public void enableInputView(boolean enabled) {
        mSendBtn.setEnabled(enabled);
        mEdit.setEnabled(enabled);
        mAddMoreBtn.setEnabled(enabled);
        mAddEmoBtn.setEnabled(enabled);
    }

    public boolean isBottomPannel() {
        if (mEmoGridView.getVisibility() == View.VISIBLE || mMorePanelView.getVisibility() == VISIBLE) {
            isShowPanelView = false;
            isShowEmoView = false;

            setSoftInputMode(SOFT_KEYBOARD_MODE_NO_PANET);
            inputManager.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
            mEdit.clearFocus();
            mEmoGridView.setVisibility(GONE);
            mMorePanelView.setVisibility(GONE);
            return true;
        }
        return false;
    }

    public void pannelBottomDown() {
        if (!isPannelDown) {
            pannelDown();
        }
    }
    private void pannelDown() {
        mEdit.clearFocus();
        setSoftInputMode(SOFT_KEYBOARD_MODE_NO_PANET);
        mEmoGridView.setVisibility(GONE);
        mMorePanelView.setVisibility(GONE);
        inputManager.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
        setSoftInputMode(SOFT_KEYBOARD_MODE_HAS_PANET);
        isPannelDown = true;
        isShowEmoView = false;
        isShowPanelView = false;
        isShowSoftKeyBoard = false;
    }

    public void dismissShortCutPopuWidow() {
        if (mShortCutPopupWindow != null && mShortCutPopupWindow.isShowing()) {
            mShortCutPopupWindow.dismiss();
            mShortCutPopupWindow = null;
        }
    }

    public void recycleEmoji() {
        if (mEmoGridView != null) {
            mEmoGridView.recycleEmoji();
        }
    }

    public void unregisterKeyBoardReceiver() {
        if (mInputMethodReceiver != null) {
            getContext().unregisterReceiver(mInputMethodReceiver);
        }
    }

    public void initSoftInputMethod() {
        initSoftInput();
    }

}
