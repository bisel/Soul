package com.soulfriends.meditation.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.soulfriends.meditation.R;
import com.soulfriends.meditation.databinding.PsychologyListBinding;
import com.soulfriends.meditation.util.ResultListener;
import com.soulfriends.meditation.util.UtilAPI;
import com.soulfriends.meditation.viewmodel.PsychologyListViewModel;
import com.soulfriends.meditation.viewmodel.PsychologyListViewModelFactory;

public class PsychologyListActivity extends AppCompatActivity implements ResultListener {

    private PsychologyListBinding binding;
    private PsychologyListViewModel viewModel;
    private ViewModelStore viewModelStore = new ViewModelStore();
    private PsychologyListViewModelFactory psychologyListViewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_psychology_list);
        binding.setLifecycleOwner(this);

        if (psychologyListViewModelFactory == null) {
            psychologyListViewModelFactory = new PsychologyListViewModelFactory(this, this);
        }
        viewModel = new ViewModelProvider(this.getViewModelStore(), psychologyListViewModelFactory).get(PsychologyListViewModel.class);
        binding.setViewModel(viewModel);


        UtilAPI.s_activity = this;
    }

    @Override
    public void onSuccess(Integer id, String message) {
        switch (id) {
            case R.id.ic_close: {
                // 닫기 버튼

                finish();

                //Intent intent = new Intent(this, MainActivity.class);
                //startActivity(intent);

            }
            break;
            case R.id.iv_feeling_button: {
                // 기분 어떤신가요?

                Intent intent = new Intent(this, PsychologyFeelingTestActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.iv_color_button: {
                // 컬러 심리 검사

                Intent intent = new Intent(this, PsychologyColorTestActivity.class);
                startActivity(intent);
            }
            break;
        }
    }

    @Override
    public void onFailure(Integer id, String message) {

    }
}