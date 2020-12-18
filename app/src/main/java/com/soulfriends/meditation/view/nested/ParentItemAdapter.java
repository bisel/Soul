package com.soulfriends.meditation.view.nested;


import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.soulfriends.meditation.R;
import com.soulfriends.meditation.databinding.ParentItemBinding;
import com.soulfriends.meditation.databinding.ParentTopItemBinding;
import com.soulfriends.meditation.model.UserProfile;
import com.soulfriends.meditation.netservice.NetServiceManager;
import com.soulfriends.meditation.parser.ResultData;
import com.soulfriends.meditation.util.ItemClickListener;
import com.soulfriends.meditation.util.PreferenceManager;
import com.soulfriends.meditation.util.UtilAPI;

import java.util.List;

public class ParentItemAdapter extends RecyclerView.Adapter {

    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private List list;
    private Context context;
    private LayoutInflater inflater;

    private LayoutInflater inflater_top;

    private ItemClickListener listener;

    private ParentTopViewHolder parentTopViewHolder = null;

    public ParentItemAdapter(List list, Context context, ItemClickListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        if (this.getItemViewType(i) == 0) {
            if (inflater_top == null) {
                inflater_top = LayoutInflater.from(viewGroup.getContext());
            }

            ParentTopItemBinding parentTopItemBinding = ParentTopItemBinding.inflate(inflater_top, viewGroup, false);
            return new ParentTopViewHolder(parentTopItemBinding);
        } else {
            if (inflater == null) {
                inflater = LayoutInflater.from(viewGroup.getContext());
            }

            ParentItemBinding parentItemBinding = ParentItemBinding.inflate(inflater, viewGroup, false);
            return new ParentViewHolder(parentItemBinding);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (list.get(position) instanceof ParentTopItemViewModel) {
            return 0;
        }
        return 1;
    }

    public void UpdateTopItem()
    {
        if(parentTopViewHolder != null) {
            parentTopViewHolder.init(this.context);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder ViewHolder, int i) {

        if (this.getItemViewType(i) == 0) {

            parentTopViewHolder = (ParentTopViewHolder) ViewHolder;

            ParentTopItemViewModel parentTopItemViewModel = (ParentTopItemViewModel) list.get(i);
            parentTopViewHolder.bind(parentTopItemViewModel);

            parentTopViewHolder.init(this.context);

            ImageView imageView = parentTopViewHolder.getParentTopItemBinding().imgChildItem;
            imageView.setOnClickListener(v -> {

                listener.onItemClick((Object) imageView, 0);
            });

        } else {
            ParentViewHolder parentViewHolder = (ParentViewHolder) ViewHolder;

            ParentItemViewModel parentItemViewModel = (ParentItemViewModel) list.get(i);
            parentViewHolder.bind(parentItemViewModel);

            ParentItemBinding parentItemBinding = parentViewHolder.getParentItemBinding();
            LinearLayoutManager layoutManager = new LinearLayoutManager(parentItemBinding.childRecyclerview.getContext(), LinearLayoutManager.HORIZONTAL, false);

            int size = parentItemViewModel.getSizeContest();
            layoutManager.setInitialPrefetchItemCount(size);

            ChildItemAdapter childItemAdapter = new ChildItemAdapter(parentItemViewModel.getMeditationCategory().contests, this.context, listener);
            parentItemBinding.childRecyclerview.setLayoutManager(layoutManager);
            parentItemBinding.childRecyclerview.setAdapter(childItemAdapter);
            parentItemBinding.childRecyclerview.setRecycledViewPool(viewPool);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ParentTopViewHolder extends RecyclerView.ViewHolder {

        private ParentTopItemBinding bind;
        private ParentTopItemViewModel viewModel;

        public ParentTopViewHolder(ParentTopItemBinding parentTopItemBinding) {
            super(parentTopItemBinding.getRoot());
            this.bind = parentTopItemBinding;
        }

        public void bind(ParentTopItemViewModel parentTopItemViewModel) {
            this.bind.setViewModel(parentTopItemViewModel);
            this.viewModel = parentTopItemViewModel;
        }

        public void init(Context context)
        {
            UserProfile userProfile = NetServiceManager.getinstance().getUserProfile();

            // nickname feeling state
            if(userProfile.nickname != null) {

                String strQuest = userProfile.nickname + " " + context.getResources().getString(R.string.feel_state_quest);

                int end_nick = userProfile.nickname.length();

                if(end_nick > 0) {
                    Spannable wordtoSpan = new SpannableString(strQuest);
                    wordtoSpan.setSpan(new ForegroundColorSpan(Color.rgb(179, 179, 227)), 0, end_nick, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    wordtoSpan.setSpan(new ForegroundColorSpan(Color.WHITE), end_nick + 1, strQuest.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    bind.tvNicknameState.setText(wordtoSpan);
                }
            }


            // 심리검사
            // - 24시간 주기로 심리검사 초기화함
            // - 00시를 기준으로 함
            // - 하루 1회 심리검사 권유 문구 표기

            //String psy_reult_time = PreferenceManager.getString(context, "psy_result_time");

            String psy_reult_time = userProfile.finaltestdate;

            if(psy_reult_time != null) {
                if (psy_reult_time.length() > 0) {
                    // 1일 차이 여부
                    long day = UtilAPI.GetDay_Date(psy_reult_time);
                    if (day > 0) {
                        // 심리 검사 유도하도록 문구 나오도록 한다.
                        userProfile.emotiontype = 0; // 초기화 한다.

                        NetServiceManager.getinstance().setOnRecvValProfileListener(new NetServiceManager.OnRecvValProfileListener() {
                            @Override
                            public void onRecvValProfile(boolean validate) {
                                if (validate == true) {
                                    int xx = 0;
                                } else {
                                    int yy = 0;
                                }
                            }
                        });

                        NetServiceManager.getinstance().sendValProfile(userProfile);
                    }
                }
            }

           // nickname feeling state
            ResultData resultData = NetServiceManager.getinstance().getResultData(userProfile.emotiontype);
            viewModel.setFeelstate(resultData.state);

            //
            int res_id_1 = context.getResources().getIdentifier(resultData.emotionimg, "drawable", context.getPackageName());
            //UtilAPI.setImage(context, bind.imgChildItem, res_id_1);
            UtilAPI.setImageResource(bind.imgChildItem, res_id_1);

            if(NetServiceManager.getinstance().getUserProfile().emotiontype == 0){
                this.bind.retrylayout.setVisibility(View.GONE);
            }else{
                this.bind.retrylayout.setVisibility(View.VISIBLE);
            }

        }

        public ParentTopItemBinding getParentTopItemBinding() {
            return bind;
        }
    }

    static class ParentViewHolder extends RecyclerView.ViewHolder {

        private ParentItemBinding parentItemBinding;

        public ParentViewHolder(ParentItemBinding parentItemBinding) {
            super(parentItemBinding.getRoot());
            this.parentItemBinding = parentItemBinding;
        }

        public void bind(ParentItemViewModel parentItemViewModel) {
            this.parentItemBinding.setViewModel(parentItemViewModel);
        }

        public ParentItemBinding getParentItemBinding() {
            return parentItemBinding;
        }
    }
}