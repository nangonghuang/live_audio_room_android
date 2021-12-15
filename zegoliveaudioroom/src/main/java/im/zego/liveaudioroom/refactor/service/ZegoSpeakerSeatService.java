package im.zego.liveaudioroom.refactor.service;

import im.zego.liveaudioroom.refactor.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.refactor.callback.ZegoSpeakerSeatServiceCallback;
import im.zego.liveaudioroom.refactor.model.ZegoSpeakerSeatModel;
import java.util.List;

/**
 * user interface to manager speaker seat.
 */
public class ZegoSpeakerSeatService {

    public List<ZegoSpeakerSeatModel> speakerSeatList;
    private ZegoSpeakerSeatServiceCallback speakerSeatServiceCallback;

    public void removeUserFromSeat(String userID, ZegoRoomCallback callback) {

    }

    /**
     * close all unused seat.
     *
     * @param isClose
     * @param callback
     */
    public void closeAllSeat(boolean isClose, ZegoRoomCallback callback) {

    }

    /**
     * close specific speaker seat.
     *
     * @param isClose
     * @param seatIndex
     * @param callback
     */
    public void closeSeat(boolean isClose, int seatIndex, ZegoRoomCallback callback) {

    }

    public void muteMic(boolean isMuted, ZegoRoomCallback callback) {

    }

    public void takeSeat(int seatIndex, ZegoRoomCallback callback) {

    }

    public void leaveSeat(int seatIndex, ZegoRoomCallback callback) {

    }

    public void switchSeat(int toSeatIndex, ZegoRoomCallback callback) {

    }
}
