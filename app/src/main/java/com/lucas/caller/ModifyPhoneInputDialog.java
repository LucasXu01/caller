package com.lucas.caller;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ModifyPhoneInputDialog extends Dialog {

    private EditText intervalEditText;
    private OnModifySetListener onIntervalSetListener;

    private String phone;

    public ModifyPhoneInputDialog(Context context, String phone) {
        super(context);
        this.phone = phone;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_phone_input_dialog);



        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText("请输入修改手机号");

        intervalEditText = findViewById(R.id.intervalEditText);
        intervalEditText.setImeOptions(EditorInfo.IME_ACTION_DONE); // 设置输入法的确定按钮
        intervalEditText.requestFocus(); // 获取焦点弹出软键盘
        intervalEditText.setText(phone.toString());

        Button confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String intervalStr = intervalEditText.getText().toString();
                if (!intervalStr.isEmpty()) {
                    if (onIntervalSetListener != null) {
                        onIntervalSetListener.onModify(intervalStr);
                    }
                }
                dismiss();
            }
        });

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // 自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public void setOnIntervalSetListener(OnModifySetListener listener) {
        this.onIntervalSetListener = listener;
    }

    public interface OnModifySetListener {
        void onModify(String newPhone);
    }
}

