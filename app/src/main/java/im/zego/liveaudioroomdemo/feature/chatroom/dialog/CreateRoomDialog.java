package im.zego.liveaudioroomdemo.feature.chatroom.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.KeyboardUtils;

import im.zego.liveaudioroomdemo.R;

public class CreateRoomDialog extends BaseDialog {
    private EditText mEtRoomId;
    private EditText mEtRoomName;
    private TextView mTvCancel;
    private TextView mTvCreate;

    private IDialogListener listener;

    public CreateRoomDialog(@NonNull Context context, IDialogListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_create_room;
    }

    @Override
    protected void initView() {
        super.initView();
        mEtRoomId = findViewById(R.id.et_room_id);
        mEtRoomName = findViewById(R.id.et_room_name);
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvCreate = findViewById(R.id.tv_create);
    }

    @Override
    protected void initData() {
        super.initData();
        KeyboardUtils.showSoftInput(mEtRoomId);
    }

    @Override
    public void dismiss() {
        KeyboardUtils.hideSoftInput(mEtRoomId);
        super.dismiss();
    }

    @Override
    protected void initListener() {
        super.initListener();
        mTvCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelClick(this);
            }
        });
        mTvCreate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCreateClick(this, mEtRoomId.getText().toString(), mEtRoomName.getText().toString());
            }
        });
    }

    public interface IDialogListener {
        void onCancelClick(Dialog dialog);

        void onCreateClick(Dialog dialog, String roomID, String roomName);
    }
}