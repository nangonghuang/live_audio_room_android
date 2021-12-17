package im.zego.liveaudioroomdemo;

import android.app.Application;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

import im.zego.liveaudioroom.refactor.ZegoRoomManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        LogUtils.getConfig().setLogHeadSwitch(false);
        LogUtils.getConfig().setBorderSwitch(false);
        // init LiveAudioRoom SDK
        ZegoRoomManager.getInstance().init(KeyCenter.appID(), KeyCenter.appExpressSign(), this);
    }
}