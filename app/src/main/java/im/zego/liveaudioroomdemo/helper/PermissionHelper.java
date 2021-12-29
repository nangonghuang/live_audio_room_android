package im.zego.liveaudioroomdemo.helper;

import android.Manifest;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.permissionx.guolindev.PermissionX;

/**
 * Created by rocket_wang on 2021/12/10.
 */
public class PermissionHelper {
    public static void requestRecordAudio(FragmentActivity activity, @Nullable IPermissionCallback permissionCallback) {
        PermissionX.init(activity)
                .permissions(Manifest.permission.RECORD_AUDIO)
                .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList, "Core fundamental are based on these permissions", "OK", "Cancel"))
                .onForwardToSettings((scope, deniedList) -> scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel"))
                .request((allGranted, grantedList, deniedList) -> {
                    if (permissionCallback != null) {
                        permissionCallback.onRequestCallback(allGranted);
                    }
                });
    }

    public interface IPermissionCallback {
        void onRequestCallback(boolean isAllGranted);
    }
}