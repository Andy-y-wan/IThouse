package com.danmo.ithouse.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.danmo.commonapi.CommonApi;
import com.danmo.commonapi.event.login.LoginEvent;
import com.danmo.ithouse.R;
import com.danmo.ithouse.base.BaseActivity;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.shareboard.SnsPlatform;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import static com.umeng.commonsdk.stateless.UMSLEnvelopeBuild.mContext;

/**
 * Created by user on 2017/12/26.
 */

public class LoginMainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "xxx";
    private TextInputEditText etLoginName, etLoginPasswd;
    private TextView tvForgetPasswd, tvLoginRegister;
    private ImageView ivLoginWeixin, ivLoginQQ, ivLoginWeibo, ivLoginTaobao;
    private Button btnLogin;

    public ArrayList<SnsPlatform> platforms = new ArrayList<SnsPlatform>();
    private SHARE_MEDIA[] list = {SHARE_MEDIA.QQ, SHARE_MEDIA.SINA, SHARE_MEDIA.WEIXIN};

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login_layout;
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginMainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void initViews() {
        super.initViews();
        etLoginName = findViewById(R.id.et_login_name);
        etLoginPasswd = findViewById(R.id.et_login_password);
        tvForgetPasswd = findViewById(R.id.tv_forget_password);
        tvLoginRegister = findViewById(R.id.tv_login_register);
        ivLoginQQ = findViewById(R.id.iv_login_qq);
        ivLoginTaobao = findViewById(R.id.iv_login_taobao);
        ivLoginWeibo = findViewById(R.id.iv_login_weibo);
        ivLoginWeixin = findViewById(R.id.iv_login_weixin);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(this);
        tvForgetPasswd.setOnClickListener(this);
        tvLoginRegister.setOnClickListener(this);
        ivLoginQQ.setOnClickListener(this);
        ivLoginTaobao.setOnClickListener(this);
        ivLoginWeibo.setOnClickListener(this);
        ivLoginWeixin.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginEvent event) {
        if (event.isOk()) {
            CommonApi.getSingleInstance().getMe();//EventBus回调到需要用户信息的页面，比如：UserFragment
            toastShort("登录成功");
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            String msg = "请重试";
            switch (event.getCode()) {
                case -1:
                    msg = "请检查网络连接";
                    break;
                case 400:
                case 401:
                    msg = "请检查用户名和密码是否正确";
                    break;
            }
            toastShort("登录失败：" + msg);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnLogin) {
            String name = etLoginName.getText().toString();
            String passwd = etLoginPasswd.getText().toString();
            if (TextUtils.isEmpty(name)) {
                toastShort("请输入用户名");
                return;
            }
            if (TextUtils.isEmpty(passwd)) {
                toastShort("请输入密码");
                return;
            }
            CommonApi.getSingleInstance().login(name, passwd);
        } else if (v == ivLoginWeixin) {
            authorization(SHARE_MEDIA.WEIXIN);

        } else if (v == ivLoginQQ) {
            authorization(SHARE_MEDIA.QQ);

        } else if (v == ivLoginWeibo) {
            authorization(SHARE_MEDIA.SINA);

        } else if (v == ivLoginTaobao) {

        }
    }

    //授权
    private void authorization(SHARE_MEDIA share_media) {
        UMShareAPI.get(this).getPlatformInfo(this, share_media, new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
                Log.d(TAG, "onStart " + "授权开始");
            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                Log.d(TAG, "onComplete " + "授权完成");

                //sdk是6.4.4的,但是获取值的时候用的是6.2以前的(access_token)才能获取到值,未知原因
                String uid = map.get("uid");
                String openid = map.get("openid");//微博没有
                String unionid = map.get("unionid");//微博没有
                String access_token = map.get("access_token");
                String refresh_token = map.get("refresh_token");//微信,qq,微博都没有获取到
                String expires_in = map.get("expires_in");
                String name = map.get("name");
                String gender = map.get("gender");
                String iconurl = map.get("iconurl");

                Intent intent = new Intent();
                Bundle bundle=new Bundle();
                bundle.putSerializable("map",(Serializable) map);
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK,intent);
                finish();

                //拿到信息去请求登录接口。。。
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                Log.d(TAG, "onError " + "授权失败");
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                Log.d(TAG, "onCancel " + "授权取消");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult");
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }
}
