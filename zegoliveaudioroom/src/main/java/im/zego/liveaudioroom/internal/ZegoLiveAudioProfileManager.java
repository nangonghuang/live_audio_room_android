package im.zego.liveaudioroom.internal;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.entity.ZegoAudioConfig;

public class ZegoLiveAudioProfileManager {
    ZegoExpressEngine zegoExpressEngine;

    public ZegoLiveAudioProfileManager() {
    }

    public void muteMicrophone(boolean isMuted) {
        zegoExpressEngine.muteMicrophone(isMuted);
    }

    public void muteSpeaker(boolean isMuted) {
        zegoExpressEngine.muteSpeaker(isMuted);
    }

    public void setExpressEngine(ZegoExpressEngine zegoExpressEngine) {
        this.zegoExpressEngine = zegoExpressEngine;
        ZegoAudioConfig config = new ZegoAudioConfig();
        this.zegoExpressEngine.setAudioConfig(config);
    }

    public static int getNewLocalSeq(int maxLocalSeq, int oldClientSeq, int newClientSeq, int oldConvSid, int newConvSid) {
        if (newConvSid == oldConvSid) {
            if (oldClientSeq > newClientSeq) {
                return maxLocalSeq - (oldClientSeq - newClientSeq);
            }
            return (newClientSeq - oldClientSeq) + maxLocalSeq;
        } else {
            return newClientSeq + maxLocalSeq;
        }
    }
}
