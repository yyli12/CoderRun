package cn.edu.fudan.cs12.coderrun.fragment;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;



import cn.edu.fudan.cs12.coderrun.R;
import cn.edu.fudan.cs12.coderrun.action.UserAction;
import cn.edu.fudan.cs12.coderrun.entity.User;
import cn.edu.fudan.cs12.coderrun.provider.BusProvider;
//Baidu Location
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.BDNotifyListener;//假如用到位置提醒功能，需要import该类
import com.baidu.location.Poi;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;


/**
 * Map fragment used to render map
 * created by HU Chen
 */
public class MapFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    User user;
    MapView mMapView = null;
    BaiduMap baiduMap = null;
    //显示消息
    private Toast mToast;
    // 定位相关声明
    public LocationClient locationClient = null;

    // 是否首次定位
    boolean isFirstLoc = true;
    // 是否手动定位
    boolean isRequst=false;
    //运动轨迹
    List<LatLng> points = new ArrayList<LatLng>();
    List<LatLng> points_tem = new ArrayList<LatLng>();
    //检查GPS开启
    Handler handler = new Handler();
    // 是否停止定位服务
    boolean isStopLocClient = false;
    // 是否开始绘制轨迹
    boolean isTrack = false;


    public BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            baiduMap.setMyLocationData(locData);    //设置定位数据
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            if (isFirstLoc||isRequst) {
                isFirstLoc = false;
                isRequst = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 16);   //设置地图中心点以及缩放
                // MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                baiduMap.animateMapStatus(u);
            }
            if(isTrack){
                points.add(point);
                if (points.size() == 5) {

                    // 这里绘制起点
                    drawStart(points);
                } else if (points.size() > 7) {
                    points_tem = points.subList(points.size() - 4, points.size());
                    OverlayOptions options = new PolylineOptions().color(0xAAFF0000).width(6)
                            .points(points_tem);
                    baiduMap.addOverlay(options);
                }
            }
        }
    };

    public MapFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
        user= UserAction.getCurrentUser();
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
        mMapView.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        BusProvider.getInstance().register(this);
        mMapView.onResume();
    }

    @Override
    public void onDestroy(){
        //退出时销毁定位
        locationClient.stop();
        isStopLocClient = true;
        baiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        user= UserAction.getCurrentUser();
        View v= inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) v.findViewById(R.id.bmapView);
        baiduMap = mMapView.getMap();
        //手动定位
        ImageButton loc = (ImageButton) v.findViewById(R.id.request);
        loc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                requestLocation();
            }
        });

        this.setLocationOption();   //设置定位参数
        locationClient.start(); // 开始定位

        Button but_start = (Button) v.findViewById(R.id.but_start);
        Button but_stop = (Button) v.findViewById(R.id.but_stop);

        //绘制
        handler.postDelayed(new MyRunable(), 3000);
        but_start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isStopLocClient = false;
                isTrack = true;
                if (!locationClient.isStarted()) {
                    locationClient.start();
                }
            }
        });
        // 结束此次运动，绘制终点
        but_stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                isStopLocClient = true;
                if (locationClient.isStarted()) {
                    // 绘制终点
                    isTrack = false;
                    drawEnd(points);
                    locationClient.stop();
                }

            }
        });
        // baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL); // 设置为一般地图

        // baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE); //设置为卫星地图
        // baiduMap.setTrafficEnabled(true); //开启交通图
        return v;
    }

    private void setLocationOption() {
        //开启定位图层
        baiduMap.setMyLocationEnabled(true);
        locationClient = new LocationClient(this.getActivity()); // 实例化LocationClient类
        locationClient.registerLocationListener(myListener); // 注册监听函数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开GPS
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(2000); // 设置发起定位请求的间隔时间为2000ms
        option.setIsNeedAddress(true); // 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true); // 返回的定位结果包含手机机头的方向
        option.setPriority(LocationClientOption.GpsFirst);
        locationClient.setLocOption(option);
    }

    public void requestLocation() {

        if(locationClient != null && locationClient.isStarted()){
            showToast("正在定位......");
            isRequst = true;
            locationClient.requestLocation();
        }else{
            Log.d("log", "locClient is null or not started");
        }
    }

    private void showToast(String msg){
        if(mToast == null){
            mToast = Toast.makeText(this.getActivity(), msg, Toast.LENGTH_SHORT);
        }else{
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    class MyRunable implements Runnable {

        public void run() {
            if (!locationClient.isStarted()) {
                locationClient.start();
            }
            if (!isStopLocClient) {
                handler.postDelayed(this, 3000);
            }

        }

    }

//    /**
//     * 根据数据绘制轨迹
//     */
//    protected void drawMyRoute(List<LatLng> points2) {
//        OverlayOptions options = new PolylineOptions().color(0xAAFF0000)
//                .width(10).points(points2);
//        baiduMap.addOverlay(options);
//    }

    /**
     * 绘制起点，取前n个点坐标的平均值绘制起点
     */
    public void drawStart(List<LatLng> points2) {
        double myLat = 0.0;
        double myLng = 0.0;

        for (LatLng ll : points2) {
            myLat += ll.latitude;
            myLng += ll.longitude;
        }
        LatLng avePoint = new LatLng(myLat / points2.size(), myLng
                / points2.size());
        points.add(avePoint);
        OverlayOptions options = new DotOptions().center(avePoint).color(0xAA00ff00)
                .radius(15);
        baiduMap.addOverlay(options);
    }

    /**
     * 绘制终点。
     */
    protected void drawEnd(List<LatLng> points2) {
        double myLat = 0.0;
        double myLng = 0.0;
        if (points2.size() > 5) {// points肯定大于5，其实不用判断
            for (int i = points2.size() - 5; i < points2.size(); i++) {
                LatLng ll = points2.get(i);
                myLat += ll.latitude;
                myLng += ll.longitude;

            }
            LatLng avePoint = new LatLng(myLat / 5, myLng / 5);
            OverlayOptions options = new DotOptions().center(avePoint).color(0xAAff00ff)
                    .radius(15);
            baiduMap.addOverlay(options);
        }

    }

}
