package com.soulfriends.meditation.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.soulfriends.meditation.R;
import com.soulfriends.meditation.model.MeditationCategory;
import com.soulfriends.meditation.model.MeditationContents;
import com.soulfriends.meditation.model.MeditationShowCategorys;
import com.soulfriends.meditation.model.UserProfile;
import com.soulfriends.meditation.netservice.NetServiceManager;
import com.soulfriends.meditation.util.ItemClickListener;
import com.soulfriends.meditation.util.UtilAPI;
import com.soulfriends.meditation.view.nested.ParentItemAdapter;
import com.soulfriends.meditation.view.nested.ParentItemViewModel;
import com.soulfriends.meditation.view.nested.ParentTopItemViewModel;
import com.soulfriends.meditation.view.player.MeditationAudioManager;

import java.util.ArrayList;
import java.util.List;

public class MusicFragment extends Fragment implements ItemClickListener {

    private UserProfile userProfile;

    private MeditationShowCategorys meditationShowCategorys;

    public MusicFragment() {
    }

    public static MusicFragment newInstance() {
        MusicFragment fragment = new MusicFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_music, container, false);
        RecyclerView ParentRecyclerViewItem = view.findViewById(R.id.parent_recyclerview);

        // 정보 요청
        userProfile = NetServiceManager.getinstance().getUserProfile();

        boolean mIsDoneTest = false;
        if (userProfile.emotiontype == 0) {
        } else {
            mIsDoneTest = true;
        }
        meditationShowCategorys = NetServiceManager.getinstance().reqMediationType(4, mIsDoneTest);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        ParentItemAdapter parentItemAdapter = new ParentItemAdapter(ParentItemList(), container.getContext(), this);

        ParentRecyclerViewItem.setAdapter(parentItemAdapter);
        ParentRecyclerViewItem.setLayoutManager(layoutManager);


        return view;
    }

    private List<ParentItemViewModel> ParentItemList()
    {
        List list = new ArrayList<>();

        // 0
        {
            ParentTopItemViewModel parentTopItemViewModel = new ParentTopItemViewModel(userProfile.nickname, userProfile);
            list.add(parentTopItemViewModel);
        }

        int num = meditationShowCategorys.showcategorys.size();
        for(int i = 0; i < num ; i++) {

            MeditationCategory entity = meditationShowCategorys.showcategorys.get(i);

            ParentItemViewModel parentItemViewModel = new ParentItemViewModel(entity);

            list.add(parentItemViewModel);
        }

        return list;
    }

    @Override
    public void onItemClick(Object obj, int pos)
    {
        if (obj instanceof ImageView) {
            // Top Item 선택 한 경우
            //Toast.makeText(this.getContext(), "Top Item select", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.setClass(getActivity(), PsychologyListActivity.class);
            getActivity().startActivity(intent);
        }
        else {

            MeditationContents meditationContents = (MeditationContents) obj;

            UtilAPI.Start_PlayerCheck(meditationContents.uid);

            // 다른 경우
            if(NetServiceManager.getinstance().getCur_contents() != null ) {
                if (NetServiceManager.getinstance().getCur_contents().uid != meditationContents.uid) {

                    MeditationAudioManager.with(getActivity()).stop();

                    if(MeditationAudioManager.with(getActivity()).getServiceBound()) {
                        MeditationAudioManager.with(getActivity()).unbind();
                    }
                }
            }

            NetServiceManager.getinstance().setCur_contents(meditationContents);

            //String str = meditationContents.uid;
            //Toast.makeText(this.getContext(), str, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.setClass(getActivity(), PlayerActivity.class);
            getActivity().startActivity(intent);
        }
    }
}