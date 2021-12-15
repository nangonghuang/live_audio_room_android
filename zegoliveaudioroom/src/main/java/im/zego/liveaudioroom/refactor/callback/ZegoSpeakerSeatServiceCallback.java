package im.zego.liveaudioroom.refactor.callback;

import im.zego.liveaudioroom.refactor.model.ZegoSpeakerSeatModel;

/**
 * notify speaker seat update.
 */
public interface ZegoSpeakerSeatServiceCallback {

    void onSpeakerSeatUpdate(ZegoSpeakerSeatModel speakerSeatModel);
}
