package im.zego.liveaudioroomdemo.feature.room;

import android.content.Context;
import android.content.Intent;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomUser;
import im.zego.liveaudioroom.internal.ZegoLiveAudioRoomManager;
import im.zego.liveaudioroom.refactor.ZegoRoomManager;
import im.zego.liveaudioroom.refactor.constants.ZegoRoomErrorCode;
import im.zego.liveaudioroom.refactor.listener.ZegoRoomServiceListener;
import im.zego.liveaudioroom.refactor.listener.ZegoUserServiceListener;
import im.zego.liveaudioroom.refactor.model.ZegoRoomInfo;
import im.zego.liveaudioroom.refactor.model.ZegoSpeakerSeatModel;
import im.zego.liveaudioroom.refactor.model.ZegoSpeakerSeatStatus;
import im.zego.liveaudioroom.refactor.model.ZegoTextMessage;
import im.zego.liveaudioroom.refactor.model.ZegoUserInfo;
import im.zego.liveaudioroom.refactor.service.ZegoGiftService;
import im.zego.liveaudioroom.refactor.service.ZegoMessageService;
import im.zego.liveaudioroom.refactor.service.ZegoRoomService;
import im.zego.liveaudioroom.refactor.service.ZegoSpeakerSeatService;
import im.zego.liveaudioroom.refactor.service.ZegoUserService;
import im.zego.liveaudioroom.util.TokenServerAssistant;
import im.zego.liveaudioroom.util.ZegoRTCServerAssistant;
import im.zego.liveaudioroomdemo.KeyCenter;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.BaseActivity;
import im.zego.liveaudioroomdemo.feature.login.UserLoginActivity;
import im.zego.liveaudioroomdemo.feature.room.adapter.MessageListAdapter;
import im.zego.liveaudioroomdemo.feature.room.adapter.SeatListAdapter;
import im.zego.liveaudioroomdemo.feature.room.dialog.IMInputDialog;
import im.zego.liveaudioroomdemo.feature.room.dialog.MemberListDialog;
import im.zego.liveaudioroomdemo.feature.room.dialog.SendGiftDialog;
import im.zego.liveaudioroomdemo.feature.room.dialog.SettingsDialog;
import im.zego.liveaudioroomdemo.feature.room.enums.RoomGift;
import im.zego.liveaudioroomdemo.helper.DialogHelper;
import im.zego.liveaudioroomdemo.helper.PermissionHelper;
import im.zego.liveaudioroomdemo.helper.UserInfoHelper;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;

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

    private List<ZegoTextMessage> textMessageList = new ArrayList<>();
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
        setContentView(R.layout.activity_chat_room);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);

        initUI();
        setListener();
        updateUI();
        initSDCallback();
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
                    ZegoMessageService service = ZegoRoomManager.getInstance().messageService;
                    service.sendTextMessage(imText, errorCode -> {
                        if (errorCode == ZegoLiveAudioRoomErrorCode.SUCCESS.getValue()) {
                            final ZegoTextMessage text = new ZegoTextMessage();
                            text.message = imText;
                            text.userID = getMyUserID();
                            textMessageList.add(text);
                            refreshMessageList();
                        } else {
                            ToastUtils.showShort(R.string.toast_send_message_error, errorCode);
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
                    ZegoRoomManager.getInstance().speakerSeatService.muteMic(bool, error -> {
                        if (error == ZegoRoomErrorCode.SUCCESS) {
                            ivMic.setSelected(!bool);
                        }
                    });
                }
            });
        });
        ivMember.setOnClickListener(v -> {
            ZegoSpeakerSeatService seatService = ZegoRoomManager.getInstance().speakerSeatService;
            ZegoUserService userService = ZegoRoomManager.getInstance().userService;

            if (memberListDialog == null) {
                memberListDialog = new MemberListDialog(LiveAudioRoomActivity.this,
                        settingsDialog != null && settingsDialog.isCheckedLockAllSeat,
                        seatService.getSpeakerSeatList(),
                        userService.getUserList());
            } else {
                memberListDialog.updateInfo(
                        settingsDialog != null && settingsDialog.isCheckedLockAllSeat,
                        seatService.getSpeakerSeatList(),
                        userService.getUserList()
                );
            }
            memberListDialog.show();
        });
        ivGift.setOnClickListener(v -> {
            giftDialog = new SendGiftDialog(this);
            giftDialog.setSendGiftListener((sendTo, giftID) -> {
                showGiftTips(sendTo, getMyUserID(), giftID);
            });
            giftDialog.show();
        });
        ivSettings.setOnClickListener(v -> {
            if (settingsDialog == null) {
                ZegoSpeakerSeatService seatService = ZegoRoomManager
                    .getInstance().speakerSeatService;
                settingsDialog = new SettingsDialog(this, seatService.getSpeakerSeatList());
            }
            settingsDialog.show();
        });
        ivMore.setOnClickListener(v -> speakerLeaveSeat());
    }

    private void updateMemberListDialog() {
        if (memberListDialog != null) {
            ZegoSpeakerSeatService seatService = ZegoRoomManager.getInstance().speakerSeatService;
            ZegoUserService userService = ZegoRoomManager.getInstance().userService;
            memberListDialog.updateInfo(
                    settingsDialog != null && settingsDialog.isCheckedLockAllSeat,
                    seatService.getSpeakerSeatList(),
                    userService.getUserList()
            );
        }
    }

    private void requestRecordAudio() {
        PermissionHelper.requestRecordAudio(this, isAllGranted -> {
            ivMic.setSelected(isAllGranted);
            if (!isAllGranted) {
                ZegoRoomManager.getInstance().speakerSeatService.muteMic(true, error -> {
                });
            }
        });
    }

    private void updateUI() {
        seatListAdapter = new SeatListAdapter();
        rvSeatList.setAdapter(seatListAdapter);
        rvSeatList.setLayoutManager(new GridLayoutManager(this, 4));
        seatListAdapter.setOnSeatClickListener(this::onSpeakerSeatClicked);
        ZegoSpeakerSeatService seatService = ZegoRoomManager.getInstance().speakerSeatService;
        seatListAdapter.setSeatList(seatService.getSpeakerSeatList());

        messageListAdapter = new MessageListAdapter(textMessageList);
        rvMessageList.setAdapter(messageListAdapter);
        rvMessageList
            .setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        if (UserInfoHelper.isSelfOwner()) {
            uiToOwner();
            ZegoRoomManager.getInstance().speakerSeatService.takeSeat(0, error -> {
                requestRecordAudio();
            });
        } else {
            uiToAudience();
        }

        ZegoRoomInfo roomInfo = ZegoRoomManager.getInstance().roomService.roomInfo;
        tvRoomName.setText(roomInfo.getRoomName());
        tvRoomNum.setText(roomInfo.getRoomID());
    }

    private void onSpeakerSeatClicked(ZegoSpeakerSeatModel seatModel) {
        ZegoSpeakerSeatService seatService = ZegoRoomManager.getInstance().speakerSeatService;
        if (UserInfoHelper.isSelfOwner()) {
            if (seatModel.status == ZegoSpeakerSeatStatus.Untaken) {
                DialogHelper.showToastDialog(LiveAudioRoomActivity.this,
                    StringUtils.getString(R.string.room_page_lock_seat), dialog -> {
                        seatService.closeSeat(true, seatModel.seatIndex, errorCode -> {
                            if (errorCode == ZegoLiveAudioRoomErrorCode.SUCCESS.getValue()) {
                                ZegoSpeakerSeatModel model = seatService.getSpeakerSeatList()
                                    .get(seatModel.seatIndex);
                                seatListAdapter.updateUserInfo(model);
                            } else {
                                ToastUtils.showShort(R.string.toast_lock_seat_fail, errorCode);
                            }
                        });
                    });
            } else if (seatModel.status == ZegoSpeakerSeatStatus.Closed) {
                DialogHelper.showToastDialog(LiveAudioRoomActivity.this,
                    StringUtils.getString(R.string.room_page_unlock_seat), dialog -> {
                        seatService.closeSeat(false, seatModel.seatIndex, errorCode -> {
                            if (errorCode == ZegoLiveAudioRoomErrorCode.SUCCESS.getValue()) {
                                ZegoSpeakerSeatModel model = seatService.getSpeakerSeatList()
                                    .get(seatModel.seatIndex);
                                seatListAdapter.updateUserInfo(model);
                            }
                        });
                    });
            } else if (seatModel.status == ZegoSpeakerSeatStatus.Occupied) {
                final String userId = seatModel.userID;
                if (getMyUserID().equals(userId)) {
                    return;
                }
                DialogHelper.showToastDialog(LiveAudioRoomActivity.this,
                    StringUtils.getString(R.string.room_page_leave_seat), dialog -> {
                        DialogHelper.showAlertDialog(LiveAudioRoomActivity.this, null,
                            StringUtils
                                .getString(R.string.dialog_warning_leave_seat_message, userId),
                            StringUtils.getString(R.string.dialog_confirm),
                            StringUtils.getString(R.string.dialog_cancel),
                            (alertDialog, which) -> {
                                seatService.removeUserFromSeat(seatModel.seatIndex, errorCode -> {
                                    if (errorCode != ZegoLiveAudioRoomErrorCode.SUCCESS
                                        .getValue()) {
                                        ToastUtils.showShort(
                                            R.string.toast_kickout_leave_seat_error,
                                            userId, errorCode);
                                    }
                                });
                            },
                            (alertDialog, which) -> alertDialog.cancel()
                        );
                    });
            }
        } else {
            // is visitor
            if (seatModel.status == ZegoSpeakerSeatStatus.Untaken) {
                boolean hasOnSeat = seatService.getSeatedUserList().contains(getMyUserID());
                if (hasOnSeat) {
                    seatService.switchSeat(seatModel.seatIndex, errorCode -> {
                        if (errorCode != ZegoLiveAudioRoomErrorCode.SUCCESS.getValue()) {
                            ToastUtils.showShort(StringUtils
                                .getString(R.string.toast_take_speaker_seat_fail, errorCode));
                        }
                    });
                } else {
                    DialogHelper.showToastDialog(LiveAudioRoomActivity.this,
                        StringUtils.getString(R.string.room_page_take_seat), dialog -> {
                            seatService.takeSeat(seatModel.seatIndex, errorCode -> {
                                if (errorCode != ZegoLiveAudioRoomErrorCode.SUCCESS.getValue()) {
                                    ToastUtils.showShort(R.string.toast_take_speaker_seat_fail,
                                        errorCode);
                                } else {
                                    requestRecordAudio();
                                }
                            });
                        });
                }
            } else if (seatModel.status == ZegoSpeakerSeatStatus.Closed) {
                ToastUtils.showShort(
                    StringUtils.getString(R.string.the_wheat_position_has_been_locked));
            } else if (seatModel.status == ZegoSpeakerSeatStatus.Occupied) {
                if (getMyUserID().equals(seatModel.userID)) {
                    speakerLeaveSeat();
                }
            }
        }
    }

    private void speakerLeaveSeat() {
        DialogHelper.showToastDialog(LiveAudioRoomActivity.this,
            StringUtils.getString(R.string.room_page_leave_seat), dialog -> {
                DialogHelper.showAlertDialog(
                    LiveAudioRoomActivity.this,
                    StringUtils.getString(R.string.room_page_leave_seat),
                    StringUtils.getString(R.string.dialog_sure_to_leave_seat),
                    StringUtils.getString(R.string.dialog_confirm),
                    StringUtils.getString(R.string.dialog_cancel),
                    (alertDialog, which) -> {
                        ZegoSpeakerSeatService seatService = ZegoRoomManager
                            .getInstance().speakerSeatService;
                        seatService.leaveSeat(errorCode -> {
                            if (errorCode != ZegoLiveAudioRoomErrorCode.SUCCESS.getValue()) {
                                ToastUtils.showShort(R.string.toast_leave_seat_fail, errorCode);
                            }
                        });
                    },
                    (alertDialog, which) -> alertDialog.cancel()
                );
            });
    }

    private void showInviteDialog() {
        DialogHelper.showAlertDialog(LiveAudioRoomActivity.this,
            StringUtils.getString(R.string.dialog_invition_title),
            StringUtils.getString(R.string.dialog_invition_descrip),
            StringUtils.getString(R.string.dialog_accept),
            StringUtils.getString(R.string.dialog_refuse),
            (dialog, which) -> {
                ZegoSpeakerSeatService speakerSeatService = ZegoRoomManager
                    .getInstance().speakerSeatService;
                List<ZegoSpeakerSeatModel> speakerSeatList = speakerSeatService
                    .getSpeakerSeatList();
                int seatIndex = -1;
                for (int i = 0; i < speakerSeatList.size(); i++) {
                    ZegoSpeakerSeatModel model = speakerSeatList.get(i);
                    if (model.status == ZegoSpeakerSeatStatus.Untaken) {
                        seatIndex = model.seatIndex;
                    }
                }
                if (seatIndex != -1) {
                    speakerSeatService.takeSeat(seatIndex, errorCode -> {
                        requestRecordAudio();
                    });
                }
                dialog.cancel();
            },
            (dialog, which) -> {
                dialog.cancel();
            }
        );
    }

    private void initSDCallback() {
        ZegoGiftService giftService = ZegoRoomManager.getInstance().giftService;
        giftService.setGiftServiceCallback((giftID, fromUserID, toUserList) -> {
            showGiftTips(toUserList, fromUserID, giftID);
        });

        ZegoMessageService messageService = ZegoRoomManager.getInstance().messageService;
        messageService.setMessageServiceCallback((textMessage, roomID) -> {
            textMessageList.add(textMessage);
            refreshMessageList();
        });

        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        userService.setListener(new ZegoUserServiceListener() {
            @Override
            public void userInfoUpdate(ZegoUserInfo userInfo) {

            }

            @Override
            public void onRoomUserJoin(List<ZegoUserInfo> memberList) {
                for (ZegoUserInfo user : memberList) {
                    ZegoTextMessage textMessage = new ZegoTextMessage();
                    textMessage.message = StringUtils
                        .getString(R.string.room_page_joined_the_room, user.getUserName());
                    textMessageList.add(textMessage);
                    refreshMessageList();
                }
                seatListAdapter.notifyDataSetChanged();
                updateMemberListDialog();
            }

            @Override
            public void onRoomUserLeave(List<ZegoUserInfo> memberList) {
                for (ZegoUserInfo user : memberList) {
                    ZegoTextMessage textMessage = new ZegoTextMessage();
                    textMessage.message = StringUtils
                        .getString(R.string.room_page_has_left_the_room, user.getUserName());
                    textMessageList.add(textMessage);
                    refreshMessageList();

                }
                seatListAdapter.notifyDataSetChanged();
                updateMemberListDialog();
            }

            @Override
            public void onReceiveTakeSeatInvitation() {
                showInviteDialog();
            }
        });

        ZegoSpeakerSeatService seatService = ZegoRoomManager.getInstance().speakerSeatService;
        seatService.setSpeakerSeatServiceCallback(model -> {
            seatListAdapter.updateUserInfo(model);
            if (getMyUserID().equals(model.userID)) {
                if (model.status == ZegoSpeakerSeatStatus.Occupied && !UserInfoHelper.isSelfOwner()) {
                    uiToSpeaker();
                    ivMic.setSelected(!model.isMicMuted);
                } else if (model.status == ZegoSpeakerSeatStatus.Untaken) {
                    uiToAudience();
                }
            }
            updateMemberListDialog();
        });

        ZegoRoomService roomService = ZegoRoomManager.getInstance().roomService;
        roomService.setListener(new ZegoRoomServiceListener() {
            @Override
            public void onReceiveRoomInfoUpdate(ZegoRoomInfo roomInfo) {
                if (roomInfo == null) {
                    ToastUtils.showShort(StringUtils.getString(R.string.toast_room_has_destroyed));
                    finish();
                } else {
                    onUserMuted(roomInfo.isTextMessageDisabled());
                }
            }

            @Override
            public void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event) {
                Log.d(TAG, "onConnectionStateChanged() called with: state = [" + state + "], event = [" + event + "]");
                if (state == ZIMConnectionState.DISCONNECTED) {
                    if (event == ZIMConnectionEvent.LOGIN_TIMEOUT) {
                        showReconnectDialog();
                    } else {
                        ToastUtils.showShort(StringUtils.getString(R.string.toast_disconnect_tips));
                        ActivityUtils.startActivity(LiveAudioRoomActivity.this, UserLoginActivity.class);
                    }
                }
            }
        });
    }

    private void showReconnectDialog() {
        ZegoRoomService roomService = ZegoRoomManager.getInstance().roomService;
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        AlertDialog.Builder builder = new Builder(LiveAudioRoomActivity.this);
        builder.setMessage(R.string.room_tips_reconnect);
        builder.setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
            try {
                roomService.leaveRoom(errorCode -> {

                });
                String loginToken = TokenServerAssistant
                    .generateToken(KeyCenter.appID(), userService.localUserInfo.getUserID(),
                        KeyCenter.appZIMServerSecret(), 60 * 60 * 24).data;
                userService.login(userService.localUserInfo, loginToken, errorCode -> {
                    Log.d(TAG, "login() returned with: errorCode = [" + errorCode + "]");
                    if (errorCode == 0) {
                        String roomID = roomService.roomInfo.getRoomID();
                        ZegoUserInfo selfUser = userService.localUserInfo;
                        ZegoRTCServerAssistant.Privileges privileges = new ZegoRTCServerAssistant.Privileges();
                        privileges.canLoginRoom = true;
                        privileges.canPublishStream = true;
                        String joinRoomToken = ZegoRTCServerAssistant
                            .generateToken(KeyCenter.appID(), roomID, selfUser.getUserID(), privileges,
                                KeyCenter.appExpressSign(), 660).data;
                        if (!UserInfoHelper.isSelfOwner()) {
                            roomService.joinRoom(roomID, joinRoomToken, errorCode2 -> {
                                Log.d(TAG, "joinRoom() returned with: errorCode = [" + errorCode2 + "]");
                                if (errorCode2 == 0) {
                                    updateUI();
                                } else {
                                    ToastUtils.showShort(R.string.toast_join_room_fail, errorCode2);
                                    ActivityUtils.startActivity(LiveAudioRoomActivity.this, UserLoginActivity.class);
                                }
                            });
                        } else {
                            AlertDialog.Builder builder2 = new Builder(LiveAudioRoomActivity.this);
                            builder2.setTitle(R.string.network_connect_failed_title);
                            builder2.setMessage(R.string.network_connect_failed);
                            builder2.setPositiveButton(R.string.dialog_confirm, (dialog1, which1) -> {
                                ActivityUtils.startActivity(LiveAudioRoomActivity.this, UserLoginActivity.class);
                            });
                            if (!LiveAudioRoomActivity.this.isFinishing()) {
                                builder2.create().show();
                            }
                        }
                    }
                    dialog.cancel();
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, ((dialog, which) -> {
            dialog.cancel();
            ActivityUtils.startActivity(LiveAudioRoomActivity.this, UserLoginActivity.class);
        }));
        if (!LiveAudioRoomActivity.this.isFinishing()) {
            builder.create().show();
        }
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

    private void showGiftTips(List<String> toUserIDList, String fromUserID, String giftID) {
        String giftName = "";
        for (RoomGift value : RoomGift.values()) {
            if (value.getId().equals(giftID)) {
                giftName = getString(value.getName());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < toUserIDList.size(); i++) {
            String userID = toUserIDList.get(i);
            String name = ZegoLiveAudioRoomManager.getInstance().getRoomUserName(userID);
            sb.append(name);
            if (i != toUserIDList.size() - 1) {
                sb.append(",");
            }
        }
        String fromUserName = ZegoLiveAudioRoomManager.getInstance().getRoomUserName(fromUserID);
        String giftTips = getString(R.string.room_page_received_gift_tips, sb.toString(),
            fromUserName);
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
        ZegoRoomInfo roomInfo = ZegoRoomManager.getInstance().roomService.roomInfo;
        return roomInfo.getRoomID();
    }

    private String getMyUserID() {
        ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        return localUserInfo.getUserID();
    }

    @Override
    public void onBackPressed() {
        if (UserInfoHelper.isSelfOwner()) {
            showDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (settingsDialog != null) {
            settingsDialog = null;
        }
        ZegoRoomManager.getInstance().roomService.leaveRoom(error -> {
        });
    }
}