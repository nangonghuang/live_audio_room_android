package im.zego.liveaudioroom.listener;

import im.zego.liveaudioroom.model.ZegoSpeakerSeatModel;

/**
 * The listener related to speaker seat status callbacks.
 * <p>Description: Callbacks that triggered when speaker seat status updates.</>
 */
public interface ZegoSpeakerSeatServiceListener {

    /**
     * Callback for the updates on the speaker seat status.
     * <p> Description: The callback will be triggered when the speaker seat is be taken, or user
     * ID, microphone status, volume, network status of a speaker seat changes.</>
     *
     * @param speakerSeatModel refers to the updated speaker seat info.
     */
    void onSpeakerSeatUpdate(ZegoSpeakerSeatModel speakerSeatModel);
}
