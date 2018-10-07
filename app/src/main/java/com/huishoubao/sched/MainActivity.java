package com.huishoubao.sched;

import android.os.Bundle;
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

import java.util.concurrent.CompletionStage;

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
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on("pong", onPong);
        socket.on("authenticated", onAuthenticated);
        socket.on("tasks created", onTaskCreated);
        socket.on("command goto", onGoto);
        socket.connect();
        Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_LONG).show();
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
        if (id == R.id.nav_checkin) {
            // Handle the camera action
            checkin("100000000088");
        } else if (id == R.id.nav_done) {
            sendDoneMessage("lcd.white");
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            toast("Connected");
        }
    };

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

    private Emitter.Listener onAuthenticated = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            toast("Authenticated");
        }
    };

    private Emitter.Listener onGoto = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                String stage = data.getString("stage");
                toast("Goto: " + stage);
                setMainText("GOTO: " + stage);
                sendOnStageMessage(stage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 向调度中心报告
     * @param view
     */
    public void checkin (String sn) {
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

    /**
     * 操作已完成 向调度中心报告
     * @param view
     */
    public void sendDoneMessage (String stage) {
        JSONObject data = new JSONObject();
        JSONObject params = new JSONObject();
        try {
            params.put("stage", stage);
            data.put("type", "done");
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
    public void sendOnStageMessage (String stage) {
        JSONObject data = new JSONObject();
        JSONObject params = new JSONObject();
        try {
            params.put("stage", stage);
            data.put("type", "on-stage");
            data.put("params", params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("MENU", "GOTO-OK--------------------------------------------------------");
        socket.emit("create", "messages", data);
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

    private void setMainText (String text) {
        final String message = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView mainText = (TextView) findViewById(R.id.mainText);
                mainText.setText(message);
            }
        });
    }
}
