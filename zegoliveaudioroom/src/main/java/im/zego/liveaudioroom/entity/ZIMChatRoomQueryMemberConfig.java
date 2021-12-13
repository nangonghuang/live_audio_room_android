package im.zego.liveaudioroom.entity;

public class ZIMChatRoomQueryMemberConfig {
    String nextFlag;
    int count;

    public ZIMChatRoomQueryMemberConfig() {

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
