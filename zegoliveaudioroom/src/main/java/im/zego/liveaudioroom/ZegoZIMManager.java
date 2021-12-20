package im.zego.liveaudioroom;

import android.app.Application;

import im.zego.zim.ZIM;

/**
 * Created by rocket_wang on 2021/12/14.
 */
public class ZegoZIMManager {

    private static volatile ZegoZIMManager singleton = null;

    private ZegoZIMManager() {
    }

    public static ZegoZIMManager getInstance() {
        if (singleton == null) {
            synchronized (ZegoZIMManager.class) {
                if (singleton == null) {
                    singleton = new ZegoZIMManager();
                }
            }
        }
        return singleton;
    }

    public ZIM zim;

    public void createZIM(long appID, Application application) {
        zim = ZIM.create(appID, application);
    }

    public void destroyZIM() {
        if (zim != null) {
            zim.destroy();
        }
    }
}
