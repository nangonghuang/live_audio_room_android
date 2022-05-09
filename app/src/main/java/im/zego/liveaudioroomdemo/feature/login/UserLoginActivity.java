package im.zego.liveaudioroomdemo.feature.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.constants.ZegoRoomErrorCode;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.BaseActivity;
import im.zego.liveaudioroomdemo.helper.PermissionHelper;
import im.zego.liveaudioroomdemo.token.ZegoTokenCallback;
import im.zego.liveaudioroomdemo.token.ZegoTokenManager;

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
            ZegoUserInfo user = new ZegoUserInfo();
            if (!(TextUtils.isEmpty(userID))) {
                String regEx = "^[a-zA-Z\\d]+$";
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(userID);
                if (!m.matches()) {
                    ToastUtils.showShort(R.string.toast_user_id_error);
                    return;
                }
                user.setUserID(userID);
                user.setUserName(userName);

                ZegoTokenManager.getInstance().getToken(userID, new ZegoTokenCallback() {
                    @Override
                    public void onTokenCallback(int errorCode1, @Nullable String token) {
                        /**
                         * Login room with user ID, user name and generate token
                         */
                        ZegoRoomManager.getInstance().userService.login(user, token, errorCode -> {
                            if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                                ActivityUtils.startActivity(RoomLoginActivity.class);
                            } else {
                                ToastUtils.showShort(StringUtils.getString(R.string.toast_login_fail, errorCode));
                            }
                        });
                    }
                });
            } else {
                ToastUtils.showShort(StringUtils.getString(R.string.toast_userid_login_fail));
            }
        });

        etUserId.setText(DeviceUtils.getManufacturer() + (int) (100 + Math.random() * 900));
    }
}