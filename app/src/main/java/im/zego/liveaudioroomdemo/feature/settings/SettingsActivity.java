package im.zego.liveaudioroomdemo.feature.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;

import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.constants.ZegoRoomErrorCode;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.BaseActivity;
import im.zego.liveaudioroomdemo.feature.login.UserLoginActivity;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zim.ZIM;

public class SettingsActivity extends BaseActivity {

    private ImageView mIvBack;
    private TextView mTvRtcSdkVersion;
    private TextView mTvZimSdkVersion;
    private TextView mTvLogout;
    private View mShareLog;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
        initData();
        initListener();
    }

    protected void initView() {
        mIvBack = findViewById(R.id.iv_logout);
        mTvRtcSdkVersion = findViewById(R.id.tv_rtc_sdk_version);
        mTvZimSdkVersion = findViewById(R.id.tv_zim_sdk_version);
        mTvLogout = findViewById(R.id.tv_logout);
        mShareLog = findViewById(R.id.layout_share_log);
    }

    protected void initData() {
        mTvRtcSdkVersion.setText(ZegoExpressEngine.getVersion());
        mTvZimSdkVersion.setText(ZIM.getVersion());
    }

    protected void initListener() {
        mIvBack.setOnClickListener(v -> finish());
        mTvLogout.setOnClickListener(v -> logout());
        mShareLog.setOnClickListener(v -> ZegoRoomManager.getInstance().uploadLog(errorCode -> {
            if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                ToastUtils.showShort(R.string.toast_upload_log_success);
            } else {
                ToastUtils.showShort(R.string.toast_upload_log_fail, errorCode);
            }
        }));
    }

    private void logout() {
        ZegoRoomManager.getInstance().userService.logout();
        ActivityUtils.finishToActivity(UserLoginActivity.class,false);
    }
}