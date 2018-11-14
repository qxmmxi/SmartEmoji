package widgets;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;


/**
 * created by shonary on 18/11/2
 * email： xiaonaxi.mail@gmail.com
 */
public class CustomEditView extends android.support.v7.widget.AppCompatEditText {
    private static final int ID_PASTE = android.R.id.paste;

    public CustomEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        if (id == ID_PASTE) {
            try {
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    String value = clipboard.getText().toString();
                    Editable edit = getEditableText();
                    int startIndex = this.getSelectionStart();
                    if (startIndex < 0 || startIndex >= edit.length()) {
                        edit.append(EmoticonParserHelper.getInstance().emoCharsequence(getContext(),value));
                    } else {
                        edit.insert(startIndex, EmoticonParserHelper.getInstance().emoCharsequence(getContext(),value));
                    }
                } else {
                    android.text.ClipboardManager clipboard =
                            (android.text.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    String value = clipboard.getText().toString();
                    Editable edit = getEditableText();
                    int startIndex = this.getSelectionStart();
                    if (startIndex < 0 || startIndex >= edit.length()) {
                        edit.append(EmoticonParserHelper.getInstance().emoCharsequence(getContext(),value));
                    } else {
                        edit.insert(startIndex, EmoticonParserHelper.getInstance().emoCharsequence(getContext(),value));
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onTextContextMenuItem(id);
    }

}