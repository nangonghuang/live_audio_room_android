package im.zego.liveaudioroomdemo.feature.webview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebChromeClient;

import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.BaseActivity;


/**
 * Created by rocket_wang on 2021/12/22.
 */
public class WebViewActivity extends BaseActivity {

    private ImageView ivLogout;
    private TextView tvTitle;
    private FrameLayout webViewContainer;

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(EXTRA_KEY_URL, url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static final String EXTRA_KEY_URL = "extra_key_url";
    private AgentWeb mAgentWeb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        String url = getIntent().getStringExtra(EXTRA_KEY_URL);
        initView();

        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(webViewContainer, new FrameLayout.LayoutParams(-1, -1))
                .useDefaultIndicator(ColorUtils.getColor(R.color.blue_text), SizeUtils.dp2px(1f))
                .setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        super.onReceivedTitle(view, title);
                        tvTitle.setText(title);
                    }
                })
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                .createAgentWeb()
                .ready()
                .go(url);

        ivLogout.setOnClickListener(v -> onBackPressed());
    }

    private void initView() {
        ivLogout = findViewById(R.id.iv_logout);
        tvTitle = findViewById(R.id.tv_title);
        webViewContainer = findViewById(R.id.web_view_container);
    }

    @Override
    public void onBackPressed() {
        if (!mAgentWeb.back()) {
            super.onBackPressed();
        }
    }
}