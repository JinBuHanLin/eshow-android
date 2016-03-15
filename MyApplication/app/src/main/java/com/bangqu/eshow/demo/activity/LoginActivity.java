package com.bangqu.eshow.demo.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.MaterialMenuView;
import com.bangqu.eshow.demo.R;
import com.bangqu.eshow.demo.bean.Enum_CodeType;
import com.bangqu.eshow.demo.bean.Enum_ThirdType;
import com.bangqu.eshow.demo.common.CommonActivity;
import com.bangqu.eshow.demo.common.Global;
import com.bangqu.eshow.demo.common.SharedPrefUtil;
import com.bangqu.eshow.demo.network.ESResponseListener;
import com.bangqu.eshow.demo.network.NetworkInterface;
import com.bangqu.eshow.demo.view.LoginAutoCompleteEdit;
import com.bangqu.eshow.fragment.ESProgressDialogFragment;
import com.bangqu.eshow.util.ESDialogUtil;
import com.bangqu.eshow.util.ESLogUtil;
import com.bangqu.eshow.util.ESStrUtil;
import com.bangqu.eshow.util.ESToastUtil;
import com.bangqu.eshow.util.ESViewUtil;
import com.umeng.socialize.Config;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * 登录页面
 * Created by daikting on 16/2/24.
 */
@EActivity(R.layout.activity_login)
public class LoginActivity extends CommonActivity {
    private Context mContext = LoginActivity.this;
    @ViewById(R.id.rlBack)
    RelativeLayout mRlMenu;
    @ViewById(R.id.material_back_button)
    MaterialMenuView mMaterialBackButton;
    @ViewById(R.id.tvTitle)
    TextView mTvTitle;
    @ViewById(R.id.tvSubTitle)
    TextView mTvSubTitle;
    @ViewById(R.id.etTel)
    LoginAutoCompleteEdit mEtTel;
    @ViewById(R.id.etPassword)
    LoginAutoCompleteEdit mEtPassword;
    @ViewById(R.id.btnLogin)
    Button mBtnLogin;
    @ViewById(R.id.tvForgetPW)
    TextView mTvForgetPW;
    @ViewById(R.id.llQQLogin)
    LinearLayout mLlQQLogin;
    @ViewById(R.id.llWechatLogin)
    LinearLayout mLlWechatLogin;
    //友盟分享
    UMShareAPI umShareAPI;

    ESProgressDialogFragment progressDialog;

    String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        ESViewUtil.scaleContentView((RelativeLayout) findViewById(R.id.rlParent));
        mTvTitle.setText(getTitle());
        mTvSubTitle.setVisibility(View.VISIBLE);
        mMaterialBackButton.setState(MaterialMenuDrawable.IconState.ARROW);
        mMaterialBackButton.setVisibility(View.GONE);

        userName = getIntent().getStringExtra(InputPasswordActivity.INTENT_TEL);
        mEtTel.setText(userName);
    }

    @Click(R.id.btnLogin)
    void onLogin() {

        String userName = mEtTel.getText().toString();
        final String password = mEtPassword.getText().toString();

        if (ESStrUtil.isEmpty(userName) && !ESStrUtil.isMobileNo(userName)) {
            ESToastUtil.showToast(mContext, "请输入账号！！");
            return;
        }

        if (ESStrUtil.isEmpty(password)) {
            ESToastUtil.showToast(mContext, "请输入密码！");
            return;
        }

        if (ESStrUtil.strLength(password) > 20) {
            ESToastUtil.showToast(mContext, "密码长度过长！");
            return;
        }
        ESResponseListener responseListener = new ESResponseListener(mContext) {
            @Override
            public void onBQSucess(String esMsg, JSONObject resultJson) {
                try {
                    String userStr = resultJson.getJSONObject("user").toString();
                    ESLogUtil.d(mContext, "Login  userStr:" + userStr);
                    SharedPrefUtil.setUser(mContext, userStr);
                    JSONObject tokenJson = resultJson.getJSONObject("accessToken");
                    String token = tokenJson.getString("accessToken");
                    SharedPrefUtil.setAccesstoken(mContext,token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MainActivity_.intent(mContext).start();
            }

            @Override
            public void onBQNoData() {

            }

            @Override
            public void onBQNotify(String bqMsg) {
                ESToastUtil.showToast(mContext, bqMsg);
            }

            @Override
            public void onStart() {
                progressDialog = ESDialogUtil.showProgressDialog(mContext, Global.LOADING_PROGRESSBAR_ID, "请求数据中...");
            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, String content, Throwable error) {
                progressDialog.dismiss();
                ESToastUtil.showToast(mContext, "请求失败，错误码：" + statusCode);
            }
        };
        NetworkInterface.login(mContext, userName, password, responseListener);
    }

    @Click(R.id.tvForgetPW)
    void onForgetPW() {
        InputTelActivity_.intent(mContext).extra(InputTelActivity.INTENT_ISREGISTER, Enum_CodeType.FINDPASSWORD).start();
//        progressDialog = ESDialogUtil.showProgressDialog(mContext, Global.LOADING_PROGRESSBAR_ID,"正在请求登录中...");
    }

    /**
     * 点击注册
     */
    @Click(R.id.tvSubTitle)
    void onRegister() {
        InputTelActivity_.intent(mContext).extra(InputTelActivity_.INTENT_ISREGISTER, Enum_CodeType.REGISTER).start();
    }

    /**
     * 微信授权登录
     */
    @Click(R.id.llWechatLogin)
    void onWechatLogin() {
        Config.dialog = ProgressDialog.show(mContext, "提示", "正在请求跳转....");
        SHARE_MEDIA platform = SHARE_MEDIA.WEIXIN;
        umShareAPI = UMShareAPI.get(mContext);
        umShareAPI.doOauthVerify(this, platform, umAuthListener);
    }

    /**
     * QQ授权登录
     */
    @Click(R.id.llQQLogin)
    void onQQLogin() {
        Config.dialog = ProgressDialog.show(mContext, "提示", "正在请求跳转....");
        SHARE_MEDIA platform = SHARE_MEDIA.QQ;
        UMShareAPI umShareAPI = UMShareAPI.get(mContext);
        umShareAPI.doOauthVerify(this, platform, umAuthListener);

    }

    /**
     * 授权回调监听
     */
    private UMAuthListener umAuthListener = new UMAuthListener() {
        @Override
        public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
            String token = data.get("access_token");
            Enum_ThirdType thirdType = Enum_ThirdType.QQ;
            if(platform.name().equals(SHARE_MEDIA.WEIXIN)){
                thirdType = Enum_ThirdType.WeChat;
            }else if(platform.name().equals(SHARE_MEDIA.QQ)){
                thirdType = Enum_ThirdType.QQ;
            }
            NetworkInterface.thirdLogin(mContext,token,thirdType,thirdLoginResListener);

        }

        @Override
        public void onError(SHARE_MEDIA platform, int action, Throwable t) {
            ESToastUtil.showToast(mContext, platform.name() + " Authorize fail");

        }

        @Override
        public void onCancel(SHARE_MEDIA platform, int action) {
            ESToastUtil.showToast(mContext, platform.name() + " Authorize cancel");
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (umShareAPI != null) {
            umShareAPI.onActivityResult(requestCode, resultCode, data);
        } else {
            //应用未审核
            ESToastUtil.showToast(mContext, "应用未审核，平台拒绝了应用的授权请求！");
        }
    }

    /**
     * 第三方登录接口回调
     */
    ESResponseListener thirdLoginResListener = new ESResponseListener(mContext) {
        @Override
        public void onBQSucess(String esMsg, JSONObject resultJson) {

        }

        @Override
        public void onBQNoData() {

        }

        @Override
        public void onBQNotify(String bqMsg) {
            ESToastUtil.showToast(mContext, bqMsg);
        }

        @Override
        public void onStart() {
            progressDialog = ESDialogUtil.showProgressDialog(mContext, Global.LOADING_PROGRESSBAR_ID, "请求登录中...");
        }

        @Override
        public void onFinish() {
            progressDialog.dismiss();
        }

        @Override
        public void onFailure(int statusCode, String content, Throwable error) {
            progressDialog.dismiss();
            ESToastUtil.showToast(mContext, "请求失败，错误码：" + statusCode);
        }
    };
}
