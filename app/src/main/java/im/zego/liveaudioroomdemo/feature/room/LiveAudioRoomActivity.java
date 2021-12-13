package im.zego.liveaudioroomdemo.feature.room;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.zego.liveaudioroom.ZegoLiveAudioRoom;
import im.zego.liveaudioroom.callback.EnterSeatCallback;
import im.zego.liveaudioroom.callback.KickUserToSeatCallback;
import im.zego.liveaudioroom.callback.LockSeatCallback;
import im.zego.liveaudioroom.callback.SendInvitationStatusCallback;
import im.zego.liveaudioroom.callback.LiveAudioRoomEventHandler;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomEvent;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomInvitationStatus;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomState;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomVoiceStatus;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomQueryMemberConfig;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomText;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomUser;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeat;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeatUpdateInfo;
import im.zego.liveaudioroom.internal.ZegoLiveAudioRoomManager;
import im.zego.liveaudioroom.internal.entity.ZegoLiveAudioRoomInfo;
import im.zego.liveaudioroom.util.TokenServerAssistant;
import im.zego.liveaudioroom.util.ZegoRTCServerAssistant;
import im.zego.liveaudioroomdemo.KeyCenter;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.BaseActivity;
import im.zego.liveaudioroomdemo.feature.room.adapter.MessageListAdapter;
import im.zego.liveaudioroomdemo.feature.room.adapter.SeatListAdapter;
import im.zego.liveaudioroomdemo.feature.room.dialog.IMInputDialog;
import im.zego.liveaudioroomdemo.feature.room.dialog.MemberListDialog;
import im.zego.liveaudioroomdemo.feature.room.dialog.SendGiftDialog;
import im.zego.liveaudioroomdemo.feature.room.dialog.SettingsDialog;
import im.zego.liveaudioroomdemo.feature.room.enums.RoomGift;
import im.zego.liveaudioroomdemo.feature.login.UserLoginActivity;
import im.zego.liveaudioroomdemo.helper.DialogHelper;
import im.zego.liveaudioroomdemo.helper.PermissionHelper;
import im.zego.liveaudioroomdemo.helper.UserInfoHelper;

public class LiveAudioRoomActivity extends BaseActivity {

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, LiveAudioRoomActivity.class);
        context.startActivity(intent);
    }

    private static final String TAG = "LiveAudioRoomActivity";

    private ImageView ivLogout;
    private TextView tvGiftToast;
    private ImageView ivIm;
    private ImageView ivMic;
    private ImageView ivMember;
    private ImageView ivSettings;
    private ImageView ivMore;
    private TextView tvRoomName;
    private TextView tvRoomNum;
    private RecyclerView rvSeatList;
    private RecyclerView rvMessageList;
    private ImageView ivGift;
    private SeatListAdapter seatListAdapter;
    private MessageListAdapter messageListAdapter;

    private SettingsDialog settingsDialog;
    private SendGiftDialog giftDialog;
    private IMInputDialog imInputDialog;
    private MemberListDialog memberListDialog;

    private List<ZegoLiveAudioRoomText> textMessageList = new ArrayList<>();
    private ZegoLiveAudioRoomQueryMemberConfig config = new ZegoLiveAudioRoomQueryMemberConfig();
    private boolean isImMuted = false;
    private Runnable hideGiftTips = () -> {
        tvGiftToast.setText("");
        ((ViewGroup) tvGiftToast.getParent()).setVisibility(View.INVISIBLE);
    };

    @Override
    protected int getStatusBarColor() {
        return R.color.milk_white;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config.setNextFlag("");
        config.setCount(100);

        setContentView(R.layout.activity_chat_room);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);

        initSDCallback();
        initUI();
        setListener();
        updateUI();
    }

    private void initUI() {
        ivLogout = findViewById(R.id.iv_logout);
        tvRoomName = findViewById(R.id.tv_room_name);
        tvRoomNum = findViewById(R.id.tv_room_num);

        rvSeatList = findViewById(R.id.rv_seat_list);
        rvMessageList = findViewById(R.id.rv_message_list);
        tvGiftToast = findViewById(R.id.tv_gift_toast);

        ivIm = findViewById(R.id.iv_im);
        ivIm.setActivated(true);
        ivMic = findViewById(R.id.iv_mic);
        ivMember = findViewById(R.id.iv_member);
        ivGift = findViewById(R.id.iv_gift);
        ivSettings = findViewById(R.id.iv_settings);
        ivMore = findViewById(R.id.iv_more);
    }

    private void setListener() {
        ivLogout.setOnClickListener(v -> this.onBackPressed());
        ivIm.setOnClickListener(v -> {
            if (isImMuted) {
                ToastUtils.showShort(R.string.room_page_bands_send_message);
            } else {
                imInputDialog = new IMInputDialog(this);
                imInputDialog.setOnSendListener(imText -> {
                    ZegoLiveAudioRoom.getInstance().sendRoomMessage(imText, error -> {
                        if (error == ZegoLiveAudioRoomErrorCode.SUCCESS) {
                            final ZegoLiveAudioRoomText text = new ZegoLiveAudioRoomText();
                            text.setContent(imText);
                            text.setFromUserID(getMyUserID());
                            textMessageList.add(text);
                            refreshMessageList();
                        } else {
                            ToastUtils.showShort(R.string.toast_send_message_error, error.getValue());
                        }
                    });
                });
                imInputDialog.show();
            }
        });
        ivMic.setOnClickListener(v -> {
            PermissionHelper.requestRecordAudio(this, isAllGranted -> {
                if (isAllGranted) {
                    boolean bool = ivMic.isSelected();
                    ZegoLiveAudioRoom.getInstance().muteSeat(bool, error -> {
                        if (error == ZegoLiveAudioRoomErrorCode.SUCCESS) {
                            ivMic.setSelected(!bool);
                        }
                    });
                }
            });
        });
        ivMember.setOnClickListener(v -> {
            ZegoLiveAudioRoomQueryMemberConfig config = new ZegoLiveAudioRoomQueryMemberConfig();
            config.setNextFlag("");
            config.setCount(100);
            ZegoLiveAudioRoom.getInstance().queryRoomMember(getRoomID(), config, (userList, nextFlag, errorCode) -> {
                memberListDialog = new MemberListDialog(LiveAudioRoomActivity.this,
                        settingsDialog != null && settingsDialog.isCheckedLockAllSeat,
                        seatListAdapter.getSeatListInRoom(),
                        userList);
                memberListDialog.show();
            });
        });
        ivGift.setOnClickListener(v -> {
            giftDialog = new SendGiftDialog(this);
            giftDialog.setSendGiftListener((sendTo, giftType) -> {
                showGiftTips(sendTo, getMyUserID(), giftType);
            });
            giftDialog.show();
        });
        ivSettings.setOnClickListener(v -> {
            if (settingsDialog == null) {
                settingsDialog = new SettingsDialog(this, seatListAdapter.getSeatListInRoom());
            }
            settingsDialog.show();
        });
        ivMore.setOnClickListener(v -> speakerLeaveSeat());
    }

    private void updateMemberListDialog(ZegoLiveAudioRoomQueryMemberConfig config) {
        if (memberListDialog != null) {
            ZegoLiveAudioRoom.getInstance().queryRoomMember(getRoomID(), config, (userList, nextFlag, errorCode) -> {
                memberListDialog.updateInfo(
                        settingsDialog != null && settingsDialog.isCheckedLockAllSeat,
                        seatListAdapter.getSeatListInRoom(),
                        userList
                );
            });
        }
    }

    private void requestRecordAudio() {
        PermissionHelper.requestRecordAudio(this, isAllGranted -> {
            ivMic.setSelected(isAllGranted);
            if (!isAllGranted) {
                ZegoLiveAudioRoom.getInstance().muteSeat(true, error -> {
                });
            }
        });
    }

    private void updateUI() {
        seatListAdapter = new SeatListAdapter();
        rvSeatList.setAdapter(seatListAdapter);
        rvSeatList.setLayoutManager(new GridLayoutManager(this, 4));
        seatListAdapter.setItemListener(new SeatListAdapter.IChatListItemListener() {
            @Override
            public void onClick(int index) {
                final ZIMSpeakerSeat seat = seatListAdapter.getSeatListInRoom().get(index);

                if (UserInfoHelper.isSelfOwner()) {
                    // is owner
                    if (seat.getStatus() == ZegoLiveAudioRoomVoiceStatus.UNUSED) {
                        DialogHelper.showToastDialog(LiveAudioRoomActivity.this, StringUtils.getString(R.string.room_page_lock_seat), dialog -> {
                            ZegoLiveAudioRoom.getInstance().lockSeat(true, index, new LockSeatCallback() {
                                @Override
                                public void lockSeat(ZegoLiveAudioRoomErrorCode error) {
                                    if (error == ZegoLiveAudioRoomErrorCode.SUCCESS) {
                                        seat.setStatus(ZegoLiveAudioRoomVoiceStatus.LOCKED);
                                        seatListAdapter.updateUserInfo(index, seat);
                                    } else {
                                        ToastUtils.showShort(R.string.toast_lock_seat_fail, error.getValue());
                                    }
                                }
                            });
                        });
                    } else if (seat.getStatus() == ZegoLiveAudioRoomVoiceStatus.LOCKED) {
                        DialogHelper.showToastDialog(LiveAudioRoomActivity.this, StringUtils.getString(R.string.room_page_unlock_seat), dialog -> {
                            ZegoLiveAudioRoom.getInstance().lockSeat(false, index, new LockSeatCallback() {
                                @Override
                                public void lockSeat(ZegoLiveAudioRoomErrorCode error) {
                                    if (error == ZegoLiveAudioRoomErrorCode.SUCCESS) {
                                        seat.setStatus(ZegoLiveAudioRoomVoiceStatus.UNUSED);
                                        seatListAdapter.updateUserInfo(index, seat);
                                    }
                                }
                            });
                        });
                    } else if (seat.getStatus() == ZegoLiveAudioRoomVoiceStatus.USED) {
                        final String userId = seatListAdapter.getSeatListInRoom().get(index).getAttribution().getUser_id();
                        if (!getMyUserID().equals(userId)) {
                            DialogHelper.showToastDialog(LiveAudioRoomActivity.this, StringUtils.getString(R.string.room_page_leave_seat), dialog -> {
                                DialogHelper.showAlertDialog(
                                        LiveAudioRoomActivity.this,
                                        null,
                                        StringUtils.getString(R.string.dialog_warning_leave_seat_message, userId),
                                        StringUtils.getString(R.string.dialog_confirm),
                                        StringUtils.getString(R.string.dialog_cancel),
                                        (alertDialog, which) -> {
                                            ZegoLiveAudioRoom.getInstance().kickUserToSeat(userId, new KickUserToSeatCallback() {
                                                @Override
                                                public void kickUserToSeat(ZegoLiveAudioRoomErrorCode error) {
                                                    if (error != ZegoLiveAudioRoomErrorCode.SUCCESS) {
                                                        ToastUtils.showShort(R.string.toast_kickout_leave_seat_error, userId, error.getValue());
                                                    }
                                                }
                                            });
                                        },
                                        (alertDialog, which) -> alertDialog.cancel()
                                );
                            });
                        }
                    }
                } else {
                    // is visitor
                    if (seat.getStatus() == ZegoLiveAudioRoomVoiceStatus.UNUSED) {
                        boolean hasOnSeat = false;
                        for (ZIMSpeakerSeat seatInfo : seatListAdapter.getSeatListInRoom()) {
                            if (getMyUserID().equals(seatInfo.getAttribution().getUser_id())) {
                                hasOnSeat = true;
                                break;
                            }
                        }
                        if (hasOnSeat) {
                            ZegoLiveAudioRoom.getInstance().switchSeat(index, error -> {
                                if (error != ZegoLiveAudioRoomErrorCode.SUCCESS) {
                                    ToastUtils.showShort(StringUtils.getString(R.string.toast_take_speaker_seat_fail, error));
                                }
                            });
                        } else {
                            DialogHelper.showToastDialog(LiveAudioRoomActivity.this, StringUtils.getString(R.string.room_page_take_seat), dialog -> {
                                ZegoLiveAudioRoom.getInstance().takeSpeakerSeat(index, error -> {
                                    if (error != ZegoLiveAudioRoomErrorCode.SUCCESS) {
                                        ToastUtils.showShort(R.string.toast_take_speaker_seat_fail, error.getValue());
                                    } else {
                                        requestRecordAudio();
                                    }
                                });
                            });
                        }
                    } else if (seat.getStatus() == ZegoLiveAudioRoomVoiceStatus.LOCKED) {
                        ToastUtils.showShort(StringUtils.getString(R.string.the_wheat_position_has_been_locked));
                    } else if (seat.getStatus() == ZegoLiveAudioRoomVoiceStatus.USED) {
                        final String userId = seatListAdapter.getSeatListInRoom().get(index).getAttribution().getUser_id();
                        if (getMyUserID().equals(userId)) {
                            speakerLeaveSeat();
                        }
                    }
                }
            }
        });

        messageListAdapter = new MessageListAdapter(textMessageList);
        rvMessageList.setAdapter(messageListAdapter);
        rvMessageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        if (UserInfoHelper.isSelfOwner()) {
            uiToOwner();
            ZegoLiveAudioRoom.getInstance().takeSpeakerSeat(0, (EnterSeatCallback) error -> {
                requestRecordAudio();
            });
        } else {
            uiToAudience();
        }

        ZegoLiveAudioRoomInfo roomInfo = ZegoLiveAudioRoomManager.getInstance().getRoomInfo();
        tvRoomName.setText(roomInfo.getRoom_Name());
        tvRoomNum.setText(roomInfo.getRoom_id());
    }

    private void speakerLeaveSeat() {
        DialogHelper.showToastDialog(LiveAudioRoomActivity.this, StringUtils.getString(R.string.room_page_leave_seat), dialog -> {
            DialogHelper.showAlertDialog(
                    LiveAudioRoomActivity.this,
                    StringUtils.getString(R.string.room_page_leave_seat),
                    StringUtils.getString(R.string.dialog_sure_to_leave_seat),
                    StringUtils.getString(R.string.dialog_confirm),
                    StringUtils.getString(R.string.dialog_cancel),
                    (alertDialog, which) -> {
                        ZegoLiveAudioRoom.getInstance().leaveSpeakerSeat(error -> {
                            if (error != ZegoLiveAudioRoomErrorCode.SUCCESS) {
                                ToastUtils.showShort(R.string.toast_leave_seat_fail, error.getValue());
                            }
                        });
                    },
                    (alertDialog, which) -> alertDialog.cancel()
            );
        });
    }

    private void initSDCallback() {
        ZegoLiveAudioRoom.getInstance().setZegoLiveAudioRoomEventHandler(new LiveAudioRoomEventHandler() {
            @Override
            public void onRoomStateUpdated(ZegoLiveAudioRoomState state, ZegoLiveAudioRoomEvent event, String roomID) {
                if (state == ZegoLiveAudioRoomState.DISCONNECTED) {
                    ToastUtils.showShort(StringUtils.getString(R.string.toast_disconnect_tips));
                    finish();
                }
            }

            @Override
            public void onRTCTokenWillExpire(String roomID, int remainTimeInSecond) {
                ZegoRTCServerAssistant.Privileges privileges = new ZegoRTCServerAssistant.Privileges();
                privileges.canLoginRoom = true;
                privileges.canPublishStream = true;
                ZegoLiveAudioRoom.getInstance().renewRTCToken(ZegoRTCServerAssistant.generateToken(KeyCenter.appID(), roomID, getMyUserID(), privileges, KeyCenter.appExpressSign(), 660).data);
            }

            @Override
            public void onZIMTokenWillExpire(int time) {
                try {
                    ZegoLiveAudioRoom.getInstance().renewZIMToken(TokenServerAssistant.generateToken(KeyCenter.appID(), getMyUserID(), KeyCenter.appZIMServerSecret(), 660).data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRoomInfoUpdated(ZegoLiveAudioRoomInfo roomInfo) {
                if (roomInfo == null) {
                    ToastUtils.showShort(StringUtils.getString(R.string.toast_room_has_destroyed));
                    finish();
                } else {
                    seatListAdapter.notifyDataSetChanged();
                    tvRoomName.setText(roomInfo.getRoom_Name());
                }
            }

            @Override
            public void onRoomMemberLeft(ArrayList<ZegoLiveAudioRoomUser> userList) {
                for (ZegoLiveAudioRoomUser user : userList) {
                    ZegoLiveAudioRoomText ZegoLiveAudioRoomText = new ZegoLiveAudioRoomText();
                    ZegoLiveAudioRoomText.setContent(StringUtils.getString(R.string.room_page_has_left_the_room, user.getUserName()));
                    textMessageList.add(ZegoLiveAudioRoomText);
                    refreshMessageList();

                }
                seatListAdapter.notifyDataSetChanged();
                updateMemberListDialog(config);
            }

            @Override
            public void onRoomMemberJoined(ArrayList<ZegoLiveAudioRoomUser> userList) {
                for (ZegoLiveAudioRoomUser user : userList) {
                    ZegoLiveAudioRoomText ZegoLiveAudioRoomText = new ZegoLiveAudioRoomText();
                    ZegoLiveAudioRoomText.setContent(StringUtils.getString(R.string.room_page_joined_the_room, user.getUserName()));
                    textMessageList.add(ZegoLiveAudioRoomText);
                    refreshMessageList();
                }
                seatListAdapter.notifyDataSetChanged();
                updateMemberListDialog(config);
            }

            @Override
            public void onReceiveRoomMassage(String message, String fromUserID) {
                ZegoLiveAudioRoomText ZegoLiveAudioRoomText = new ZegoLiveAudioRoomText();
                ZegoLiveAudioRoomText.setContent(message);
                ZegoLiveAudioRoomText.setFromUserID(fromUserID);
                textMessageList.add(ZegoLiveAudioRoomText);
                refreshMessageList();
            }

            @Override
            public void onReceiveGiftMessage(int giftType, String fromUserID) {
                super.onReceiveGiftMessage(giftType, fromUserID);
            }

            @Override
            public void onReceiveGiftBroadcastMessage(List<String> toUSerIDList, int giftType, String fromUserID) {
                if (toUSerIDList != null) {
                    showGiftTips(toUSerIDList, fromUserID, giftType);
                }
            }

            @Override
            public void onReceiveInvitation(String fromUserID) {
                super.onReceiveInvitation(fromUserID);
                DialogHelper.showAlertDialog(
                        LiveAudioRoomActivity.this,
                        StringUtils.getString(R.string.dialog_invition_title),
                        StringUtils.getString(R.string.dialog_invition_descrip),
                        StringUtils.getString(R.string.dialog_accept),
                        StringUtils.getString(R.string.dialog_refuse),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ZegoLiveAudioRoom.getInstance().respondInvitation(ZegoLiveAudioRoomInvitationStatus.ACCEPT, new SendInvitationStatusCallback() {
                                    @Override
                                    public void sendInvitationStatus(ZegoLiveAudioRoomErrorCode errorCode) {
                                        if (errorCode == ZegoLiveAudioRoomErrorCode.SUCCESS) {
                                            List<ZIMSpeakerSeat> zimSpeakerSeats = seatListAdapter.getSeatListInRoom();
                                            for (ZIMSpeakerSeat zimSpeakerSeat : zimSpeakerSeats) {
                                                if (zimSpeakerSeat.getStatus() == ZegoLiveAudioRoomVoiceStatus.UNUSED) {
                                                    ZegoLiveAudioRoom.getInstance().takeSpeakerSeat(zimSpeakerSeat.getAttribution().getIndex(), new EnterSeatCallback() {
                                                        @Override
                                                        public void enterSeat(ZegoLiveAudioRoomErrorCode error) {
                                                            if (error == ZegoLiveAudioRoomErrorCode.SUCCESS) {
                                                                requestRecordAudio();
                                                            }
                                                        }
                                                    });
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                });
                                dialog.cancel();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ZegoLiveAudioRoom.getInstance().respondInvitation(ZegoLiveAudioRoomInvitationStatus.REJECT, new SendInvitationStatusCallback() {
                                    @Override
                                    public void sendInvitationStatus(ZegoLiveAudioRoomErrorCode errorCode) {

                                    }
                                });
                                dialog.cancel();
                            }
                        }
                );
            }

            @Override
            public void onResponseInvitation(ZegoLiveAudioRoomInvitationStatus ZegoLiveAudioRoomInvitationStatus, String fromUserID) {
                super.onResponseInvitation(ZegoLiveAudioRoomInvitationStatus, fromUserID);
            }

            @Override
            public void onMuteAllMessage(boolean isMuted) {
                Log.d(TAG, "onMuteAllMessage() called with: isMuted = [" + isMuted + "]");
                onUserMuted(isMuted);
            }

            @Override
            public void onRoomSpeakerSeatUpdated(ArrayList<ZIMSpeakerSeatUpdateInfo> speakerSeatUpdateInfos) {
                for (ZIMSpeakerSeatUpdateInfo info : speakerSeatUpdateInfos) {
                    ZIMSpeakerSeat speakerSeat = info.getSpeakerSeat();
                    int index = speakerSeat.getAttribution().getIndex();

                    ZegoLiveAudioRoomUser selfUser = ZegoLiveAudioRoomManager.getInstance().getMyUserInfo();
                    if (selfUser.getUserID().equals(info.getSpeakerSeat().getAttribution().getUser_id())) {
                        if (!UserInfoHelper.isSelfOwner()) {
                            switch (info.getSpeakerSeat().getStatus()) {
                                case USED:
                                    uiToSpeaker();
                                    ivMic.setSelected(!info.getSpeakerSeat().getAttribution().isIs_muted());
                                    break;
                                case UNUSED:
                                    uiToAudience();
                                    speakerSeat.getAttribution().setUser_id("");
                                    break;
                            }
                        }
                    }
                    if (!selfUser.getUserID().equals(info.getSpeakerSeat().getAttribution().getUser_id())
                            && info.getSpeakerSeat().getStatus() == ZegoLiveAudioRoomVoiceStatus.UNUSED) {
                        if (settingsDialog != null && settingsDialog.isCheckedLockAllSeat && UserInfoHelper.isSelfOwner()) {
                            ZegoLiveAudioRoom.getInstance().lockSeat(true, info.getSpeakerSeat().getAttribution().getIndex(), error -> {
                                if (error != ZegoLiveAudioRoomErrorCode.SUCCESS) {
                                    ToastUtils.showShort(R.string.toast_lock_seat_fail, error.getValue());
                                }
                            });
                        }
                        speakerSeat.getAttribution().setUser_id("");
                    }
                    seatListAdapter.updateUserInfo(index, speakerSeat);
                }
                updateMemberListDialog(config);
            }

            @Override
            public void OnLocalUserSoundLevelUpdated(float soundLevel) {
                super.OnLocalUserSoundLevelUpdated(soundLevel);
//                LogUtils.dTag(TAG, "OnLocalUserSoundLevelUpdated() called with: soundLevel = [" + soundLevel + "]");
                seatListAdapter.updateSoundWaves(getMyUserID(), soundLevel);
            }

            @Override
            public void OnRemoteUserSoundLevelUpdated(HashMap<String, Float> soundLevel) {
                super.OnRemoteUserSoundLevelUpdated(soundLevel);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    soundLevel.entrySet().forEach(entry -> {
//                        Log.d(TAG, "OnRemoteUserSoundLevelUpdated() called with: " +
//                                "soundLevel = [" + "key=" + entry.getKey() + ", value=" + entry.getValue() + "]");
                        seatListAdapter.updateSoundWaves(entry.getKey(), entry.getValue());
                    });
                }
            }


            @Override
            public void onConnectionStateChanged(ZegoLiveAudioRoomState state, ZegoLiveAudioRoomEvent event, JSONObject extendedData) {
                if (state == ZegoLiveAudioRoomState.DISCONNECTED) {
                    ToastUtils.showShort(StringUtils.getString(R.string.toast_disconnect_tips));
                    ActivityUtils.startActivity(LiveAudioRoomActivity.this, UserLoginActivity.class);
                }
            }
        });
    }

    private void onUserMuted(boolean isMuted) {
        isImMuted = isMuted;
        if (imInputDialog != null) {
            imInputDialog.updateSendButtonState(isMuted);
        }
        ivIm.setActivated(!isMuted);
    }

    private void uiToOwner() {
        ivIm.setVisibility(View.VISIBLE);
        ivMic.setVisibility(View.VISIBLE);
        ivMember.setVisibility(View.VISIBLE);
        ivGift.setVisibility(View.VISIBLE);
        ivSettings.setVisibility(View.VISIBLE);
        ivMore.setVisibility(View.GONE);
    }

    private void uiToSpeaker() {
        ivIm.setVisibility(View.VISIBLE);
        ivMic.setVisibility(View.VISIBLE);
        ivMember.setVisibility(View.GONE);
        ivGift.setVisibility(View.VISIBLE);
        ivSettings.setVisibility(View.GONE);
        ivMore.setVisibility(View.VISIBLE);
    }

    private void uiToAudience() {
        ivIm.setVisibility(View.VISIBLE);
        ivMic.setVisibility(View.GONE);
        ivMember.setVisibility(View.GONE);
        ivGift.setVisibility(View.VISIBLE);
        ivSettings.setVisibility(View.GONE);
        ivMore.setVisibility(View.GONE);
    }

    private boolean isSelfUser(String userId) {
        ZegoLiveAudioRoomUser selfUser = ZegoLiveAudioRoomManager.getInstance().getMyUserInfo();
        return selfUser.getUserID().equals(userId);
    }

    private void refreshMessageList() {
        messageListAdapter.notifyItemInserted(textMessageList.size());
        rvMessageList.scrollToPosition(messageListAdapter.getItemCount() - 1);
    }

    private void showGiftTips(List<String> toUSerIDList, String fromUserID, int type) {
        String giftName = "";
        for (RoomGift value : RoomGift.values()) {
            if (value.getType() == type) {
                giftName = getString(value.getName());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < toUSerIDList.size(); i++) {
            String userID = toUSerIDList.get(i);
            String name = ZegoLiveAudioRoomManager.getInstance().getRoomUserName(userID);
            sb.append(name);
            if(i != toUSerIDList.size() -1){
                sb.append(",");
            }
        }
        String fromUserName = ZegoLiveAudioRoomManager.getInstance().getRoomUserName(fromUserID);
        String giftTips = getString(R.string.room_page_received_gift_tips, sb.toString(), fromUserName);
        SpannableString string = new SpannableString(giftTips);
        ForegroundColorSpan yellowSpan = new ForegroundColorSpan(
                ContextCompat.getColor(this, R.color.yellow)
        );

        int indexOfGiftName = giftTips.indexOf(giftName);
        string.setSpan(yellowSpan, indexOfGiftName,
                indexOfGiftName + giftName.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        ((ViewGroup) tvGiftToast.getParent()).setVisibility(View.VISIBLE);
        tvGiftToast.setText(string);
        tvGiftToast.removeCallbacks(hideGiftTips);
        tvGiftToast.postDelayed(hideGiftTips, 10_000L);
    }

    public void showDialog() {
        DialogHelper.showAlertDialog(LiveAudioRoomActivity.this,
                StringUtils.getString(R.string.room_page_destroy_room),
                StringUtils.getString(R.string.dialog_sure_to_destroy_room),
                StringUtils.getString(R.string.dialog_confirm),
                StringUtils.getString(R.string.dialog_cancel),
                (dialog, which) -> {
                    ToastUtils.showShort(R.string.toast_room_has_destroyed);
                    dialog.dismiss();
                    finish();
                },
                (dialog, which) -> dialog.dismiss()
        );
    }

    private String getRoomID() {
        return ZegoLiveAudioRoomManager.getInstance().getRoomInfo().getRoom_id();
    }

    private String getMyUserID() {
        return ZegoLiveAudioRoomManager.getInstance().getMyUserInfo().getUserID();
    }

    @Override
    public void onBackPressed() {
        if (UserInfoHelper.isSelfOwner()) {
            showDialog();
        } else {
            ZegoLiveAudioRoom.getInstance().leaveRoom(getRoomID(), error -> {
            });
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (settingsDialog != null) {
            settingsDialog = null;
        }
        ZegoLiveAudioRoom.getInstance().setZegoLiveAudioRoomEventHandler(null);
        ZegoLiveAudioRoom.getInstance().leaveRoom(getRoomID(), error -> {

        });
    }
}