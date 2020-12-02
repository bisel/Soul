package com.soulfriends.meditation.util;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.soulfriends.meditation.model.MeditationContents;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilAPI {

    public static long button_delay_time = 400;

    public static Activity s_Main_activity;
    public static Activity s_activity;
    public static boolean s_bDestroyPass = false;

    public static boolean s_one_mini = false;

    public static boolean s_bBookmark_update = false;

    public static int player_track_count = 0;


    public static void SetFullScreen(android.view.Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = window.getInsetsController();

            if (controller != null)
                controller.hide(WindowInsets.Type.statusBars());
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        }
    }

    public static void load_imageEX(Context context, String str_uri, ImageView view, MeditationContents meditationContents)
    {
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(str_uri);
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

            @Override
            public void onSuccess(Uri uri) {

                meditationContents.thumbnail_uri = uri.toString();
                showImage(context, uri, view);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                Log.d("Meditation","load_imageEX onFailure : " + str_uri);
                int xx = 0;
            }
        });
    }

    public static void load_image(Context context, String str_uri, ImageView view)
    {
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(str_uri);
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                showImage(context, uri, view);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }

    //static Map<String, String> statesList = new HashMap<>();

    public static void showImage(Context context, Uri uri, ImageView view)
    {
       //statesList.put(uri.toString(),uri.toString());
       Glide.with(view.getContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(view);
    }

    public static void setImage(Context context, ImageView view, int res_id)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(ContextCompat.getDrawable(context, res_id));
        } else {
            view.setBackgroundDrawable(ContextCompat.getDrawable(context, res_id));
        }
    }

    public static void setButtonBackground(Context context, Button button, int res_id)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            button.setBackground(ContextCompat.getDrawable(context, res_id));
        } else {
            button.setBackgroundDrawable(ContextCompat.getDrawable(context, res_id));
        }
    }

    public static long GetDay_Date(String date)
    {
        SimpleDateFormat format_date = new SimpleDateFormat ( "yyyy-MM-dd" );

        long milliseconds_release = 0;
        try {
            Date ReleaseDate = format_date.parse(date);
            milliseconds_release = ReleaseDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date date_now = new Date(System.currentTimeMillis());
        long milliseconds_cur = date_now.getTime();
        long gap_time = milliseconds_cur - milliseconds_release;

        long day = gap_time / (60 * 60 * 24 * 1000);

        return day;
    }

    public static void setMarginBottom(Context context, View v, int bottom) {

        int dp_bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottom, context.getResources().getDisplayMetrics());

        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams)v.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin,
                params.rightMargin, dp_bottom);
    }

    public static void Release_Session()
    {
        s_bBookmark_update = false;
    }
}
