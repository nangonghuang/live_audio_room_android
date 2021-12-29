package im.zego.liveaudioroomdemo.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.StringUtils;

import im.zego.liveaudioroomdemo.feature.room.dialog.ConfirmDialog;

public final class DialogHelper {
    public static void showAlertDialog(
            Context context,
            String title,
            String content,
            String positiveText,
            String negativeText,
            DialogInterface.OnClickListener positiveClickListener,
            DialogInterface.OnClickListener negativeClickListener
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (!StringUtils.isTrimEmpty(title)) {
            builder.setTitle(title);
        }
        builder.setMessage(content);

        builder.setPositiveButton(positiveText, positiveClickListener);
        builder.setNegativeButton(negativeText, negativeClickListener);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showToastDialog(
            Context context,
            @NonNull String text,
            @NonNull ConfirmDialog.IDialogListener listener
    ) {
        ConfirmDialog confirmDialog = new ConfirmDialog(context, dialog -> {
            dialog.dismiss();
            listener.onConfirmClick(dialog);
        });
        confirmDialog.setTitle(text);
        confirmDialog.show();
    }
}