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
import im.zego.liveaudioroomdemo.BuildConfig;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.constants.Constants;
import im.zego.liveaudioroomdemo.feature.BaseActivity;
import im.zego.liveaudioroomdemo.feature.login.UserLoginActivity;
import im.zego.liveaudioroomdemo.feature.webview.WebViewActivity;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zim.ZIM;

public class SettingsActivity extends BaseActivity {

    private ImageView ivBack;
    private TextView tvRtcSdkVersion;
    private TextView tvZimSdkVersion;
    private TextView tvAppVersion;
    private View layoutTermsOfService;
    private View layoutPrivacyPolicy;
    private View shareLog;
    private TextView tvLogout;

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
        ivBack = findViewById(R.id.iv_logout);
        tvRtcSdkVersion = findViewById(R.id.tv_rtc_sdk_version);
        tvZimSdkVersion = findViewById(R.id.tv_zim_sdk_version);
        tvAppVersion = findViewById(R.id.tv_app_version);
        layoutTermsOfService = findViewById(R.id.layout_terms_of_service);
        layoutPrivacyPolicy = findViewById(R.id.layout_privacy_policy);
        tvLogout = findViewById(R.id.tv_logout);
        shareLog = findViewById(R.id.layout_share_log);
    }

    protected void initData() {
        tvRtcSdkVersion.setText(ZegoExpressEngine.getVersion());
        tvZimSdkVersion.setText(ZIM.getVersion());
        tvAppVersion.setText(BuildConfig.VERSION_NAME);
    }

    protected void initListener() {
        ivBack.setOnClickListener(v -> finish());
        tvLogout.setOnClickListener(v -> logout());
        layoutTermsOfService.setOnClickListener(v -> WebViewActivity.start(this, Constants.URL_TERMS_OF_SERVICE));
        layoutPrivacyPolicy.setOnClickListener(v -> WebViewActivity.start(this, Constants.URL_PRIVACY_POLICY));
        shareLog.setOnClickListener(v ->
                /**
                 * Upload log to SDK server, let the sdk grow up
                 */
                ZegoRoomManager.getInstance().uploadLog(errorCode -> {
                    if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                        ToastUtils.showShort(R.string.toast_upload_log_success);
                    } else {
                        ToastUtils.showShort(R.string.toast_upload_log_fail, errorCode);
                    }
                }));
    }

    private void logout() {
        ZegoRoomManager.getInstance().userService.logout();
        ActivityUtils.finishToActivity(UserLoginActivity.class, false);
    }
}