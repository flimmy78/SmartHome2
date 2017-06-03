package com.heiman.smarthome.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.heiman.baselibrary.Constant;
import com.heiman.baselibrary.GwBaseActivity;
import com.heiman.baselibrary.http.HttpManage;
import com.heiman.baselibrary.mode.XlinkDevice;
import com.heiman.baselibrary.utils.SmartHomeUtils;
import com.heiman.smarthome.MyApplication;
import com.heiman.smarthome.R;
import com.heiman.utils.LogUtil;
import com.heiman.utils.UsefullUtill;
import com.heiman.widget.button.FButton;
import com.heiman.widget.edittext.ClearEditText;

import org.apache.http.Header;

public class ShareDeviceActivity extends GwBaseActivity {

    private ImageView imgBack;
    private TextView txtTitle;
    private ImageView imgMore;
    private ClearEditText editInvitedUser;
    private TextView txtShareDevice;
    private ImageView imgDevice;
    private FButton btnSubmit;


    private static final int MSG_INIT_IMGDEVICE_DEVICE_NAME = 10000;
    private static final int MSG_SHARE_DEVICE_FAIL = 10001;
    private static final int MSG_SHARE_DEVICE_SUCCEED = 10002;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT_IMGDEVICE_DEVICE_NAME:
                    if (device != null) {
                        setDeviceImgName(device);
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_INIT_IMGDEVICE_DEVICE_NAME, 50);
                    }
                    break;
                case MSG_SHARE_DEVICE_SUCCEED:
                    dismissHUMProgress();
                    Toast.makeText(ShareDeviceActivity.this, R.string.send_share_invition_succeed, Toast.LENGTH_LONG).show();
                    break;
                case MSG_SHARE_DEVICE_FAIL:
                    dismissHUMProgress();
                    if (msg.obj == null) {
                        Toast.makeText(ShareDeviceActivity.this, getString(R.string.send_share_invition_fail) + "！", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ShareDeviceActivity.this, getString(R.string.send_share_invition_fail) + ":" + msg.obj.toString(), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_device);
        View head = findViewById(R.id.share_device_head);
        imgBack = (ImageView) head.findViewById(R.id.title_bar_return);
        txtTitle = (TextView) head.findViewById(R.id.title_bar_title);
        imgMore = (ImageView) head.findViewById(R.id.title_bar_more);
        editInvitedUser = (ClearEditText) findViewById(R.id.edit_invited_user);
        btnSubmit = (FButton) findViewById(R.id.btn_submit);
        txtShareDevice = (TextView) findViewById(R.id.txt_shared_device);
        imgDevice = (ImageView) findViewById(R.id.img_device);

        imgMore.setVisibility(View.GONE);
        txtTitle.setText(R.string.txt_share_device);
        imgBack.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(MSG_INIT_IMGDEVICE_DEVICE_NAME);
    }

    @Override
    public void deviceData(String dataString) {
        LogUtil.e("dataString:" + dataString);
    }

    @Override
    public void onClickListener(View v) {
        if (!UsefullUtill.judgeClick(R.layout.activity_share_device, 500)) {
            LogUtil.e("点击过快！");
            return;
        }
        switch (v.getId()) {
            case R.id.title_bar_return:
                onBackPressed();
                break;
            case R.id.btn_submit:
                String user = editInvitedUser.getText().toString();
                if (TextUtils.isEmpty(user)) {
                    Toast.makeText(ShareDeviceActivity.this, R.string.input_invited_user, Toast.LENGTH_LONG).show();
                    return;
                }
                HttpManage.getInstance().shareDevice(MyApplication.getMyApplication(), user, device.getDeviceId(), "app", shareDeviceCallback);
                showHUDProgress();
                break;
        }
    }

    private void setDeviceImgName(XlinkDevice xlinkDevice){
        txtShareDevice.setText(getString(R.string.txt_share_device) + ":" + xlinkDevice.getDeviceName());
        switch (xlinkDevice.getDeviceType()) {
            case Constant.DEVICE_TYPE.DEVICE_WIFI_RC:
                imgDevice.setImageResource(R.drawable.device_rc);
                break;
            case Constant.DEVICE_TYPE.DEVICE_WIFI_GATEWAY:
                imgDevice.setImageResource(R.drawable.device_gw);
                break;
            case Constant.DEVICE_TYPE.DEVICE_WIFI_PLUGIN:
                imgDevice.setImageResource(R.drawable.device_plug);
                break;
            case Constant.DEVICE_TYPE.DEVICE_WIFI_METRTING_PLUGIN:
                imgDevice.setImageResource(R.drawable.device_e_plug);
                break;
            case Constant.DEVICE_TYPE.DEVICE_WIFI_AIR:
                imgDevice.setImageResource(R.drawable.device_e_plug);
                break;
        }
    }

    private HttpManage.ResultCallback<String> shareDeviceCallback = new HttpManage.ResultCallback<String>() {
        @Override
        public void onError(Header[] headers, HttpManage.Error error) {
            if (error != null) {
                LogUtil.e("Code:" + error.getCode() + SmartHomeUtils.showHttpCode(error.getCode()));
                Message msg = new Message();
                msg.what = MSG_SHARE_DEVICE_FAIL;
                msg.obj = SmartHomeUtils.showHttpCode(error.getCode());
                mHandler.sendMessage(msg);
            }
        }

        @Override
        public void onSuccess(int code, String response) {
            LogUtil.e("code:" + code + "\tresponse:" + response);
            mHandler.sendEmptyMessage(MSG_SHARE_DEVICE_SUCCEED);
        }
    };
}
