package com.huishoubao.sched;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SchedApplication app = (SchedApplication) this.getApplication();
        TextView snText = (TextView) findViewById(R.id.snText);
        snText.setText("sn=" + app.getSn());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        this.initSocket();
    }

    private void initSocket () {
        SchedApplication app = (SchedApplication) this.getApplication();
        socket = app.getSocket();
        // socket.on("pong", onPong);
        socket.on(Socket.EVENT_ERROR, onError);
        socket.on(Socket.EVENT_RECONNECT, onReconnect);
        socket.on("authenticated", onAuthenticated);
        socket.on("tasks created", onTaskCreated);
        socket.on("commands goto", onGoto);
        socket.on("messages hello", onHello);

        socket.connect();
        setStatus("Connecting");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_done_lcd) {
            sendDoneMessage("lcd.white");
        } else if (id == R.id.nav_done_touch) {
            sendDoneMessage("touch");
        } else if (id == R.id.nav_done_sensors) {
            sendDoneMessage("sensors");
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private Emitter.Listener onTaskCreated = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("MAIN", "task created");
            toast("Task created");
        }
    };

    private Emitter.Listener onPong = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            toast("Pone");
        }
    };

    private Emitter.Listener onError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            toast("Error");
        }
    };

    private Emitter.Listener onReconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            SchedApplication app = (SchedApplication) getApplication();
            app.login();
            toast("Reconnect");
        }
    };

    private Emitter.Listener onAuthenticated = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("SCHED", "=============================///authenticated");
            toast("Authenticated");
            setStatus("Authenticated");
        }
    };

    private Emitter.Listener onGoto = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                String stage = data.getString("stage");
                toast("Goto: " + stage);
                setStage(stage);
                sendReadyMessage(stage, new JSONObject()); // 模拟返回ready message
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onHello = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                String random = data.getString("random");
                toast("Message: hello");
                setStage("hello:" + random);
                JSONObject params = new JSONObject();
                params.put("random", random);
                sendReadyMessage("hello", params);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 操作已完成 向调度中心报告
     * @param view
     */
    public void sendDoneMessage (String stage) {
        JSONObject data = new JSONObject();
        JSONObject params = new JSONObject();
        try {
            data.put("type", "done");
            params.put("stage", stage);
            data.put("params", params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("MENU", "DONE--------------------------------------------------------");
        socket.emit("create", "messages", data);
    }

    /**
     * 操作已完成 向调度中心报告
     * @param view
     */
    public void sendReadyMessage(String stage, JSONObject params) {
        final JSONObject data = new JSONObject();
        JSONObject p = new JSONObject();
        try {
            data.put("type", "ready");
            p.put("stage", stage);
            Iterator keys = params.keys();
            while (keys.hasNext()) { // 复制参数
                String key = (String) keys.next();
                p.put(key, params.getString(key));
            }
            data.put("params", params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("MENU", "READY--------------------------------------------------------");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                socket.emit("create", "messages", data);
            }
        }, 2000 );
    }

    private void toast (String text) {
        final String message = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setStage(String text) {
        final String message = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView stageText = (TextView) findViewById(R.id.stageText);
                stageText.setText("stage=" + message);
            }
        });
    }

    private void setStatus(String text) {
        final String message = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView statusText = (TextView) findViewById(R.id.statusText);
                statusText.setText("status=" + message);
            }
        });
    }
}












