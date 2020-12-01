package com.soulfriends.meditation.view;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.soulfriends.meditation.R;
import com.soulfriends.meditation.databinding.ContentsinfoBinding;
import com.soulfriends.meditation.model.MeditationContents;
import com.soulfriends.meditation.netservice.NetServiceManager;
import com.soulfriends.meditation.util.ResultListener;
import com.soulfriends.meditation.util.UtilAPI;
import com.soulfriends.meditation.viewmodel.ContentsinfoViewModel;
import com.soulfriends.meditation.viewmodel.ContentsinfoViewModelFactory;

public class ContentsinfoActivity extends AppCompatActivity implements ResultListener {

    private ContentsinfoBinding binding;
    private ContentsinfoViewModel viewModel;

    private ViewModelStore viewModelStore = new ViewModelStore();
    private ContentsinfoViewModelFactory contentsinfoViewModelFactory;

    private MeditationContents meditationContents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contentsinfo);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_contentsinfo);
        binding.setLifecycleOwner(this);

       // meditationContents = new MeditationContents();

//        meditationContents.audio = "스튜어트섬으로 떠나는 환상적인 별자리 여행";
//        meditationContents.narrtor = "이명진";
//        meditationContents.author = "Tamara Levitt text testtestrestwe";
//        meditationContents.favoritecnt = 3;

        meditationContents = NetServiceManager.getinstance().getCur_contents();

        if (contentsinfoViewModelFactory == null) {
            contentsinfoViewModelFactory = new ContentsinfoViewModelFactory(meditationContents,this, this);
        }
        viewModel = new ViewModelProvider(this.getViewModelStore(), contentsinfoViewModelFactory).get(ContentsinfoViewModel.class);
        binding.setViewModel(viewModel);


        // image
        if(!meditationContents.publisher.isEmpty())
        {
            int id_image = -1;
            if(meditationContents.publisher.contains(this.getResources().getString(R.string.publisher_soulfriends)))
            {
                id_image = R.drawable.publisher_soulfriends;
            }
            if(meditationContents.publisher.contains(this.getResources().getString(R.string.publisher_deep)))
            {
                id_image = R.drawable.publisher_deep;
            }
            if(meditationContents.publisher.contains(this.getResources().getString(R.string.publisher_carensia)))
            {
                id_image = R.drawable.publisher_carensia;
            }

            if(id_image != -1) {
                UtilAPI.setImage(this, binding.ivImage, id_image);
            }
        }

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onSuccess(Integer id, String message) {
        switch (id) {
            case R.id.iv_close: {
                // 나가기 버튼

                //Intent intent = new Intent(this, PlayerActivity.class);
                //startActivity(intent);

                finish();
            }
            break;
        }
    }

    @Override
    public void onFailure(Integer id, String message) {

    }
}