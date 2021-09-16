package com.example.testclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testserver.IRemoteService;
import com.example.testserver.MyData;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BinderSimple";

    private IRemoteService mRemoteService;

    private boolean mIsBound;
    private TextView mCallBackTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "[ClientActivity] onCreate");

        setContentView(R.layout.activity_main);

        mCallBackTv = (TextView) findViewById(R.id.tv_callback);
        mCallBackTv.setText("unattached");
    }

    /**
     * 用语监控远程服务连接的状态
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIsBound = true;
            mRemoteService = IRemoteService.Stub.asInterface(service);
            String pidInfo = null;
            try {
                MyData myData = mRemoteService.getMyData();
                pidInfo = "pid="+ mRemoteService.getPid() +
                        ", data1 = "+ myData.getData1() +
                        ", data2="+ myData.getData2();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "[ClientActivity] onServiceConnected  "+pidInfo);
            mCallBackTv.setText(pidInfo);
            Toast.makeText(MainActivity.this, "remote_service_connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "[ClientActivity] onServiceDisconnected");
            mCallBackTv.setText("onServiceDisconnected");
            mRemoteService = null;
            mIsBound = false;
            Toast.makeText(MainActivity.this, "remote_service_disconnected", Toast.LENGTH_SHORT).show();
        }
    };


    public void clickHandler(View view){
        switch (view.getId()){
            case R.id.btn_bind:
                bindRemoteService();
                break;

            case R.id.btn_unbind:
                unbindRemoteService();
                break;

            case R.id.btn_kill:
                killRemoteService();
                break;
        }
    }

    /**
     * 绑定远程服务
     */
    private void bindRemoteService(){
        Log.i(TAG, "[ClientActivity] bindRemoteService");
        //Intent intent = new Intent(this, RemoteService.class);
        //intent.setAction(IRemoteService.class.getName());
        Intent intent = new Intent();
        intent.setAction("CkyRemoteService");
        intent.setPackage("com.example.testserver");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        mCallBackTv.setText("binding");
    }

    /**
     * 解除绑定远程服务
     */
    private void unbindRemoteService(){
        if(!mIsBound){
            return;
        }
        Log.i(TAG, "[ClientActivity] unbindRemoteService ==>");
        unbindService(mConnection);
        mIsBound = false;
        mCallBackTv.setText("unbinding");
    }

    /**
     * 杀死远程服务
     */
    private void killRemoteService(){
        Log.i(TAG, "[ClientActivity] killRemoteService");
        try {
            android.os.Process.killProcess(mRemoteService.getPid());
            mCallBackTv.setText("kill_success");
        } catch (RemoteException e) {
            e.printStackTrace();
            Toast.makeText(this, "kill_failure", Toast.LENGTH_SHORT).show();
        }
    }
}