package im.zego.liveaudioroomdemo.feature.login;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.json.JSONException;

import im.zego.liveaudioroom.refactor.ZegoRoomManager;
import im.zego.liveaudioroom.refactor.constants.ZegoRoomErrorCode;
import im.zego.liveaudioroom.refactor.listener.ZegoRoomServiceListener;
import im.zego.liveaudioroom.refactor.model.ZegoRoomInfo;
import im.zego.liveaudioroom.refactor.model.ZegoUserInfo;
import im.zego.liveaudioroom.util.TokenServerAssistant;
import im.zego.liveaudioroom.util.ZegoRTCServerAssistant;
import im.zego.liveaudioroomdemo.KeyCenter;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.BaseActivity;
import im.zego.liveaudioroomdemo.feature.room.LiveAudioRoomActivity;
import im.zego.liveaudioroomdemo.feature.room.dialog.CreateRoomDialog;
import im.zego.liveaudioroomdemo.feature.settings.SettingsActivity;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;

public class RoomLoginActivity extends BaseActivity implements View.OnClickListener {
    private EditText etRoomID;
    private TextView btnSettings;
    private Button btnCreate;
    private Button btnJoin;
    private static final String TAG = "RoomLoginActivity";

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, RoomLoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_login);
        initUI();

        ZegoRoomManager.getInstance().roomService.setListener(new ZegoRoomServiceListener() {
            @Override
            public void onReceiveRoomInfoUpdate(ZegoRoomInfo roomInfo) {

            }

            @Override
            public void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event) {
                Log.d(TAG, "onConnectionStateChanged() called with: state = [" + state + "], event = [" + event + "]");
                if (state == ZIMConnectionState.DISCONNECTED && event == ZIMConnectionEvent.KICKED_OUT) {
                    ToastUtils.showShort(R.string.toast_kickout_error);
                    ActivityUtils.startActivity(RoomLoginActivity.this, UserLoginActivity.class);
                }
            }
        });
    }

    private void initUI() {
        etRoomID = findViewById(R.id.et_room_id);
        btnSettings = findViewById(R.id.btn_settings);
        btnCreate = findViewById(R.id.btn_create);
        btnJoin = findViewById(R.id.btn_join);
        btnSettings.setOnClickListener(this);
        btnCreate.setOnClickListener(this);
        btnJoin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_settings) {
            SettingsActivity.startActivity(this);
        } else if (v.getId() == R.id.btn_create) {
            showCreateDialog();
        } else if (v.getId() == R.id.btn_join) {
            String roomID = etRoomID.getText().toString();

            if (TextUtils.isEmpty(roomID)) {
                ToastUtils.showShort(StringUtils.getString(R.string.toast_room_id_enter_error));
                return;
            }

            try {
                ZegoUserInfo selfUser = ZegoRoomManager.getInstance().userService.localUserInfo;
                String token = TokenServerAssistant.generateToken(KeyCenter.appID(), selfUser.getUserID(), KeyCenter.appZIMServerSecret(), 60 * 60 * 24).data;
                ZegoRoomManager.getInstance().roomService.joinRoom(roomID, token, errorCode -> {
                    if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                        LiveAudioRoomActivity.startActivity(RoomLoginActivity.this);
                    } else if (errorCode == ZegoRoomErrorCode.ROOM_NOT_FOUND) {
                        ToastUtils.showShort(StringUtils.getString(R.string.toast_room_not_exist_fail));
                    } else {
                        ToastUtils.showShort(StringUtils.getString(R.string.toast_join_room_fail, errorCode));
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showCreateDialog() {
        CreateRoomDialog dialog = new CreateRoomDialog(this, new CreateRoomDialog.IDialogListener() {
            @Override
            public void onCancelClick(Dialog dialog) {
                dialog.dismiss();
            }

            @Override
            public void onCreateClick(Dialog dialog, String roomID, String roomName) {
                if (TextUtils.isEmpty(roomID)) {
                    ToastUtils.showShort(StringUtils.getString(R.string.toast_room_id_enter_error));
                    return;
                }

                if (TextUtils.isEmpty(roomName)) {
                    ToastUtils.showShort(StringUtils.getString(R.string.toast_room_name_error));
                    return;
                }

                ZegoRTCServerAssistant.Privileges privileges = new ZegoRTCServerAssistant.Privileges();
                privileges.canLoginRoom = true;
                privileges.canPublishStream = true;

                if ((!TextUtils.isEmpty(roomID)) && (!TextUtils.isEmpty(roomName))) {
                    ZegoUserInfo selfUser = ZegoRoomManager.getInstance().userService.localUserInfo;
                    String token = ZegoRTCServerAssistant.generateToken(KeyCenter.appID(), roomID, selfUser.getUserID(), privileges, KeyCenter.appExpressSign(), 660).data;
                    ZegoRoomManager.getInstance().roomService.joinRoom(roomID, token, errorCode -> {
                        dialog.dismiss();
                        if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                            LiveAudioRoomActivity.startActivity(RoomLoginActivity.this);
                            ToastUtils.showShort(StringUtils.getString(R.string.toast_create_room_success));
                        } else {
                            ToastUtils.showShort(StringUtils.getString(R.string.toast_create_room_fail, errorCode));
                        }
                    });
                }
            }
        });
        dialog.setOnDismissListener(it -> KeyboardUtils.hideSoftInput(RoomLoginActivity.this));
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ZegoRoomManager.getInstance().userService.logout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoRoomManager.getInstance().userService.logout();
    }
}