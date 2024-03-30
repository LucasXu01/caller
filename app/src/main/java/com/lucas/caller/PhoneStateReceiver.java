package com.lucas.caller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

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
                Toast.makeText(context, "电话已挂断,开始准备拨打下一个", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
