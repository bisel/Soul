package com.soulfriends.meditation.viewmodel;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.soulfriends.meditation.util.ResultListener;
import com.soulfriends.meditation.util.UtilAPI;

public class PsychologyColorTestViewModel extends ViewModel {

    private ResultListener listener;
    private Context context;

    public MutableLiveData<String> getTitle() {
        return title;
    }

    public void setTitle(MutableLiveData<String> title) {
        this.title = title;
    }

    public void setTitle_data(String title) {
        this.title.setValue(title);
    }

    public MutableLiveData<String> getText_1() {
        return text_1;
    }

    public void setText_1(MutableLiveData<String> text_1) {
        this.text_1 = text_1;
    }

    public void setText_1_data(String text_1) {
        this.text_1.setValue(text_1);
    }

    public MutableLiveData<String> getText_2() {
        return text_2;
    }

    public void setText_2(MutableLiveData<String> text_2) {
        this.text_2 = text_2;
    }

    public void setText_2_data(String text_2) {
        this.text_2.setValue(text_2);
    }

    public MutableLiveData<String> getText_3() {
        return text_3;
    }

    public void setText_3(MutableLiveData<String> text_3) {
        this.text_3 = text_3;
    }

    public void setText_3_data(String text_3) {
        this.text_3.setValue(text_3);
    }

    public MutableLiveData<String> getText_4() {
        return text_4;
    }

    public void setText_4(MutableLiveData<String> text_4) {
        this.text_4 = text_4;
    }

    public void setText_4_data(String text_4) {
        this.text_4.setValue(text_4);
    }

    public MutableLiveData<String> title = new MutableLiveData<>();     // title
    public MutableLiveData<String> text_1 = new MutableLiveData<>();    // text_1
    public MutableLiveData<String> text_2 = new MutableLiveData<>();    // text_2
    public MutableLiveData<String> text_3= new MutableLiveData<>();     // text_3
    public MutableLiveData<String> text_4 = new MutableLiveData<>();    // text_4

    public View getView() {
        return view;
    }

    private View view;

    private long mLastClickTime = 0;

    public PsychologyColorTestViewModel(Context mContext, ResultListener listener) {
        this.context = mContext;
        this.listener = listener;

        title.setValue("");
        text_1.setValue("");
        text_2.setValue("");
        text_3.setValue("");
        text_4.setValue("");
    }

    public void OnClick_Select(View view)
    {
        if (SystemClock.elapsedRealtime() - mLastClickTime < UtilAPI.button_delay_time){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        this.view = view;
        this.listener.onSuccess(view.getId(),"Success!");
    }
}
