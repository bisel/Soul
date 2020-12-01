package com.soulfriends.meditation.view.nested;

import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.soulfriends.meditation.databinding.ChildItemBinding;
import com.soulfriends.meditation.model.MediationShowContents;
import com.soulfriends.meditation.util.ItemClickListener;
import com.soulfriends.meditation.util.UtilAPI;

import java.util.ArrayList;
import java.util.List;

public class ChildItemAdapter extends RecyclerView.Adapter {

    private List<ChildItemViewModel> list;
    private Context context;
    private LayoutInflater inflater;

    private ItemClickListener listener;

    private long mLastClickTime = 0;

    public ChildItemAdapter(List<MediationShowContents> list_data, Context context, ItemClickListener listener) {

        this.listener = listener;
        if(this.list == null) {

            this.list = new ArrayList<ChildItemViewModel>();
            for (MediationShowContents data : list_data) {

                ChildItemViewModel childItemViewModel = new ChildItemViewModel(data.entity);
                this.list.add(childItemViewModel);
            }
        }

        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        if (inflater == null) {
            inflater = LayoutInflater.from(viewGroup.getContext());
        }

        ChildItemBinding childItemBinding = ChildItemBinding.inflate(inflater, viewGroup, false);

        return new ChildViewHolder(childItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder ViewHolder, int position) {

        ChildViewHolder childViewHolder = (ChildViewHolder) ViewHolder;

        ChildItemViewModel childItemViewModel = list.get(position);
        childViewHolder.bind(list.get(position));

        //String str = childItemViewModel.entity.getValue().thumbnail;

        //if(childItemViewModel.meditationContents.thumbnail_uri == null)
        if(childItemViewModel.meditationContents.thumbnail_uri == null)
        {
            UtilAPI.load_imageEX(this.context, childItemViewModel.entity.getValue().thumbnail, childViewHolder.getChildItemBinding().imgChildItem, childItemViewModel.meditationContents);
        }
        else
        {
            Uri uri = Uri.parse(childItemViewModel.meditationContents.thumbnail_uri);
            UtilAPI.showImage(this.context, uri, childViewHolder.getChildItemBinding().imgChildItem);
        }

        //Glide.with(this.context).load(childItemViewModel.entity.getValue().thumbnail).into(childViewHolder.getChildItemBinding().imgChildItem);

        // 배치 처리
        String releasedate = childItemViewModel.entity.getValue().releasedate;

        long day = UtilAPI.GetDay_Date(releasedate);

        if(day > 7)
        {
            childViewHolder.getChildItemBinding().ivBadge.setVisibility(View.GONE);
        }
        else
        {
            // new
            childViewHolder.getChildItemBinding().ivBadge.setVisibility(View.VISIBLE);
        }


        FrameLayout frame = childViewHolder.getChildItemBinding().frameLayout;
        frame.setOnClickListener(v -> {

            if (SystemClock.elapsedRealtime() - mLastClickTime < UtilAPI.button_delay_time){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            listener.onItemClick((Object) childItemViewModel.entity.getValue(), position);
        });
    }

    @Override
    public int getItemCount() {

        return list.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {

        private ChildItemBinding childItemBinding;

        public ChildViewHolder(ChildItemBinding childItemBinding) {
            super(childItemBinding.getRoot());
            this.childItemBinding = childItemBinding;
        }

        public void bind(ChildItemViewModel childItemViewModel) {
            this.childItemBinding.setViewModel(childItemViewModel);
        }

        public ChildItemBinding getChildItemBinding() {
            return childItemBinding;
        }
    }

}