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

public class IntervalInputDialog extends Dialog {

    private EditText intervalEditText;
    private OnIntervalSetListener onIntervalSetListener;

    public IntervalInputDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interval_input_dialog);

        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText("请输入拨打间隔（s），不要超过60s");

        intervalEditText = findViewById(R.id.intervalEditText);
        intervalEditText.setImeOptions(EditorInfo.IME_ACTION_DONE); // 设置输入法的确定按钮
        intervalEditText.requestFocus(); // 获取焦点弹出软键盘

        Button confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String intervalStr = intervalEditText.getText().toString();
                if (!intervalStr.isEmpty()) {
                    int interval = Integer.parseInt(intervalStr);
                    if (onIntervalSetListener != null) {
                        onIntervalSetListener.onIntervalSet(interval);
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

    public void setOnIntervalSetListener(OnIntervalSetListener listener) {
        this.onIntervalSetListener = listener;
    }

    public interface OnIntervalSetListener {
        void onIntervalSet(int interval);
    }
}

