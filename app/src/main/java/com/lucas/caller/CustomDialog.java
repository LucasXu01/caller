package com.lucas.caller;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomDialog extends AlertDialog {

    private Drawable imageDrawable;
    private String message;

    public CustomDialog(Context context, Drawable imageDrawable, String message) {
        super(context);
        this.imageDrawable = imageDrawable;
        this.message = message;
    }

    public CustomDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);

        ImageView imageView = findViewById(R.id.imageView);
        TextView textView = findViewById(R.id.textView);

//        imageView.setImageDrawable(imageDrawable);
//        textView.setText(message);
    }
}

