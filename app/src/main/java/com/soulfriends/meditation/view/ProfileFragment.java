package com.soulfriends.meditation.view;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.soulfriends.meditation.R;
import com.soulfriends.meditation.databinding.ProfileBinding;
import com.soulfriends.meditation.model.UserProfile;
import com.soulfriends.meditation.netservice.NetServiceManager;
import com.soulfriends.meditation.parser.ResultData;
import com.soulfriends.meditation.util.ResultListener;
import com.soulfriends.meditation.util.UtilAPI;
import com.soulfriends.meditation.viewmodel.ProfileViewModel;
import com.soulfriends.meditation.viewmodel.ProfileViewModelFactory;

public class ProfileFragment extends Fragment implements ResultListener {

    private ProfileBinding binding;
    private ProfileViewModel viewModel;

    private ViewModelStore viewModelStore = new ViewModelStore();
    private ProfileViewModelFactory profileViewModelFactory;

    private UserProfile userProfile;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        binding.setLifecycleOwner(this);

        userProfile = NetServiceManager.getinstance().getUserProfile();

        //userProfile.playtime = 99999;
        //userProfile.sessionnum = 777;

        if (profileViewModelFactory == null) {
            profileViewModelFactory = new ProfileViewModelFactory(userProfile, this.getContext(), this);
        }
        viewModel = new ViewModelProvider(this.getViewModelStore(), profileViewModelFactory).get(ProfileViewModel.class);
        binding.setViewModel(viewModel);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        viewModel.Update();

        if(userProfile.emotiontype > 0) {

            binding.layFeel.setVisibility(View.VISIBLE);
            binding.tvStateQuest.setVisibility(View.VISIBLE);

            // 닉네임 질의
            String strQuest = userProfile.nickname + getResources().getString(R.string.psychology_nickname);

            int end_nick = userProfile.nickname.length();

            Spannable wordtoSpan = new SpannableString(strQuest);
            wordtoSpan.setSpan(new ForegroundColorSpan(Color.rgb(179, 179, 227)), 0, end_nick, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            wordtoSpan.setSpan(new ForegroundColorSpan(Color.WHITE), end_nick + 1, strQuest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.tvStateQuest.setText(wordtoSpan);

            // 이모티콘
            ResultData resultData = NetServiceManager.getinstance().getResultData(userProfile.emotiontype);

            if (resultData.id > 0) {
                String str_id = "";
                if (resultData.id < 10) {
                    str_id = "0" + String.valueOf(resultData.id);
                } else {
                    str_id = String.valueOf(resultData.id);
                }
                String strEmoti = "emoti_" + str_id;
                int res_id_1 = this.getResources().getIdentifier(strEmoti, "drawable", getContext().getPackageName());
                UtilAPI.setImage(getContext(), binding.ivIcon, res_id_1);
            }
        }
        else
        {
            binding.layFeel.setVisibility(View.GONE);
            binding.tvStateQuest.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSuccess(Integer id, String message) {

    }

    @Override
    public void onFailure(Integer id, String message) {

    }
}