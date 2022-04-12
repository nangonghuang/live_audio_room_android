package im.zego.liveaudioroomdemo;

import android.app.Application;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroomdemo.helper.AuthInfoManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        LogUtils.getConfig().setLogHeadSwitch(false);
        LogUtils.getConfig().setBorderSwitch(false);

        AuthInfoManager.getInstance().init(this);
        long appID = AuthInfoManager.getInstance().getAppID();
        ZegoRoomManager.getInstance().init(appID, this);
    }
}