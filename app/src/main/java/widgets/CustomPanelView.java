package widgets;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.smartemoji.R;

import common.SysConstant;

import static common.SysConstant.CallBack.TAKE_PHOTO_PERMISSION;


/**
 * created by shonary on 18/11/8
 * email： xiaonaxi.mail@gmail.com
 */
public class CustomPanelView extends LinearLayout implements View.OnClickListener {

    public static String mFileName;

    public CustomPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public CustomPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CustomPanelView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rootView = inflater.inflate(R.layout.bottombar_add_other_layout, null);

        View takePhotoBtn = rootView.findViewById(R.id.pick_photo_btn);
        View takeCameraBtn = rootView.findViewById(R.id.take_camera_btn);
        takePhotoBtn.setOnClickListener(this);
        takeCameraBtn.setOnClickListener(this);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(params);
        if(this.getChildCount() == 0){
            addView(rootView);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.pick_photo_btn) {
           gotoPickPhoto();
        } else if (id == R.id.take_camera_btn) {
            checkCameraPermission();
        }
    }

    public void gotoPickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ((Activity)getContext()).startActivityForResult(intent, SysConstant.CallBack.PICK_PICTURE);
    }


    public void gotoTakePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            ((Activity)getContext()).startActivityForResult(takePictureIntent, SysConstant.CallBack.TAKE_PHOTO);
        }


    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) getContext(), Manifest.permission.CAMERA)) {
                CustomToast.getInstance(getContext()).show(getContext().getString(R.string.take_photo_permission_tip));
            } else {
                ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.CAMERA}, TAKE_PHOTO_PERMISSION);
            }
        } else {
            gotoTakePhoto();
        }
    }


}
