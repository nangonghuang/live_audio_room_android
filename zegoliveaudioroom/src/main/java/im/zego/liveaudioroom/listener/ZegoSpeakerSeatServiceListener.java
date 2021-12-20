package im.zego.liveaudioroom.listener;

import im.zego.liveaudioroom.model.ZegoSpeakerSeatModel;

/**
 * notify speaker seat update.
 */
public interface ZegoSpeakerSeatServiceListener {

    void onSpeakerSeatUpdate(ZegoSpeakerSeatModel speakerSeatModel);
}
