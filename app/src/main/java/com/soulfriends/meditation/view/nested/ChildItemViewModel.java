package com.soulfriends.meditation.view.nested;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.soulfriends.meditation.model.MeditationContents;

public class ChildItemViewModel extends ViewModel {
    public MutableLiveData<MeditationContents> entity = new MutableLiveData<>();

    public MutableLiveData<String> playtime = new MutableLiveData<>();
    public MutableLiveData<String> title = new MutableLiveData<>();

    public MeditationContents meditationContents;

    public ChildItemViewModel(MeditationContents entity_data) {

        meditationContents = entity_data;

        //thumb_url.setValue("");
        entity.setValue(entity_data);
        title.setValue(entity_data.title);

        // 초
        float play_time_second = Float.valueOf(entity_data.playtime);
        float calc_minute = play_time_second / 60.0f;
        float final_minute = Math.round(calc_minute);
        int minute = (int)final_minute;

        String str_play_time = "";
        if(minute == 0)
        {
            str_play_time = "1분";
        }
        else
        {
            str_play_time =String.valueOf(minute) + "분";
        }
        playtime.setValue(str_play_time);
    }

    public MutableLiveData<MeditationContents> getEntity() {
        return entity;
    }

    public void setEntity(MutableLiveData<MeditationContents> entity) {
        this.entity = entity;
    }
}
