package cn.com.watchman.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.linked.erfli.library.base.BaseActivity;
import com.linked.erfli.library.utils.SharedUtil;
import com.linked.erfli.library.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.com.watchman.R;
import cn.com.watchman.bean.DinatesBean;
import cn.com.watchman.bean.DinatesDaoImpl;
import cn.com.watchman.bean.GPSBean;
import cn.com.watchman.service.GPSService;
import cn.com.watchman.utils.Distance;
import cn.com.watchman.utils.MyRequest;
import cn.com.watchman.utils.PointsUtil;
import cn.com.watchman.utils.SensorEventHelper;
import cn.com.watchman.utils.WMyUtils;

/**
 * 文件名：MoveShowActivity
 * 描    述：平滑移动轨迹
 * 作    者：stt
 * 时    间：2017.6.8
 * 版    本：V1.1.2
 */

public abstract class MoveShowActivity extends BaseActivity implements LocationSource, AMapLocationListener {
    protected MapView mapView;
    protected AMap aMap;
    private DinatesDaoImpl dinatesDao;
    private SensorEventHelper mSensorHelper;
    private boolean mFirstFix = false;
    private Marker mLocMarker;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private OnLocationChangedListener mListener;
    private Circle mCircle;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    public static final String LOCATION_MARKER_FLAG = "mylocation";
    protected List<DinatesBean> dinatesList = new ArrayList<>();
    private boolean isSend = true;
    private int currentCount = 0, totalCount;
    private Intent intent;
    private long[] time = {0, 10, 30, 1 * 60, 5 * 60, 10 * 60};
    private int[] compareWork = {100, 200, 400, 800, 1500};
    private int[] compareBic = {300, 600, 1200, 2400, 4800};
    private int[] compareCar = {500, 1500, 3000, 6000, 30000};
    protected ToggleButton togBtn;


    @Override
    protected void setView() {
        setContentView(R.layout.activity_move);
    }

    @Override
    protected void setDate(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.move_map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        dinatesDao = new DinatesDaoImpl(this);
    }

    @Override
    protected void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        //定位方法
        locate();
        togBtn = (ToggleButton) findViewById(R.id.move_toggle);
        togBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mListener = null;
                    if (mlocationClient != null) {
                        mlocationClient.stopLocation();
                        mlocationClient.onDestroy();
                    }
                    mlocationClient = null;
                    trajectory();
                } else {
                    locate();
                }
            }
        });
    }

    public void clearInfo(View view) {
        dinatesList = dinatesDao.find();
        if (dinatesList.size() > 0) {
            for (DinatesBean bean : dinatesList) {
                dinatesDao.delete(bean.getId());
            }
        }
    }

    protected void addPolylineInPlayGround() {
        List<LatLng> list = readLatLngs();
        List<LatLng> lists = readLatLng();
        aMap.addPolyline(new PolylineOptions().setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.custtexture))
                .addAll(list)
                .useGradient(true)
                .width(18));
        aMap.addPolyline(new PolylineOptions().setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.custtexture_dotted))
                .addAll(lists)
                .useGradient(true)
                .width(18));
    }

    /**
     * 取到Sqlite的数据，把点位add到mark上
     *
     * @return
     */
    protected List<LatLng> readLatLngs() {
        List<LatLng> points = new ArrayList<>();
        for (int i = 0; i < dinatesList.size(); i++) {
            if ((int) dinatesList.get(i).getLongitude() != 0 || (int) dinatesList.get(i).getLatitude() != 0) {
                //这里判断前一个点位和目前点位时间差在一定范围画虚线
                if ((i + 1) != dinatesList.size() && Math.abs(dinatesList.get(i).getTime() - dinatesList.get(i + 1).getTime()) <= 60) {
                    points.add(new LatLng(dinatesList.get(i).getLatitude(), dinatesList.get(i).getLongitude()));
                    points.add(new LatLng(dinatesList.get(i + 1).getLatitude(), dinatesList.get(i + 1).getLongitude()));
                }
            }
        }
        return points;
    }

    protected List<LatLng> readLatLng() {
        List<LatLng> points = new ArrayList<>();
        for (int i = 0; i < dinatesList.size(); i++) {
            if ((int) dinatesList.get(i).getLongitude() != 0 || (int) dinatesList.get(i).getLatitude() != 0) {
                //这里判断前一个点位和目前点位时间差在一定范围画虚线
                if ((i + 1) != dinatesList.size() && Math.abs(dinatesList.get(i).getTime() - dinatesList.get(i + 1).getTime()) > 60) {
                    points.add(new LatLng(dinatesList.get(i).getLatitude(), dinatesList.get(i).getLongitude()));
                    points.add(new LatLng(dinatesList.get(i + 1).getLatitude(), dinatesList.get(i + 1).getLongitude()));
                }
            }

        }
        return points;
    }

    /**
     * 定位方法
     */
    protected void locate() {
        setUpMap();
        mSensorHelper = new SensorEventHelper(this);
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
    }

    /**
     * 轨迹方法
     */
    protected void trajectory() {
        dinatesList = dinatesDao.rawQuery("select * from t_gps where time > ?", new String[]{WMyUtils.getTimesmorning()});

        if (dinatesList.size() > 0) {
            addPolylineInPlayGround();
            //实线的方法
            List<LatLng> points = readLatLngs();
            LatLngBounds.Builder b = LatLngBounds.builder();
            for (int i = 0; i < points.size(); i++) {
                b.include(points.get(i));
            }
            LatLngBounds bounds = b.build();
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            LatLng drivePoint = points.get(0);
            Pair<Integer, LatLng> pair = PointsUtil.calShortestDistancePoint(points, drivePoint);
            points.set(pair.first, drivePoint);

            //虚线的方法
            List<LatLng> pointx = readLatLng();
            LatLngBounds.Builder bx = LatLngBounds.builder();
            for (int i = 0; i < pointx.size(); i++) {
                bx.include(pointx.get(i));
            }
            LatLngBounds boundsx = bx.build();
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsx, 100));
            LatLng drivePointx = pointx.get(0);
            Pair<Integer, LatLng> pairx = PointsUtil.calShortestDistancePoint(pointx, drivePointx);
            pointx.set(pairx.first, drivePointx);
            if (isSend) {
                isSend = false;
                initMarker();
            }
        }
    }

    /**
     * 设置一些amap的属性
     */
    protected void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();
            mSensorHelper.setCurrentMarker(null);
            mSensorHelper = null;
        }
        mapView.onPause();
        deactivate();
        mFirstFix = false;
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (mLocMarker != null) {
            mLocMarker.destroy();
        }
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                LatLng location = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                if (!mFirstFix) {
                    mFirstFix = true;
                    addCircle(location, aMapLocation.getAccuracy());//添加定位精度圆
                    addMarker(location);//添加定位图标
                    mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
                } else {
                    mCircle.setCenter(location);
                    mCircle.setRadius(aMapLocation.getAccuracy());
                    mLocMarker.setPosition(location);
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(location));
                }
                if (isSend) {
                    trajectory();
                }

                /**
                 * 下边的逻辑是用来写当在地图界面的时候还在行走状态，路线要实时画出来
                 */
                GPSBean gpsBean = new GPSBean(aMapLocation.getLongitude(), aMapLocation.getLatitude());
                if ((int) aMapLocation.getLongitude() != 0 || (int) aMapLocation.getLatitude() != 0) {
                    if (Double.parseDouble(String.valueOf(aMapLocation.getAccuracy())) > 0 && Double.parseDouble(String.valueOf(aMapLocation.getAccuracy())) < 25) {
                        if (Distance.isCompare(this, gpsBean)) {
                            MyRequest.gpsRequest(this, gpsBean);
                            dinatesDao.insert(new DinatesBean(aMapLocation.getLongitude(), aMapLocation.getLatitude(), System.currentTimeMillis() / 1000));
                            SharedUtil.setString(this, "longitude", String.valueOf(aMapLocation.getLongitude()));
                            SharedUtil.setString(this, "latitude", String.valueOf(aMapLocation.getLatitude()));
                            trajectory();
                        }
                    }
                }
            }
        } else {
            String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
            Log.e("AmapErr", errText);
        }
    }

    private void sendMessage(AMapLocation location) {
        GPSBean gpsBean = new GPSBean(location.getLongitude(), location.getLatitude());
        MyRequest.gpsRequest(this, gpsBean);
        currentCount++;
        totalCount = SharedUtil.getInteger(getApplicationContext(), "totalCount", 0) + 1;
        SharedUtil.setInteger(getApplicationContext(), "totalCount", totalCount);
        intent.putExtra("currentCount", currentCount);
        intent.putExtra("totalCount", totalCount);
        sendBroadcast(intent);
        if (String.valueOf(location.getLatitude()).length() > 9 || String.valueOf(location.getLongitude()).length() > 10) {
            dinatesDao.insert(new DinatesBean(location.getLongitude(), location.getLatitude(), System.currentTimeMillis() / 1000));
            SharedUtil.setString(this, "longitude", String.valueOf(location.getLongitude()));
            SharedUtil.setString(this, "latitude", String.valueOf(location.getLatitude()));
            SharedUtil.setLong(this, "compareTime", System.currentTimeMillis() / 1000);
            trajectory();
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    private void addCircle(LatLng latlng, double radius) {
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(FILL_COLOR);
        options.strokeColor(STROKE_COLOR);
        options.center(latlng);
        options.radius(radius);
        mCircle = aMap.addCircle(options);
    }

    protected void addMarker(LatLng latlng) {
        if (mLocMarker != null) {
            return;
        }
        MarkerOptions options = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(), R.mipmap.navi_map_gps_locked)));
        options.anchor(0.5f, 0.5f);
        options.position(latlng);
        mLocMarker = aMap.addMarker(options);
        mLocMarker.setTitle(LOCATION_MARKER_FLAG);
    }

    protected abstract void initMarker();
}