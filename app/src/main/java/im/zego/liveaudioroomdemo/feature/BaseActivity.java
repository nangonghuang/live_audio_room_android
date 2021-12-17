package im.zego.liveaudioroomdemo.feature;

import android.os.Bundle;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gyf.immersionbar.ImmersionBar;

import im.zego.liveaudioroomdemo.R;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImmersionBar.with(this)
                .fitsSystemWindows(true)
                .statusBarColor(getStatusBarColor())
                .statusBarDarkFont(true)
                .init();
    }

    @ColorRes
    protected int getStatusBarColor() {
        return R.color.white;
    }
}
