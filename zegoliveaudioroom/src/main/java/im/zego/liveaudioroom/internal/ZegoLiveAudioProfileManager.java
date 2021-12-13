package im.zego.liveaudioroom.internal;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.entity.ZegoAudioConfig;

/**
 * 音频管理模块，在这里实现设置麦克风静音，扬声器静音，音浪变化的相关操作；远端麦克风和扬声器的回调等等
 */
public class ZegoLiveAudioProfileManager {
    ZegoExpressEngine zegoExpressEngine;

    public ZegoLiveAudioProfileManager() {
    }

    /**
     * 在这里具体实现管理麦克风和扬声器等等相应方法
     */

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

    public static void main(String[] args) {
        checkConvMsgSeqContinuous(100, 102);
    }

    public static boolean checkConvMsgSeqContinuous(int oldConvMsgSeq, int newConvMsgSeq) {
        return (oldConvMsgSeq + 1) == newConvMsgSeq;
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
