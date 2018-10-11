package com.huishoubao.sched;

import android.app.Application;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;

public class SchedApplication extends Application {

    private String sn = "100000000088";

    private Socket socket;
    {
        try {
            // 建立websocket通道
            socket = IO.socket("https://sched.breezemakes.com");
            JSONObject args = new JSONObject();
            args.put("strategy", "local");
            args.put("email", "steam-app@huishoubao.com.cn");
            args.put("password", "123456");
            socket.emit("authenticate", args, new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject response = (JSONObject) args[1];
                    Log.d("SCHED", "=================================authenticate ack");
                    try {
                        Log.d("SCHED", response.getString("accessToken"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    checkin();
                }
            });
        } catch (URISyntaxException e) {
            Log.e("APP", "ERROR================create socket");
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw  new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public String getSn() {
        return sn;
    }

    /**
     * 向调度中心报告
     * @param view
     */
    public void checkin () {
        JSONObject data = new JSONObject();
        JSONObject params = new JSONObject();
        try {
            params.put("sn", sn);
            data.put("type", "checkin");
            data.put("params", params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("MENU", "CHECKIN--------------------------------------------------------");
        socket.emit("create", "messages", data);
    }
}
