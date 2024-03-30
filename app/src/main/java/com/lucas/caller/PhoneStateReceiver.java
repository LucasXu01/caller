package com.lucas.caller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;

public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取电话管理器
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            // 获取当前电话状态
            int state = telephonyManager.getCallState();
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                // 电话挂断
                Log.e("xujinjin", "onReceive: " );

                ToastUtils.showShort("电话已挂断,开始准备拨打下一个");
                EventBus.getDefault().post(new MessageEvent());

            }
        }
    }
}
