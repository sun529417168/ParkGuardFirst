package com.linked.erfli.library.application;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.baidu.mapapi.SDKInitializer;
import com.github.mzule.activityrouter.annotation.Modules;
import com.linked.erfli.library.R;
import com.linked.erfli.library.service.LocationService;
import com.linked.erfli.library.utils.EventPool;
import com.linked.erfli.library.utils.MyUtils;
import com.linked.erfli.library.utils.SharedUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * 文件名：MyApplication
 * 描    述：初始化数据
 * 作    者：
 * 时    间：2016.12.30
 * 版    本：V1.0.0
 */
@Modules({"app", "moduleTask", "moduleNotice", "moduleProblem", "moduleWatchMan", "moduleMonitor", "ModuleInspection"})
public class LibApplication extends Application {
    private static final String TAG = "Init";
    private static Context context;
    public static ImageLoader imageLoader = ImageLoader.getInstance();
    public LocationService locationService;
    public Vibrator mVibrator;
    private boolean isMorning;

    @Override
    public void onCreate() {
        super.onCreate();
        isMorning = SharedUtil.getBoolean(this, "isMorning", true);
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(getApplicationContext());
        initCloudChannel(this);
        context = this;
        EventBus.getDefault().register(this);
        initTimeMoring();
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.mipmap.logo)//加载开始默认的图片
                .showImageForEmptyUri(R.mipmap.logo)     //url爲空會显yg7示该图片
                .showImageOnFail(R.mipmap.logo)                //加载图片出现问题
                .cacheInMemory() // 1.8.6包使用时候，括号里面传入参数true
                .cacheOnDisc() // 1.8.6包使用时候，括号里面传入参数true
                .build();
        ImageLoaderConfiguration config2 = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).enableLogging() // Not
                .build();
        imageLoader.init(config2);

    }

    private void initTimeMoring() {
        long morning = MyUtils.getTimeMorning();
        if (isMorning) {
            SharedUtil.setLong(this, "morningTime", morning);
            SharedUtil.setBoolean(this, "isMorning", false);
        }
    }

    public static Context getContent() {
        return context;
    }

    /**
     * 初始化云推送通道
     *
     * @param applicationContext
     */
    private void initCloudChannel(Context applicationContext) {
        PushServiceFactory.init(applicationContext);
        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.register(applicationContext, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i(TAG, "init cloudchannel success");
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.e(TAG, "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });

        // 初始化小米通道，自动判断是否支持小米系统推送，如不支持会跳过注册
//        MiPushRegister.register(applicationContext, "小米AppID", "小米AppKey");
        // 初始化华为通道，自动判断是否支持华为系统推送，如不支持会跳过注册
//        HuaWeiRegister.register(applicationContext);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void logActivityCreate(EventPool.ActivityNotify activityNotify) {
        Log.d("ActivityCreate", activityNotify.activityName);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
