package im.zego.liveaudioroomdemo.feature.room.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import im.zego.liveaudioroomdemo.R;

public class LoadingDialog extends Dialog {

    private TextView content;

    public LoadingDialog(@NonNull Context context) {
        super(context);
        initDialog(context);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        initDialog(context);
    }

    private void initDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading_layout, null);
        content = view.findViewById(R.id.content);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        setContentView(view);
        getWindow().setDimAmount(0.1f);
    }

    public void updateText(String text) {
        content.setText(text);
        if (text.isEmpty()) {
            content.setVisibility(View.GONE);
        } else {
            content.setVisibility(View.VISIBLE);
        }
    }

    public void updateText(@StringRes int text) {
        content.setText(text);
        if (content.getText().toString().isEmpty()) {
            content.setVisibility(View.GONE);
        } else {
            content.setVisibility(View.VISIBLE);
        }
    }
}
