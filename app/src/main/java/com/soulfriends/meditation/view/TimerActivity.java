package com.soulfriends.meditation.view;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.soulfriends.meditation.R;

public class TimerActivity extends AppCompatActivity {

    //private TimePicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

//        picker=(TimePicker)findViewById(R.id.timePicker1);
//        picker.setIs24HourView(true);


        //Get the widgets reference from XML layout
        final TextView tv = (TextView) findViewById(R.id.textView_hour);
        NumberPicker np = (NumberPicker) findViewById(R.id.np_hour);

        //Set TextView text color
        tv.setTextColor(Color.parseColor("#ffd32b3b"));

        //Populate NumberPicker values from minimum and maximum value range
        //Set the minimum value of NumberPicker
        np.setMinValue(0);
        //Specify the maximum value/number of NumberPicker
        np.setMaxValue(24);

        //Gets whether the selector wheel wraps when reaching the min/max value.
        np.setWrapSelectorWheel(true);

        //Set a value change listener for NumberPicker
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Display the newly selected number from picker
               // tv.setText("Selected Number : " + newVal);
            }
        });

        //
        //
        //

        //Get the widgets reference from XML layout
        final TextView tv_m = (TextView) findViewById(R.id.textView_minute);
        NumberPicker np_m = (NumberPicker) findViewById(R.id.np_minute);

        //Set TextView text color
        tv_m.setTextColor(Color.parseColor("#ffd32b3b"));

        //Populate NumberPicker values from minimum and maximum value range
        //Set the minimum value of NumberPicker
        np_m.setMinValue(1);
        //Specify the maximum value/number of NumberPicker
        np_m.setMaxValue(60);

        //Gets whether the selector wheel wraps when reaching the min/max value.
        np_m.setWrapSelectorWheel(true);

        //Set a value change listener for NumberPicker
        np_m.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Display the newly selected number from picker
                //tv_m.setText("Selected Number : " + newVal);
            }
        });
    }
}