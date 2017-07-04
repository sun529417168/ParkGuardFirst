package cn.com.problem.app;

import android.app.Application;
import android.content.Context;

import com.linked.erfli.library.utils.NetWorkUtils;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import cn.com.problem.R;

/**
 * Created by 志强 on 2017.5.2.
 */

public class ProblemApplication extends Application {
    private static final String TAG = "Init";
    private static Context context;
    public static int mNetWorkState;
    public static ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        initData();
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.mipmap.login_logo)//加载开始默认的图片
                .showImageForEmptyUri(R.mipmap.login_logo)     //url爲空會显yg7示该图片
                .showImageOnFail(R.mipmap.image_on_fail)                //加载图片出现问题
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

    public void initData() {
        mNetWorkState = NetWorkUtils.getNetWorkState(this);
    }

    public static Context getContext() {
        return context;
    }


}
