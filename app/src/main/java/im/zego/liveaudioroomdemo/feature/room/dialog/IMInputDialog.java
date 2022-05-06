package im.zego.liveaudioroomdemo.feature.room.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.blankj.utilcode.util.KeyboardUtils;
import im.zego.liveaudioroomdemo.R;

public class IMInputDialog extends BaseDialog {

    private EditText imInputEditText;
    private SendListener sendListener;
    private View sendButton;

    public IMInputDialog(@NonNull Context context) {
        super(context);
    }

    public IMInputDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void initView() {
        sendButton = findViewById(R.id.im_input_send);
        sendButton.setOnClickListener(v -> {
            KeyboardUtils.hideSoftInput(imInputEditText);
            dismiss();
            sendListener.onSend(imInputEditText.getText().toString().trim());
        });
        sendButton.setEnabled(false);
        imInputEditText = findViewById(R.id.softinput_edittext);
        imInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sendButton.setEnabled(editable.length() > 0);
            }
        });
        imInputEditText.postDelayed(() -> {
            requestInputWindow(imInputEditText);
        }, 100);
    }

    private static final String TAG = "String";
    @Override
    public void dismiss() {
        super.dismiss();
        hideInputWindow(imInputEditText);
    }

    @Override
    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        super.setOnDismissListener(listener);
    }

    @Override
    protected int getGravity() {
        return Gravity.BOTTOM;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_im_input;
    }

    public void updateSendButtonState(boolean enable) {
        sendButton.setEnabled(enable);
    }

    private void hideInputWindow(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean result = imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void requestInputWindow(View view) {
        boolean result = view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean input = false;
        if (view.isAttachedToWindow()) {
            input = imm.showSoftInput(view, 0);
        }
    }
    public void setOnSendListener(SendListener listener) {
        this.sendListener = listener;
    }

    public interface SendListener {
        void onSend(String imText);
    }
}
