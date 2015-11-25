package com.lingmutec.buqing2009;

import android.app.Activity;
//import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

/**
 * Created by buqing2009 on 15-11-16.
 */
public class BaiduMapFragment extends Fragment implements View.OnClickListener {
    private MapView mMapView = null;
    private BaiduMap bdMap;
    //    private MapController mMapController = null;
//    private Toast mToast=null;
//    private BMapManager mBMapManager=null;
    private boolean firstLocation;
    private BitmapDescriptor mCurrentMarker;
    private MyLocationConfiguration config;
    private LocationClient mLocationClient = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getActivity().getApplicationContext());
        return inflater.inflate(R.layout.baidumap, null);
    }

    @Override

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        setContentView(R.layout.baidumap);
        //获取地图控件引用
        mMapView = (MapView) getActivity().findViewById(R.id.bmapView);
        bdMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15f);
        bdMap.setMapStatus(msu);


        //新建drone的marker
        BitmapDescriptor droneMarker = BitmapDescriptorFactory.fromResource(R.drawable.drone_marker);


        // 定位初始化
        mLocationClient = new LocationClient(this.getActivity().getApplicationContext());
        firstLocation = true;

        // 设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);

        BitmapDescriptor myMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.download);
        MyLocationConfiguration config = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.FOLLOWING, true, myMarker);

        Button btn_sel_map_type = (Button) getActivity().findViewById(R.id.mtype_select_bottom);
        Button btn_locate_map = (Button) getActivity().findViewById(R.id.mlocate_bottom);

        btn_sel_map_type.setOnClickListener(this);
        btn_locate_map.setOnClickListener(this);

        mLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                // map view 销毁后不在处理新接收的位置
                if (location == null || mMapView == null)
                    return;
                // 构造定位数据
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                                // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                // 设置定位数据
                bdMap.setMyLocationData(locData);

                // 第一次定位时，将地图位置移动到当前位置
                if (firstLocation) {
                    firstLocation = false;
                    LatLng xy = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(xy);
                    bdMap.animateMapStatus(status);
                }
            }
        });

        //下面获取Dronekit Fragment的GPS信息
//        FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
//        DronekitFragment dronekitFragment = (DronekitFragment) fragmentManager.findFragmentById(R.id.dronekit);
//        if (dronekitFragment.isGPSReturn()) {
//            Double[] gpsPos = dronekitFragment.getGPSPos();
//            if (gpsPos[0] != null && gpsPos[1] != null) {
//                LatLng dronePos = new LatLng(gpsPos[0], gpsPos[1]);
//                OverlayOptions options = new MarkerOptions().position(dronePos).icon(droneMarker);
//                bdMap.addOverlay(options);
//            }
//        }

    }

    @Override
    public void onStart() {
        // 如果要显示位置图标,必须先开启图层定位
        bdMap.setMyLocationEnabled(true);
        super.onStart();
    }

    @Override
    public void onStop() {
        // 关闭图层定位
        bdMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    protected void selectMap() {
        if (bdMap.getMapType() == BaiduMap.MAP_TYPE_NORMAL) {
            bdMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        } else {
            bdMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        }
    }

    protected void blu_locate() {
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mtype_select_bottom:
                selectMap();
                break;
            case R.id.mlocate_bottom:
                blu_locate();
                break;
        }
    }
}
