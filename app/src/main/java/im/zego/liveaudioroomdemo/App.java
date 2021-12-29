package im.zego.liveaudioroomdemo;

import android.app.Application;
import android.util.Log;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import im.zego.liveaudioroom.ZegoRoomManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

public class App extends Application {

    private String serverSecret;
    private long appID;
    private String appSign;

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        LogUtils.getConfig().setLogHeadSwitch(false);
        LogUtils.getConfig().setBorderSwitch(false);

        String jsonFile = readJsonFile("KeyCenter.json");
        try {
            JSONObject jsonObject = new JSONObject(jsonFile);
            appID = jsonObject.getLong("appID");
            appSign = jsonObject.getString("appSign");
            serverSecret = jsonObject.getString("serverSecret");

            Log.d("App", "onCreate() called:" + serverSecret);

            // init LiveAudioRoom SDK
            ZegoRoomManager.getInstance().init(appID, appSign, this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getServerSecret() {
        return serverSecret;
    }

    public long getAppID() {
        return appID;
    }

    public String getAppSign() {
        return appSign;
    }

    private String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            ;
            InputStream inputStream = getAssets().open(fileName);
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            inputStream.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}