package widgets;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.smartemoji.R;


/**
 * created by shonary on 18/10/25
 * email： xiaonaxi.mail@gmail.com
 */
public class CustomToast extends Toast {
    Context context = null;
    private static CustomToast mToast = null;


    public  static CustomToast getInstance(Context ctx){
        if (null == mToast){
            mToast = new CustomToast(ctx);
        }
        return mToast;
    }
    /**
     * @param ctx
     */
    private CustomToast(Context ctx) {
        super(ctx);
        this.context=ctx;
    }

    public void show(String text) {
        try {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.toast_layout, null);
            TextView textview = view.findViewById(R.id.toast_content);

            if (!TextUtils.isEmpty(text)) {
                textview.setText(text);
            }
            mToast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.CENTER, 0, 0);
            mToast.setDuration(Toast.LENGTH_SHORT);

            mToast.setView(view);
            mToast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
