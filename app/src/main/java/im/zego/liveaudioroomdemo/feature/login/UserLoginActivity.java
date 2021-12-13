package im.zego.liveaudioroomdemo.feature.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.json.JSONException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.zego.liveaudioroom.ZegoLiveAudioRoom;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomUserInfo;
import im.zego.liveaudioroom.util.TokenServerAssistant;
import im.zego.liveaudioroomdemo.KeyCenter;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.BaseActivity;
import im.zego.liveaudioroomdemo.helper.PermissionHelper;

public class UserLoginActivity extends BaseActivity {
    private EditText etUserId;
    private EditText etUserName;
    private Button btnLogin;

    // login info
    private String userID;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        initUI();
        PermissionHelper.requestRecordAudio(this, null);
    }

    private void initUI() {
        etUserId = findViewById(R.id.et_user_id);
        etUserName = findViewById(R.id.et_user_name);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            userID = etUserId.getText().toString();
            userName = etUserName.getText().toString();
            if (TextUtils.isEmpty(userName)) {
                userName = userID;
            }
            String regEx = "^[a-zA-Z\\d]+$";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(etUserId.getText().toString());
            if (!m.matches()) {
                ToastUtils.showShort(R.string.toast_user_id_error);
                return;
            }

            ZegoLiveAudioRoomUserInfo user = new ZegoLiveAudioRoomUserInfo();
            if (!(TextUtils.isEmpty(userID))) {
                user.setUserID(userID);
                user.setUserName(userName);
                try {
                    // Call Chat Room SDK
                    ZegoLiveAudioRoom.getInstance().login(user, TokenServerAssistant.generateToken(KeyCenter.appID(), userID, KeyCenter.appZIMServerSecret(), 60 * 60 * 24).data, error -> {
                        if (error == ZegoLiveAudioRoomErrorCode.SUCCESS) {
                            RoomLoginActivity.startActivity(UserLoginActivity.this);
                        } else {
                            ToastUtils.showShort(StringUtils.getString(R.string.toast_login_fail, error.getValue()));
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                ToastUtils.showShort(StringUtils.getString(R.string.toast_userid_login_fail));
            }
        });

        etUserId.setText(DeviceUtils.getManufacturer() + (int) (100 + Math.random() * 900));
    }
}