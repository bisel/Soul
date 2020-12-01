package com.soulfriends.meditation.dlg;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.soulfriends.meditation.R;
import com.soulfriends.meditation.view.MainActivity;

import androidx.annotation.NonNull;

public class AlertDialog extends Dialog {

    private Context context;
    private Button button;

    private String message;
    private TextView textView;

    public AlertDialog(@NonNull Context context, String message) {
        super(context);
        this.context = context;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_alert);

        textView = findViewById(R.id.tv_message);
        textView.setText(this.message);

        button = findViewById(R.id.button_ok);
        button.setOnClickListener(v->{
            switch (v.getId()) {
                case R.id.button_ok:
                    this.dismiss();
                    break;
            }
        });
    }
}