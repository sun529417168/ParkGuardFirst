package cn.com.parkguard.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.github.mzule.activityrouter.router.Routers;
import com.linked.erfli.library.base.BaseActivity;
import com.linked.erfli.library.base.MyTitle;
import com.linked.erfli.library.myserviceutils.MyConstant;
import com.linked.erfli.library.myserviceutils.MyServiceUtils;
import com.linked.erfli.library.utils.MyUtils;
import com.linked.erfli.library.utils.NetWorkUtils;
import com.linked.erfli.library.utils.SharedUtil;
import com.linked.erfli.library.utils.StatusBarUtils;
import com.linked.erfli.library.utils.ToastUtil;
import com.linked.erfli.library.weight.ReportedGridview;

import java.util.ArrayList;

import cn.com.parkguard.R;
import cn.com.parkguard.adapter.HomeAdapter;
import cn.com.parkguard.bean.HomeBean;
import cn.com.watchman.service.GPSService;
import cn.com.watchman.utils.DialogUtils;
import cn.com.watchman.utils.NotifyUtils;
import cn.com.watchman.utils.WMyUtils;


/**
 * 文件名：HomeActivity
 * 描    述：主界面
 * 作    者：stt
 * 时    间：2017.01.04
 * 版    本：V1.0.0
 */
public class HomeActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private TextView netText;
    private ReportedGridview gridView;
    private LinearLayout setting;
    private int image[] = {R.mipmap.home_task, R.mipmap.home_notice, R.mipmap.home_problem, R.mipmap.home_statistical, R.mipmap.home_xungeng, R.mipmap.home_anwen, R.mipmap.ic_map, R.mipmap.home_monitor, R.mipmap.home_anwen};
    private ArrayList<HomeBean> homeArrayList = new ArrayList<>();
    private HomeAdapter homeAdapter;
    private long exitTime;//上一次按退出键时间
    private static final long TIME = 2000;//双击回退键间隔时间
    private NotifyUtils notifyUtils;

    @Override
    protected void setView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void setDate(Bundle savedInstanceState) {
        StatusBarUtils.ff(this, R.color.colorPrimary);
        MyTitle.getInstance().setTitle(this, "移动园区卫士", PGApp, false);
        notifyUtils = new NotifyUtils(this);
        setting = (LinearLayout) findViewById(R.id.home_setter);
        setting.setVisibility(View.VISIBLE);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        ArrayList<HomeBean> homeList = (ArrayList<HomeBean>) JSON.parseArray(MyUtils.getFromAssets(this, "home.txt"), HomeBean.class);
        for (int i = 0; i < homeList.size(); i++) {
            if (homeList.get(i).isIsTrue()) {
                HomeBean homeBean = new HomeBean();
                homeBean.setId(homeList.get(i).getId());
                homeBean.setName(homeList.get(i).getName());
                homeBean.setImageView(image[i]);
                homeArrayList.add(homeBean);
            }
        }
    }

    @Override
    protected void init() {
        netText = (TextView) findViewById(R.id.main_title_net);
        //启动时判断网络状态
        boolean netConnect = this.isNetConnect();
        if (netConnect) {
            netText.setVisibility(View.GONE);
        } else {
            netText.setVisibility(View.VISIBLE);
        }
        gridView = (ReportedGridview) findViewById(R.id.home_gridView);
        homeAdapter = new HomeAdapter(this, homeArrayList);
        gridView.setAdapter(homeAdapter);
        gridView.setOnItemClickListener(this);
        netText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
        if (!MyServiceUtils.isServiceRunning(MyConstant.GPSSERVICE_CLASSNAME, HomeActivity.this)) {
            //Intent startIntent = new Intent(this, MyScoketService.class);
            //startService(startIntent);
//            startServiceDialog();
            if (!WMyUtils.isOpen(this)) {
                DialogUtils.showGPSDialog(this);
                Intent startIntent = new Intent(HomeActivity.this, GPSService.class);
                startService(startIntent);

            } else {
                Intent startIntent = new Intent(HomeActivity.this, GPSService.class);
                startService(startIntent);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                notifyUtils.showButtonNotify(0);
            }
        }
    }

    /**
     * 启动服务
     */
    public void startServiceDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
        dialog.setTitle("启动服务");
        dialog.setMessage("是否要开启巡更服务?");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent startIntent = new Intent(HomeActivity.this, GPSService.class);
                startService(startIntent);
            }
        });
        dialog.setNeutralButton("取消",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                    }
                });
        dialog.show();
    }

    @Override

    public void onNetChange(int netMobile) {
        super.onNetChange(netMobile);
        //网络状态变化时的操作
        if (netMobile == NetWorkUtils.NETWORK_NONE) {
            netText.setVisibility(View.VISIBLE);
        } else {
            netText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (homeArrayList.get(position).getId()) {
            case 101:
                SharedUtil.setBoolean(this, "isTask", true);
                Routers.open(HomeActivity.this, Uri.parse("modularization://task_list"));
                break;
            case 102:
                SharedUtil.setBoolean(this, "isNotice", true);
                Routers.open(HomeActivity.this, Uri.parse("modularization://notice_list"));
                break;
            case 103:
                SharedUtil.setBoolean(this, "isProblem", true);
                Routers.open(HomeActivity.this, Uri.parse("modularization://problem_list"));
                break;
            case 104:
                //统计
                break;
            case 105:
                SharedUtil.setBoolean(this, "isWatchMan", true);
                Routers.open(HomeActivity.this, Uri.parse("modularization://watchman"));
                break;
            case 106:

                break;
            case 107:

                break;
            case 108:
                ToastUtil.show(this, "正在开发中");
//                SharedUtil.setBoolean(this, "isMonitor", true);
//                Routers.open(HomeActivity.this, Uri.parse("modularization://monitor"));
                break;
            case 109:
                Routers.open(HomeActivity.this, Uri.parse("modularization://inspection"));
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > TIME) {
                ToastUtil.show(this, "再按一次返回键退出");
                exitTime = System.currentTimeMillis();
                return true;
            } else {
                PGApp.exit();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean serviceBool = MyServiceUtils.isServiceRunning(MyConstant.GPSSERVICE_CLASSNAME, HomeActivity.this);
        findViewById(R.id.home_sendMessage).setVisibility(serviceBool ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onNetChanges(int i, int b) {
        Log.i("onNetChanges", "homeActivity:" + i);
        notifyUtils.showButtonNotify(i);
    }
}
