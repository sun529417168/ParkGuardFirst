package cn.com.inspection;

import android.os.Bundle;

import com.github.mzule.activityrouter.annotation.Router;
import com.linked.erfli.library.base.BaseActivity;


/**
 * Created by Sultan on 2017/7/18.
 */
@Router("inspection")
public class InspectionActivity extends BaseActivity {
    @Override
    protected void setView() {
        setContentView(R.layout.activity_inspection);
    }

    @Override
    protected void setDate(Bundle savedInstanceState) {

    }

    @Override
    protected void init() {

    }
}
