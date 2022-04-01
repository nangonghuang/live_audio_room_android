package im.zego.liveaudioroomdemo;

import android.app.Application;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import im.zego.liveaudioroom.ZegoRoomManager;

public class App extends Application {

    private String serverSecret;
    private long appID;

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        LogUtils.getConfig().setLogHeadSwitch(false);
        LogUtils.getConfig().setBorderSwitch(false);

        String jsonFile = readJsonFile("KeyCenter.json");
        if (jsonFile == null) {
            /// Please run the script './configure.sh' first on the root directory
            throw new IllegalArgumentException("\n========\n*** Please run the script './configure.sh' first！***\n========");
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonFile);
            appID = jsonObject.getLong("appID");
            serverSecret = jsonObject.getString("serverSecret");

            /**
             * init LiveAudioRoom SDK with your appID and appSign
             */
            ZegoRoomManager.getInstance().init(appID, this);
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

    private String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
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