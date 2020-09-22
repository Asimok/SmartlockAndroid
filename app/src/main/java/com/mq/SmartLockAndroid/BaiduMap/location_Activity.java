package com.mq.SmartLockAndroid.BaiduMap;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.mq.SmartLockAndroid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class location_Activity extends AppCompatActivity {
    BaiduMap mBaiduMap;
    BitmapDescriptor mCurrentMarker;
    int pointnum = 0;
    double sumdistance = 0;
    List<LatLng> points;
    boolean isFirstLoc = true; // 是否首次定位
    PowerManager.WakeLock wakeLock = null;
    private location_Activity.MessageBroadcastReceiver receiver;
    private MapView mMapView;
    private TextView gpsinfo, distance;
    private Button requestLocButton;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口
//原有BDLocationListener接口暂时同步保留。具体介绍请参考后文第四步的说明
    private SensorManager mSensorManager;
    private double mCurrentLat = 0.0; //精度
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    private MyLocationData locData;
    // 普通折线，点击时改变宽度
    private Polyline mPolyline;

    @SuppressLint({"InvalidWakeLockTag", "WakelockTimeout"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("智能车锁实时位置");
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();
        points = new ArrayList<>();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务


        //初始化布局
        initView(this);
        //初始化地图设置
        initMapOption();
        //初始化定位配置
        // initLocationOption();
        //绘制轨迹
        initLine();
        registerBroadcast();

        // 点击polyline的事件响应
        mBaiduMap.setOnPolylineClickListener(new BaiduMap.OnPolylineClickListener() {
            @Override
            public boolean onPolylineClick(Polyline polyline) {
                if (polyline == mPolyline) {
                    //polyline.setWidth(20);
                    Toast.makeText(getApplicationContext(), "点数："
                            + polyline.getPoints().size()
                            + ",width:" + polyline.getWidth(), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }


    private void initLine() {
    }

    /**
     * 添加线、文字
     */
    public void addCustomElementsDemo(List<LatLng> points1) {
        if(points1.size()>2) {
            // 添加普通折线绘制
            OverlayOptions ooPolyline = new PolylineOptions().width(10).color(0xFFFF00FF).points(points1);
            mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
            if (pointnum < 4) {
                // 添加文字
                // LatLng llText = new LatLng(mCurrentLat, mCurrentLon);
                OverlayOptions ooText = new TextOptions()
                        .bgColor(0xAAFFFF00)
                        .fontSize(40)
                        .fontColor(0xFFFF00FF)
                        .text("MQ test")
                        .rotate(-30)
                        .position(points.get(0));
                mBaiduMap.addOverlay(ooText);
            }
        }
    }

    // 初始化View
    private void initView(Context context) {

        setContentView(R.layout.activity_location_);
        mMapView = findViewById(R.id.bmapView);
        requestLocButton = findViewById(R.id.button1);
        gpsinfo = findViewById(R.id.gpsinfo);
        distance = findViewById(R.id.distance);
        requestLocButton.setText("定位模式：普通");
        View.OnClickListener btnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        requestLocButton.setText("定位模式：跟随");
                        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        break;
                    case COMPASS:
                        requestLocButton.setText("定位模式：普通");
                        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder1 = new MapStatus.Builder();
                        builder1.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
                        break;
                    case FOLLOWING:
                        requestLocButton.setText("定位模式：罗盘");
                        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        break;
                    default:
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);
    }

    //初始化地图设置
    private void initMapOption() {
        mBaiduMap = mMapView.getMap();
        //卫星白图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启地图的定位图层
        mBaiduMap.setMyLocationEnabled(true);

        //自定义地定位显示样式
        //定位模式跟随
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.overlook(0);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

    }

    @Override
    protected void onResume() {
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }


    //动态注册广播
    private void registerBroadcast() {
        receiver = new location_Activity.MessageBroadcastReceiver();
        IntentFilter filter = new IntentFilter("com.ssy.mina.broadcast");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void statrAnalyze(String msg) {
        pointnum++;
        //points.clear();
        //遍历解析
        JSONArray jsonArray1 = null;
        try {
            jsonArray1 = new JSONArray(msg);

            for (int i = 0; i < jsonArray1.length(); i++) {

                JSONObject jsonObj = jsonArray1.getJSONObject(i);
                mCurrentLat = jsonObj.getDouble("latitude");
                mCurrentLon = jsonObj.getDouble("longitude");
                mCurrentAccracy = (float) jsonObj.getDouble("radius");
                float direction = (float) jsonObj.getDouble("direction");
                gpsinfo.setText(String.format("%s   %s  %s", mCurrentLat, mCurrentLon, mCurrentAccracy));
                // 设置初始位置坐标
                locData = new MyLocationData.Builder()
                        .accuracy(mCurrentAccracy)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(direction).latitude(mCurrentLat)
                        .longitude(mCurrentLon).build();

                mBaiduMap.setMyLocationData(locData);

                //首次定位缩放地图
                if (isFirstLoc) {
                    isFirstLoc = false;
                    LatLng ll = new LatLng(mCurrentLat, mCurrentLon);
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll).zoom(18.0f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }

                mBaiduMap.setMyLocationData(locData);
                // 界面加载时添加绘制图层
                LatLng p1 = new LatLng(mCurrentLat, mCurrentLon);
                points.add(p1);
                if (points.size() == 1) {
                    distance.setText(String.format("距离  0m"));
                } else {
                    //计算距离
                    double distence = DistanceUtil.getDistance(points.get(points.size() - 2), points.get(points.size() - 1));
                    sumdistance = sumdistance + abs(distence);
                    distance.setText(String.format("%.02f m", sumdistance));
                }
                if (pointnum > 2) {
                    //画折线
                    addCustomElementsDemo(points);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //接收发送的广播
    private class MessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String msg = intent.getStringExtra("message");
            Log.d("SendAsyncTask", msg);
            if (msg.contains("latitude")) {
                Toast.makeText(location_Activity.this, "location_Activity 收到坐标", Toast.LENGTH_SHORT).show();
                statrAnalyze(msg);
            } else
                Toast.makeText(location_Activity.this, "location_Activity " + msg.trim(), Toast.LENGTH_SHORT).show();

        }


    }

}
