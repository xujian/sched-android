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
                    Log.d("SCHED", "authenticate ack");
                    Log.d("AUTH---", String.valueOf(args.length));
                    JSONObject response = (JSONObject) args[1];
                    try {
                        Log.d("AUTH---", response.toString(4));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JSONObject data = new JSONObject();
                    try {
                        data.put("product", "100000000088");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("create", "tasks", data);
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
}
