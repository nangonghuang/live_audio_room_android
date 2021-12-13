package im.zego.liveaudioroomdemo.feature.room.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.NonNull;

import im.zego.liveaudioroomdemo.R;


/**
 * Created by rocket_wang on 2021/12/3.
 */
public class ConfirmDialog extends BaseDialog {
    private TextView tvTitle;

    private IDialogListener listener;
    private String title;

    public ConfirmDialog(@NonNull Context context, IDialogListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_confirm;
    }

    @Override
    protected int getGravity() {
        return Gravity.BOTTOM;
    }

    @Override
    protected void initView() {
        super.initView();
        tvTitle = findViewById(R.id.tv_title);
    }

    @Override
    protected void initData() {
        super.initData();
        tvTitle.setText(title);
    }

    @Override
    protected void initListener() {
        super.initListener();
        tvTitle.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmClick(this);
            }
        });
    }

    public void setTitle(String text) {
        title = text;
    }

    public interface IDialogListener {
        void onConfirmClick(Dialog dialog);
    }
}