package cn.com.parkguard.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linked.erfli.library.base.BaseActivity;
import com.linked.erfli.library.base.MyTitle;

import cn.com.parkguard.R;


/**
 * 文件名：ForgetPasswordActivity
 * 描    述：忘记密码的类
 * 作    者：stt
 * 时    间：2017.01.19
 * 版    本：V1.0.0
 */

public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener {
    private TextView callPhone;

    @Override
    protected void setView() {
        setContentView(R.layout.activity_forget_password);
    }

    @Override
    protected void setDate(Bundle savedInstanceState) {
        MyTitle.getInstance().setTitle(this, "忘记密码", PGApp, true);
    }

    @Override
    protected void init() {
        callPhone = (TextView) findViewById(R.id.forget_password_call);
        callPhone.setOnClickListener(this);
    }

    @Override
    public void onNetChange(int netMobile) {
        super.onNetChange(netMobile);
        MyTitle.getInstance().setNetText(this, netMobile);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                PGApp.finishTop();
                break;
            case R.id.forget_password_call:
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "6566988"));
                startActivity(phoneIntent);
                break;
        }
    }
}
