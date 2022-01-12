package im.zego.liveaudioroomdemo.feature.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.json.JSONException;

import java.util.List;

import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.constants.ZegoRoomErrorCode;
import im.zego.liveaudioroom.listener.ZegoUserServiceListener;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.liveaudioroom.util.TokenServerAssistant;
import im.zego.liveaudioroom.util.ZegoRTCServerAssistant;
import im.zego.liveaudioroomdemo.App;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.BaseActivity;
import im.zego.liveaudioroomdemo.feature.room.LiveAudioRoomActivity;
import im.zego.liveaudioroomdemo.feature.room.dialog.CreateRoomDialog;
import im.zego.liveaudioroomdemo.feature.settings.SettingsActivity;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMErrorCode;

public class RoomLoginActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_ROOM_ENDED = 0x01;

    private EditText etRoomID;
    private TextView btnSettings;
    private Button btnCreate;
    private Button btnJoin;
    private static final String TAG = "RoomLoginActivity";
    private CreateRoomDialog createRoomDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_login);
        initUI();

        /**
         * Listen the connectionStateChanged Event whether myself be kick out
         */
        ZegoRoomManager.getInstance().userService.setListener(new ZegoUserServiceListener() {
            @Override
            public void onRoomUserJoin(List<ZegoUserInfo> userList) {

            }

            @Override
            public void onRoomUserLeave(List<ZegoUserInfo> userList) {

            }

            @Override
            public void onReceiveTakeSeatInvitation() {

            }

            @Override
            public void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event) {
                if (state == ZIMConnectionState.DISCONNECTED && event == ZIMConnectionEvent.KICKED_OUT) {
                    ToastUtils.showShort(R.string.toast_kickout_error);
                    ActivityUtils.finishToActivity(UserLoginActivity.class, false);
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
                App app = (App) getApplication();
                long appID = app.getAppID();
                String appSecret = app.getServerSecret();
                String token = TokenServerAssistant
                        .generateToken(appID, selfUser.getUserID(), appSecret, 60 * 60 * 24).data;
                /**
                 * Join an existed room with roomID and generate token
                 */
                ZegoRoomManager.getInstance().roomService.joinRoom(roomID, token, errorCode -> {
                    if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                        LiveAudioRoomActivity.startActivityForResult(RoomLoginActivity.this, REQUEST_CODE_ROOM_ENDED);
                    } else if (errorCode == ZIMErrorCode.ROOM_NOT_EXIST.value()) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_ROOM_ENDED) {
                showRoomEndedDialog();
            }
        }
    }

    private void showRoomEndedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(StringUtils.getString(R.string.dialog_tips_title))
                .setMessage(StringUtils.getString(R.string.toast_room_has_destroyed))
                .setPositiveButton(StringUtils.getString(R.string.dialog_confirm), (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void showCreateDialog() {
        if (createRoomDialog == null) {
            createRoomDialog = new CreateRoomDialog(this, new CreateRoomDialog.IDialogListener() {
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

                    if ((!TextUtils.isEmpty(roomID)) && (!TextUtils.isEmpty(roomName))) {
                        ZegoUserInfo selfUser = ZegoRoomManager.getInstance().userService.localUserInfo;
                        ZegoRTCServerAssistant.Privileges privileges = new ZegoRTCServerAssistant.Privileges();
                        privileges.canLoginRoom = true;
                        privileges.canPublishStream = true;
                        App app = (App) getApplication();
                        long appID = app.getAppID();
                        String appSign = app.getAppSign();
                        String token = ZegoRTCServerAssistant
                                .generateToken(appID, roomID, selfUser.getUserID(), privileges, appSign, 660).data;
                        /**
                         * create a room with room ID, room name and generate token, then join it.
                         */
                        ZegoRoomManager.getInstance().roomService.createRoom(roomID, roomName, token, errorCode -> {
                            dialog.dismiss();
                            if (errorCode == ZIMErrorCode.SUCCESS.value()) {
                                LiveAudioRoomActivity.startActivity(RoomLoginActivity.this);
                                ToastUtils.showShort(StringUtils.getString(R.string.toast_create_room_success));
                            } else if (errorCode == ZIMErrorCode.CREATE_EXIST_ROOM.value()) {
                                ToastUtils.showShort(R.string.toast_room_existed);
                            } else {
                                ToastUtils.showShort(StringUtils.getString(R.string.toast_create_room_fail, errorCode));
                            }
                        });
                    }
                }
            });
            createRoomDialog.setOnDismissListener(it -> KeyboardUtils.hideSoftInput(RoomLoginActivity.this));
        }
        if (!createRoomDialog.isShowing()) {
            createRoomDialog.show();
        }
    }

    /**
     * When this activity destroyed or on back pressed
     * We need logout
     */
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