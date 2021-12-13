package im.zego.liveaudioroom.entity;

public class ZegoLiveAudioRoomQueryMemberConfig {
    String nextFlag;
    int count;

    public ZegoLiveAudioRoomQueryMemberConfig() {

    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }

    public void setNextFlag(String nextFlag) {
        this.nextFlag = nextFlag;
    }

    public String getNextFlag() {
        return this.nextFlag;
    }
}
