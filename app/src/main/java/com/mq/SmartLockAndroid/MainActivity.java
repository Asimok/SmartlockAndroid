package com.mq.SmartLockAndroid;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mq.SmartLockAndroid.BaiduMap.location_Activity;
import com.mq.SmartLockAndroid.dao.HttpAsyncTask;
import com.mq.SmartLockAndroid.service.MinaService;
import com.mq.SmartLockAndroid.tools.Codes;
import com.mq.SmartLockAndroid.tools.MyNotification;
import com.mq.SmartLockAndroid.tools.aboutByte;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView rcvmsg,rcvmsgforgps;
    private Button btn3GOpen, btn3GClose, btnGetGPS, getLocation,btnBluetoothOpen,btnBluetoothClose,btnAutoBluetooth,alarm;
private EditText WriteValues;
    private Intent serviceIntent;
    private MessageBroadcastReceiver receiver;
    private MyTimerTaskopen Taskopen1;
    private  MyTimerTaskyuyin Taskyuyin;
    private TextToSpeech texttospeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitView();

        //开启长连接服务
        serviceIntent = new Intent(this, MinaService.class);
        startService(serviceIntent);
        registerBroadcast();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 适配android M，检查权限
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNeedRequestPermissions(permissions)) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
        }
    }

    private void InitView() {
        WriteValues=findViewById(R.id.WriteValues);
        btn3GOpen = findViewById(R.id.btn3GOpen);
        alarm= findViewById(R.id.alarm);
        btn3GOpen.setOnClickListener(this);
        btnBluetoothOpen = findViewById(R.id.btnBluetoothOpen);
        btnBluetoothOpen.setOnClickListener(this);
        btnBluetoothClose = findViewById(R.id.btnBluetoothClose);
        btnBluetoothClose.setOnClickListener(this);
        btnAutoBluetooth = findViewById(R.id.btnAutoBluetooth);
        btnAutoBluetooth.setOnClickListener(this);
        alarm.setOnClickListener(this);
        btn3GClose = findViewById(R.id.btn3GClose);
        btn3GClose.setOnClickListener(this);
        btnGetGPS = findViewById(R.id.btnGetGPS);
        btnGetGPS.setOnClickListener(this);
        getLocation = findViewById(R.id.getLocation);
        getLocation.setOnClickListener(this);
        rcvmsg = findViewById(R.id.rcvmsg);
        rcvmsgforgps = findViewById(R.id.rcvmsgforgps);
        texttospeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // 如果装载TTS引擎成功
                if (status == TextToSpeech.SUCCESS) {
                    // 设置使用美式英语朗读
                    int result = texttospeech.setLanguage(Locale.US);
                    // 如果不支持所设置的语言
                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                            && result != TextToSpeech.LANG_AVAILABLE) {
                        Log.d("ff", "TTS暂时不支持这种语言的朗读！");
                    }
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn3GOpen:
                byte[] msg = Codes.OPEN;
                String name = "mq";
                String sessionId = "CONN_9527";
                serviceCONN(msg, name, sessionId);
                break;
            case R.id.alarm:
                byte[] msg17 = Codes.alarm;
                String name17 = "mq";
                String sessionId17 = "CONN_9527";
                serviceCONN(msg17, name17, sessionId17);
                if(alarm.getText().toString().trim().equals("已关闭设备保护"))
                {
                    startAlarm();
                    alarm.setText("已开启设备保护");
                }
               else
                {
                    endAlarm();
                    alarm.setText("已关闭设备保护");
                }
                break;
            case R.id.btn3GClose:
                byte[] msg2 = Codes.CLOSE;
                String name2 = "mq";
                String sessionId2 = "CONN_9527";
                serviceCONN(msg2, name2, sessionId2);
                break;
            case R.id.btnBluetoothOpen:
                byte[] msg5 = Codes.relayOPEN;
                String name5 = "mq";
                String sessionId5 = "CONN_9527";
                serviceCONN(msg5, name5, sessionId5);
                break;
            case R.id.btnBluetoothClose:
                byte[] msg6 = Codes.relayCLOSE;
                String name6 = "mq";
                String sessionId6 = "CONN_9527";
                serviceCONN(msg6, name6, sessionId6);
                break;
            case R.id.btnAutoBluetooth:
                if(btnAutoBluetooth.getText().equals("蓝牙自动开锁 关"))
                {
                    btnAutoBluetooth.setText("蓝牙自动开锁 开");
                    byte[] msg7 = Codes.autoBluetoothOpen;
                    String name7 = "mq";
                    String sessionId7 = "CONN_9527";
                    serviceCONN(msg7, name7, sessionId7);
                }
                else
                {
                    btnAutoBluetooth.setText("蓝牙自动开锁 关");
                    byte[] msg7 = Codes.autoBluetoothClose;
                    String name7 = "mq";
                    String sessionId7 = "CONN_9527";
                    serviceCONN(msg7, name7, sessionId7);
                }
                break;
            case R.id.btnGetGPS:
                if(btnGetGPS.getText().equals("正在获取GPS"))
                {
                    btnGetGPS.setText("已关闭获取GPS");
                    byte[] msg3 = Codes.closeAutoGetGPS;
                    String name3 = "mq";
                    String sessionId3 = "CONN_9527";
                    serviceCONN(msg3, name3, sessionId3);
                }
                else
                {
                    btnGetGPS.setText("正在获取GPS");
                    byte[] msg4 = Codes.autoGetGPS;
                    String name4 = "mq";
                    String sessionId4 = "CONN_9527";
                    serviceCONN(msg4, name4, sessionId4);
                }
                break;
            case R.id.getLocation:
                Intent intent = new Intent(this, location_Activity.class);
                startActivity(intent);
                break;

        }
    }







    private void serviceCONN(byte[] bytes, String username, String sessionId) {
        //将byte数组转化成字符串
        String data_msg = Arrays.toString(aboutByte.bytesToInt(bytes));
        //服务器的url
        String url = "http://39.96.68.13:8080/SmartLockServer/servlet/AppControlServlet";
        //将数据拼接起来
        String data = "username=" + username + "&sessionId=" + sessionId + "&code=" + data_msg;
        String[] str = new String[]{url, data};

        //发出一个请求
        new HttpAsyncTask(MainActivity.this, new HttpAsyncTask.PriorityListener() {

            @Override
            public void setActivity(int code) {
                switch (code) {
                    case 200:
                        //如果返回的resultCode是200,那么说明APP的数据传送成功，并成功解析返回的json数据
                        Toast.makeText(MainActivity.this, "发送数据成功", Toast.LENGTH_SHORT).show();
                        break;
                    case 202:
                        Toast.makeText(MainActivity.this, "设备离线状态", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "网络传输异常", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        }).execute(str);
    }


    //动态注册广播
    private void registerBroadcast() {
        receiver = new MessageBroadcastReceiver();
        IntentFilter filter = new IntentFilter("com.ssy.mina.broadcast");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    //检查权限
    private boolean isNeedRequestPermissions(List<String> permissions) {
        // 定位精确位置
        addPermission(permissions, Manifest.permission.ACCESS_FINE_LOCATION);
        // 存储权限
        addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // 读取手机状态
        addPermission(permissions, Manifest.permission.READ_PHONE_STATE);
        return permissions.size() > 0;
    }

    private void addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
        }
    }

    public void clear(View view) {
        rcvmsg.setText("");
    }

    public void push(View view) {
        //TODO PUSH



        String username = "mq";
        String sessionId = "CONN_9527";



        //将byte数组转化成字符串
        String data_msg = WriteValues.getText().toString().trim();
        //服务器的url
        String url = "http://39.96.68.13:8080/SmartLockServer/servlet/AppControlServlet";
        //将数据拼接起来
        String data = "username=" + username + "&sessionId=" + sessionId + "&code=" + data_msg;
        String[] str = new String[]{url, data};
        //发出一个请求
        new HttpAsyncTask(MainActivity.this, new HttpAsyncTask.PriorityListener() {

            @Override
            public void setActivity(int code) {
                switch (code) {
                    case 200:
                        //如果返回的resultCode是200,那么说明APP的数据传送成功，并成功解析返回的json数据
                        Toast.makeText(MainActivity.this, "发送数据成功", Toast.LENGTH_SHORT).show();
                        break;
                    case 202:
                        Toast.makeText(MainActivity.this, "设备离线状态", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "网络传输异常", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        }).execute(str);
    }

    public void push1(View view) {
        String username = "mq";
        String sessionId = "CONN_9527";



        //将byte数组转化成字符串
        String data_msg = "open";
        //服务器的url
        String url = "http://39.96.68.13:8080/SmartLockServer/servlet/AppControlServlet";
        //将数据拼接起来
        String data = "username=" + username + "&sessionId=" + sessionId + "&code=" + data_msg;
        String[] str = new String[]{url, data};
        //发出一个请求
        new HttpAsyncTask(MainActivity.this, new HttpAsyncTask.PriorityListener() {

            @Override
            public void setActivity(int code) {
                switch (code) {
                    case 200:
                        //如果返回的resultCode是200,那么说明APP的数据传送成功，并成功解析返回的json数据
                        Toast.makeText(MainActivity.this, "发送数据成功", Toast.LENGTH_SHORT).show();
                        break;
                    case 202:
                        Toast.makeText(MainActivity.this, "设备离线状态", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "网络传输异常", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        }).execute(str);
    }

    public void push2(View view) {
        String username = "mq";
        String sessionId = "CONN_9527";



        //将byte数组转化成字符串
        String data_msg = "close";
        //服务器的url
        String url = "http://39.96.68.13:8080/SmartLockServer/servlet/AppControlServlet";
        //将数据拼接起来
        String data = "username=" + username + "&sessionId=" + sessionId + "&code=" + data_msg;
        String[] str = new String[]{url, data};
        //发出一个请求
        new HttpAsyncTask(MainActivity.this, new HttpAsyncTask.PriorityListener() {

            @Override
            public void setActivity(int code) {
                switch (code) {
                    case 200:
                        //如果返回的resultCode是200,那么说明APP的数据传送成功，并成功解析返回的json数据
                        Toast.makeText(MainActivity.this, "发送数据成功", Toast.LENGTH_SHORT).show();
                        break;
                    case 202:
                        Toast.makeText(MainActivity.this, "设备离线状态", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "网络传输异常", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        }).execute(str);
    }

    //接收发送的广播
    private class MessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//TODO 收到车锁端消息
            String msg = intent.getStringExtra("message");
            Log.d("SendAsyncTask", msg);
            if (msg.contains("latitude")||msg.contains("longitude")||msg.contains("radius")) {
                rcvmsgforgps.setText("收到坐标   " +System.currentTimeMillis()+ "\n");

            } else{
                rcvmsg.append(msg.trim() + "\n");
        }
            //Toast.makeText(MainActivity.this, "车锁 3G端 发送过来的数据：" , Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出时关掉长连接服务
        stopService(serviceIntent);
    }
    class MyTimerTaskopen extends TimerTask {
        @Override
        public void run() {
            //每次需要执行的代码放到这里面。
            Log.d("ee","过了十秒");
            MyNotification notify = new MyNotification(getApplicationContext());
            notify.MyNotification("智能车锁", "设备状态异常 请打开地图", R.drawable.lock, "addroom", "增加房间", 5, "增加");

            }
        }

    class MyTimerTaskyuyin extends TimerTask {

        @Override
        public void run() {
            texttospeech.speak("设备异常 请及时确认 设备异常 请及时确认 设备异常 请及时确认", TextToSpeech.QUEUE_ADD,null);
            //texttospeech.speak("别偷我了 我不值钱   别偷我了 我不值钱   别偷我了 我不值钱", TextToSpeech.QUEUE_ADD,null);
        }
    }
    //TODO 定时
    private void startAlarm() {
        java.util.Timer timeropen = new java.util.Timer(true);
        java.util.Timer timeryuyin = new java.util.Timer(true);
        if (Taskopen1 != null) {
            Taskopen1.cancel();  //将原任务从队列中移除
        }
        Taskopen1 = new MyTimerTaskopen();  // 新建一个任务
        timeropen.schedule(Taskopen1, 3000);


        if (Taskyuyin != null) {
            Taskyuyin.cancel();  //将原任务从队列中移除
        }
        Taskyuyin = new MyTimerTaskyuyin();  // 新建一个任务
        timeryuyin.schedule(Taskyuyin, 7000);

    }

    private void endAlarm() {

        if (Taskopen1 != null) {
            Taskopen1.cancel();  //将原任务从队列中移除
        } else
            Toast.makeText(getApplicationContext(), "设备保护生效，无需关闭！", Toast.LENGTH_SHORT).show();
        if (Taskyuyin != null) {
            Taskyuyin.cancel();  //将原任务从队列中移除
        } else
            Toast.makeText(getApplicationContext(), "设备保护生效，无需关闭！", Toast.LENGTH_SHORT).show();
    }


}
