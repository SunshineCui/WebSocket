package com.billy.websocket;

import android.databinding.Bindable;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.billy.websocket.databinding.ActivityMainBinding;
import com.billy.websocket.model.Model;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.framing.PongFrame;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding mBinding;
    private WebSocketClient mSocketClient;
    private Model mModel;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSocketClient != null && mSocketClient.isClosed()) {
                mSocketClient.reconnect();
            }
        }
    };

    private ExecutorService mExecutorService;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setOnClick(this);
        init();
    }

    private void init() {
        mModel = new Model();
        mBinding.setModel(mModel);
        mModel.setContent("欢迎来到聊天室");
        mExecutorService = Executors.newCachedThreadPool();
        MyThread mMyThread = new MyThread();
        mMyThread.start();
    }

    private class MyThread extends Thread {
        @Override
        public void run() {
            try {
                mSocketClient = new WebSocketClient(new URI("ws://172.18.44.135:2017/"), new Draft_6455()) {
                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                        Log.d("picher_log", "打开通道" + handshakedata.getHttpStatus());
                    }

                    @Override
                    public void onMessage(String message) {
                        Log.d("picher_log", "接收消息" + message);
                        mModel.setContent(mModel.getContent() + "\n" + message);
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        Log.d("picher_log", "通道关闭,code:" + code + ",reason:" + reason + ",remote:" + remote);
                        if (mHandler == null) {
                            mHandler = new Handler(getMainLooper());
                        }
                        mHandler.postDelayed(mRunnable, 1000);
                    }

                    @Override
                    public void onCloseInitiated(int code, String reason) {
                        super.onCloseInitiated(code, reason);
                    }

                    @Override
                    public void onError(Exception ex) {
                        Log.d("picher_log", "链接错误:" + ex.getMessage());
                    }

                    @Override
                    public void onWebsocketPing(WebSocket conn, Framedata f) {
                        super.onWebsocketPing(conn, f);
                        Log.d("picher_log", "ping");
                    }

                    @Override
                    public void onWebsocketPong(WebSocket conn, Framedata f) {
//                            conn.sendFrame( new PingFrame() );
                        Log.d("picher_log", "pong");
                    }
                };
                mSocketClient.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocketClient != null) {
            mSocketClient.close();
            mSocketClient = null;
        }
        if (mHandler!=null){
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            if (mSocketClient.isOpen()) {
                mSocketClient.send(mBinding.edittext.getText().toString());
            }
        } else if (v.getId() == R.id.button_closed) {
            mSocketClient.close(CloseFrame.NORMAL, "手动关闭");
        } else if (v.getId() == R.id.button_return) {
            myReconnect();
        }
    }

    private void myReconnect() {
        if (mSocketClient != null && mSocketClient.isClosed()) {
            mExecutorService.execute(mRunnable);
        }
    }
}
