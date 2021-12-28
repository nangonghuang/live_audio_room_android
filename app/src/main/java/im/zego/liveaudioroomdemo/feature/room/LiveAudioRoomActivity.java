package im.zego.liveaudioroomdemo.feature.room;

import android.app.Dialog;
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
import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.constants.ZegoRoomErrorCode;
import im.zego.liveaudioroom.listener.ZegoRoomServiceListener;
import im.zego.liveaudioroom.listener.ZegoUserServiceListener;
import im.zego.liveaudioroom.model.ZegoRoomInfo;
import im.zego.liveaudioroom.model.ZegoRoomUserRole;
import im.zego.liveaudioroom.model.ZegoSpeakerSeatModel;
import im.zego.liveaudioroom.model.ZegoSpeakerSeatStatus;
import im.zego.liveaudioroom.model.ZegoTextMessage;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.liveaudioroom.service.ZegoGiftService;
import im.zego.liveaudioroom.service.ZegoMessageService;
import im.zego.liveaudioroom.service.ZegoRoomService;
import im.zego.liveaudioroom.service.ZegoSpeakerSeatService;
import im.zego.liveaudioroom.service.ZegoUserService;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.BaseActivity;
import im.zego.liveaudioroomdemo.feature.room.adapter.MessageListAdapter;
import im.zego.liveaudioroomdemo.feature.room.adapter.SeatListAdapter;
import im.zego.liveaudioroomdemo.feature.room.dialog.IMInputDialog;
import im.zego.liveaudioroomdemo.feature.room.dialog.LoadingDialog;
import im.zego.liveaudioroomdemo.feature.room.dialog.MemberListDialog;
import im.zego.liveaudioroomdemo.feature.room.dialog.SendGiftDialog;
import im.zego.liveaudioroomdemo.feature.room.dialog.SettingsDialog;
import im.zego.liveaudioroomdemo.feature.room.enums.RoomGift;
import im.zego.liveaudioroomdemo.helper.DialogHelper;
import im.zego.liveaudioroomdemo.helper.PermissionHelper;
import im.zego.liveaudioroomdemo.helper.UserInfoHelper;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LiveAudioRoomActivity extends BaseActivity {

    private LoadingDialog loadingDialog;
    private AlertDialog inviteDialog;

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
    private TextView tvRoomID;
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
        tvRoomID = findViewById(R.id.tv_room_id);

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
            if (isImMuted && !UserInfoHelper.isSelfOwner()) {
                ToastUtils.showShort(R.string.room_page_bands_send_message);
            } else {
                imInputDialog = new IMInputDialog(this);
                imInputDialog.setOnSendListener(imText -> {
                    ZegoMessageService service = ZegoRoomManager.getInstance().messageService;
                    boolean textMessageDisabled = ZegoRoomManager.getInstance().roomService.roomInfo
                        .isTextMessageDisabled();
                    String userID = ZegoRoomManager.getInstance().userService.localUserInfo.getUserID();
                    String hostID = ZegoRoomManager.getInstance().roomService.roomInfo.getHostID();
                    if (userID.equals(hostID) || !textMessageDisabled) {
                        service.sendTextMessage(imText, errorCode -> {
                            if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                                final ZegoTextMessage text = new ZegoTextMessage();
                                text.message = imText;
                                text.userID = getMyUserID();
                                textMessageList.add(text);
                                refreshMessageList();
                            } else {
                                ToastUtils.showShort(R.string.toast_send_message_error, errorCode);
                            }
                        });
                    } else {
                        ToastUtils.showShort(R.string.room_page_bands_send_message);
                    }
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
            ZegoUserService userService = ZegoRoomManager.getInstance().userService;
            List<ZegoUserInfo> userList = userService.getUserList();
            if (memberListDialog == null) {
                memberListDialog = new MemberListDialog(this, userList);
            }
            if (!memberListDialog.isShowing()) {
                memberListDialog.show();
            }
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
                settingsDialog = new SettingsDialog(this);
            }
            settingsDialog.show();
        });
        ivMore.setOnClickListener(v -> speakerLeaveSeat());
    }

    private void updateMemberListDialog() {
        if (memberListDialog != null) {
            ZegoUserService userService = ZegoRoomManager.getInstance().userService;
            memberListDialog.updateUserList(userService.getUserList());
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
        tvRoomID.setText(roomInfo.getRoomID());
    }

    private void onSpeakerSeatClicked(ZegoSpeakerSeatModel seatModel) {
        ZegoSpeakerSeatService seatService = ZegoRoomManager.getInstance().speakerSeatService;
        if (UserInfoHelper.isSelfOwner()) {
            if (seatModel.status == ZegoSpeakerSeatStatus.Untaken) {
                DialogHelper.showToastDialog(LiveAudioRoomActivity.this,
                    StringUtils.getString(R.string.room_page_lock_seat), dialog -> {
                        if (seatModel.status == ZegoSpeakerSeatStatus.Untaken) {
                            seatService.closeSeat(true, seatModel.seatIndex, errorCode -> {
                                if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                                    ZegoSpeakerSeatModel model = seatService.getSpeakerSeatList()
                                        .get(seatModel.seatIndex);
                                    seatListAdapter.updateUserInfo(model);
                                } else {
                                    ToastUtils.showShort(R.string.toast_lock_seat_fail, errorCode);
                                }
                            });
                        } else {
                            ToastUtils.showShort(R.string.toast_lock_seat_already_take_seat);
                        }
                    });
            } else if (seatModel.status == ZegoSpeakerSeatStatus.Closed) {
                DialogHelper.showToastDialog(LiveAudioRoomActivity.this,
                    StringUtils.getString(R.string.room_page_unlock_seat), dialog -> {
                        seatService.closeSeat(false, seatModel.seatIndex, errorCode -> {
                            if (errorCode == ZegoRoomErrorCode.SUCCESS) {
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
                if (seatModel.status == ZegoSpeakerSeatStatus.Occupied) {
                    DialogHelper.showToastDialog(LiveAudioRoomActivity.this,
                        StringUtils.getString(R.string.room_page_leave_speaker_seat), dialog -> {
                            DialogHelper.showAlertDialog(LiveAudioRoomActivity.this, null,
                                StringUtils.getString(R.string.dialog_warning_leave_seat_message, userId),
                                StringUtils.getString(R.string.dialog_confirm),
                                StringUtils.getString(R.string.dialog_cancel),
                                (alertDialog, which) -> {
                                    seatService.removeUserFromSeat(seatModel.seatIndex, errorCode -> {
                                        if (errorCode != ZegoRoomErrorCode.SUCCESS) {
                                            ToastUtils.showShort(R.string.toast_kickout_leave_seat_error,
                                                userId, errorCode);
                                        }
                                    });
                                },
                                (alertDialog, which) -> alertDialog.cancel()
                            );
                        });
                } else {
                    ToastUtils
                        .showShort(R.string.toast_kickout_leave_seat_error, userId, ZegoRoomErrorCode.NOT_IN_SEAT);
                }

            }
        } else {
            // is visitor
            if (seatModel.status == ZegoSpeakerSeatStatus.Untaken) {
                DialogHelper.showToastDialog(LiveAudioRoomActivity.this,
                    StringUtils.getString(R.string.room_page_take_seat), dialog -> {
                        if (seatModel.status == ZegoSpeakerSeatStatus.Untaken) {
                            boolean hasOnSeat = seatService.getSeatedUserList().contains(getMyUserID());
                            if (hasOnSeat) {
                                seatService.switchSeat(seatModel.seatIndex, errorCode -> {
                                    if (errorCode != ZegoRoomErrorCode.SUCCESS) {
                                        ToastUtils.showShort(StringUtils
                                            .getString(R.string.toast_take_speaker_seat_fail, errorCode));
                                    }
                                });
                            } else {
                                seatService.takeSeat(seatModel.seatIndex, errorCode -> {
                                    if (errorCode != ZegoRoomErrorCode.SUCCESS) {
                                        ToastUtils.showShort(R.string.toast_take_speaker_seat_fail,
                                            errorCode);
                                    } else {
                                        requestRecordAudio();
                                    }
                                });
                            }
                        } else {
                            ToastUtils.showShort(R.string.toast_take_speaker_seat_fail, ZegoRoomErrorCode.ERROR);
                        }
                    });
            } else if (seatModel.status == ZegoSpeakerSeatStatus.Closed) {
                ToastUtils.showShort(StringUtils.getString(R.string.the_wheat_position_has_been_locked));
            } else if (seatModel.status == ZegoSpeakerSeatStatus.Occupied) {
                if (getMyUserID().equals(seatModel.userID)) {
                    speakerLeaveSeat();
                } else {
                    ToastUtils.showShort(R.string.the_wheat_position_has_been_locked);
                }
            }
        }
    }

    private void speakerLeaveSeat() {
        DialogHelper.showToastDialog(LiveAudioRoomActivity.this,
            StringUtils.getString(R.string.room_page_leave_speaker_seat), dialog -> {
                DialogHelper.showAlertDialog(
                    LiveAudioRoomActivity.this,
                    StringUtils.getString(R.string.room_page_leave_speaker_seat),
                    StringUtils.getString(R.string.dialog_sure_to_leave_seat),
                    StringUtils.getString(R.string.dialog_confirm),
                    StringUtils.getString(R.string.dialog_cancel),
                    (alertDialog, which) -> {
                        ZegoSpeakerSeatService seatService = ZegoRoomManager.getInstance().speakerSeatService;
                        ZegoSpeakerSeatModel mySeatModel = null;
                        for (ZegoSpeakerSeatModel seatModel : seatService.getSpeakerSeatList()) {
                            if (Objects.equals(seatModel.userID, getMyUserID())) {
                                mySeatModel = seatModel;
                                break;
                            }
                        }
                        if (mySeatModel != null && mySeatModel.status == ZegoSpeakerSeatStatus.Occupied) {
                            seatService.leaveSeat(errorCode -> {
                                if (errorCode != ZegoRoomErrorCode.SUCCESS) {
                                    ToastUtils.showShort(R.string.toast_leave_seat_fail, errorCode);
                                }
                            });
                        } else {
                            ToastUtils.showShort(R.string.toast_leave_seat_fail, ZegoRoomErrorCode.NOT_IN_SEAT);
                        }
                    },
                    (alertDialog, which) -> alertDialog.cancel()
                );
            });
    }

    private void showInviteDialog() {
        if (inviteDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(StringUtils.getString(R.string.dialog_invition_title));
            builder.setMessage(StringUtils.getString(R.string.dialog_invition_descrip));
            builder.setPositiveButton(StringUtils.getString(R.string.dialog_accept), (dialog, which) -> {
                ZegoSpeakerSeatService speakerSeatService = ZegoRoomManager.getInstance().speakerSeatService;
                ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
                List<ZegoSpeakerSeatModel> speakerSeatList = speakerSeatService.getSpeakerSeatList();
                boolean isSpeaker = false;
                for (int i = 0; i < speakerSeatList.size(); i++) {
                    ZegoSpeakerSeatModel speakerSeatModel = speakerSeatList.get(i);
                    if (speakerSeatModel.userID.equals(localUserInfo.getUserID())
                        && speakerSeatModel.status == ZegoSpeakerSeatStatus.Occupied) {
                        isSpeaker = true;
                        break;
                    }
                }
                if (isSpeaker) {
                    return;
                }
                int seatIndex = -1;
                for (int i = 0; i < speakerSeatList.size(); i++) {
                    ZegoSpeakerSeatModel model = speakerSeatList.get(i);
                    if (model.status == ZegoSpeakerSeatStatus.Untaken) {
                        seatIndex = model.seatIndex;
                        break;
                    }
                }
                if (seatIndex != -1) {
                    speakerSeatService.takeSeat(seatIndex, errorCode -> {
                        if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                            requestRecordAudio();
                        } else {
                            ToastUtils.showShort(R.string.toast_take_speaker_seat_fail, errorCode);
                        }
                    });
                }
                dialog.dismiss();
            });
            builder.setNegativeButton(StringUtils.getString(R.string.dialog_refuse), (dialog, which) -> {
                dialog.dismiss();
            });
            inviteDialog = builder.create();
        }
        if (!inviteDialog.isShowing()) {
            inviteDialog.show();
        }
    }

    private void initSDCallback() {
        ZegoGiftService giftService = ZegoRoomManager.getInstance().giftService;
        giftService.setListener((giftID, fromUserID, toUserList) -> {
            showGiftTips(toUserList, fromUserID, giftID);
        });

        ZegoMessageService messageService = ZegoRoomManager.getInstance().messageService;
        messageService.setListener((textMessage, roomID) -> {
            textMessageList.add(textMessage);
            refreshMessageList();
        });

        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        userService.setListener(new ZegoUserServiceListener() {
            @Override
            public void userInfoUpdate(ZegoUserInfo userInfo) {

            }

            @Override
            public void onRoomUserJoin(List<ZegoUserInfo> userInfos) {
                boolean containsSelf = false;
                ZegoUserInfo localUserInfo = userService.localUserInfo;
                for (ZegoUserInfo userInfo : userInfos) {
                    if (Objects.equals(userInfo.getUserID(), localUserInfo.getUserID())) {
                        containsSelf = true;
                        break;
                    }
                }
                if (containsSelf) {
                    ZegoTextMessage textMessage = new ZegoTextMessage();
                    textMessage.message = StringUtils
                        .getString(R.string.room_page_joined_the_room, localUserInfo.getUserName());
                    textMessageList.add(textMessage);
                    refreshMessageList();
                } else {
                    for (ZegoUserInfo user : userInfos) {
                        ZegoTextMessage textMessage = new ZegoTextMessage();
                        textMessage.message = StringUtils
                            .getString(R.string.room_page_joined_the_room, user.getUserName());
                        textMessageList.add(textMessage);
                        refreshMessageList();
                    }
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
        seatService.setListener(model -> {
            ZegoUserInfo userInfo = userService.getUserInfo(getMyUserID());
            if (userInfo != null) {
                if (userInfo.getRole() == ZegoRoomUserRole.Speaker) {
                    uiToSpeaker();
                    for (ZegoSpeakerSeatModel seatModel : seatService.getSpeakerSeatList()) {
                        if (Objects.equals(seatModel.userID, getMyUserID())) {
                            ivMic.setSelected(seatModel.mic);
                        }
                    }
                } else if (userInfo.getRole() == ZegoRoomUserRole.Host) {
                    uiToOwner();
                } else {
                    uiToAudience();
                }
            }
            seatListAdapter.updateUserInfo(model);
            updateMemberListDialog();
            if (giftDialog != null && giftDialog.isShowing()) {
                giftDialog.updateList();
            }
        });

        ZegoRoomService roomService = ZegoRoomManager.getInstance().roomService;
        roomService.setListener(new ZegoRoomServiceListener() {
            @Override
            public void onReceiveRoomInfoUpdate(ZegoRoomInfo roomInfo) {
                Log.d(TAG, "onReceiveRoomInfoUpdate() called with: roomInfo = [" + roomInfo + "]");
                if (roomInfo == null) {
                    ToastUtils.showShort(StringUtils.getString(R.string.toast_room_has_destroyed));
                    finish();
                } else {
                    onUserMessageDisabled(roomInfo.isTextMessageDisabled());
                }
            }

            @Override
            public void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event) {
                Log.d(TAG, "onConnectionStateChanged() called with: state = [" + state + "], event = [" + event + "]");
                if (state == ZIMConnectionState.DISCONNECTED) {
                    dismissDialog(loadingDialog);
                    if (event == ZIMConnectionEvent.LOGIN_TIMEOUT) {
                        showDisconnectDialog();
                    } else {
                        if (event == ZIMConnectionEvent.SUCCESS) {
                            // disconnect because of room end
                            ToastUtils.showShort(StringUtils.getString(R.string.toast_room_has_destroyed));
                            finish();
                        } else if (event == ZIMConnectionEvent.KICKED_OUT) {
                            //disconnect because of multiple login,been kicked out
                            ToastUtils.showShort(R.string.toast_kickout_error);
                            ActivityUtils.finishAllActivities();
                            ActivityUtils.startLauncherActivity();
                        } else {
                            ToastUtils.showShort(StringUtils.getString(R.string.toast_disconnect_tips));
                            ActivityUtils.finishAllActivities();
                            ActivityUtils.startLauncherActivity();
                        }

                    }
                } else if (state == ZIMConnectionState.RECONNECTING) {
                    showLoadingDialog();
                } else if (state == ZIMConnectionState.CONNECTED) {
                    dismissDialog(loadingDialog);
                }
            }
        });
    }

    private void showDisconnectDialog() {
        AlertDialog.Builder builder2 = new Builder(LiveAudioRoomActivity.this);
        builder2.setTitle(R.string.network_connect_failed_title);
        builder2.setMessage(R.string.network_connect_failed);
        builder2.setCancelable(false);
        builder2.setPositiveButton(R.string.dialog_confirm, (dialog1, which1) -> {
            ActivityUtils.finishAllActivities();
            ActivityUtils.startLauncherActivity();
        });
        if (!LiveAudioRoomActivity.this.isFinishing()) {
            AlertDialog alertDialog = builder2.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    private void onUserMessageDisabled(boolean isMuted) {
        isImMuted = isMuted;
        if (imInputDialog != null) {
            imInputDialog.updateSendButtonState(isMuted);
        }
        if (!UserInfoHelper.isSelfOwner()) {
            ivIm.setActivated(!isMuted);
        }
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
            String name = ZegoRoomManager.getInstance().userService.getUserName(userID);
            sb.append(name);
            if (i != toUserIDList.size() - 1) {
                sb.append(",");
            }
        }
        String fromUserName = ZegoRoomManager.getInstance().userService.getUserName(fromUserID);
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

    public void showExitDialog() {
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
            showExitDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDialog(settingsDialog);
        dismissDialog(giftDialog);
        dismissDialog(imInputDialog);
        dismissDialog(memberListDialog);
        dismissDialog(loadingDialog);
        dismissDialog(inviteDialog);
        ZegoRoomManager.getInstance().roomService.leaveRoom(error -> {
        });
    }

    private void dismissDialog(Dialog dialog) {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        loadingDialog.updateText(R.string.network_reconnect);
        loadingDialog.show();
    }
}