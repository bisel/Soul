package com.soulfriends.meditation.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.soulfriends.meditation.R;
import com.soulfriends.meditation.databinding.PsychologyColorTestBinding;
import com.soulfriends.meditation.netservice.NetServiceManager;
import com.soulfriends.meditation.parser.QuestionData;
import com.soulfriends.meditation.util.ResultListener;
import com.soulfriends.meditation.viewmodel.PsychologyColorTestViewModel;
import com.soulfriends.meditation.viewmodel.PsychologyColorTestViewModelFactory;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class PsychologyColorTestActivity extends AppCompatActivity implements ResultListener {

    private PsychologyColorTestBinding binding;
    private PsychologyColorTestViewModel viewModel;
    private ViewModelStore viewModelStore = new ViewModelStore();
    private PsychologyColorTestViewModelFactory psychologyColorTestViewModelFactory;

    private boolean bCheckResult = false;
    private int select_item_id = -1;
    private View viewPrev = null;

    ArrayList<QuestionData> questionData_list;
    ArrayList<Integer> list_page_selectid;

    private int curPageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_psychology_color_test);
        binding.setLifecycleOwner(this);

        if (psychologyColorTestViewModelFactory == null) {
            psychologyColorTestViewModelFactory = new PsychologyColorTestViewModelFactory(this, this);
        }
        viewModel = new ViewModelProvider(this.getViewModelStore(), psychologyColorTestViewModelFactory).get(PsychologyColorTestViewModel.class);
        binding.setViewModel(viewModel);

        list_page_selectid = new ArrayList<Integer>();
        questionData_list = xmlParser(R.xml.questiondata_result);





//        Integer ss = 3;
//        int sss = (int)ss;

        curPageIndex = 0;

        for(int i = 0; i < questionData_list.size(); i++) {
            list_page_selectid.add(-1);
        }

        MakePage(0);
    }

    XmlResourceParser parser;
    private ArrayList<QuestionData> xmlParser(int res_id)  {

        ArrayList<QuestionData> arrayList = new ArrayList<QuestionData>();
        Resources res = this.getResources();
        parser = res.getXml(res_id);

        try {
            int eventType = parser.getEventType();
            QuestionData data = null;

            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String startTag = parser.getName();
                        if(startTag.equals("colorquestiondata")) {
                            data = new QuestionData();
                        }
                        if(startTag.equals("id")) {
                            data.id = Integer.parseInt(parser.nextText());
                        }
                        if(startTag.equals("page")) {
                            data.page = Integer.parseInt(parser.nextText());
                        }
                        if(startTag.equals("question")) {
                            data.question = parser.nextText();
                        }
                        if(startTag.equals("answer1_id")) {
                            data.answer1_id = Integer.parseInt(parser.nextText());
                        }
                        if(startTag.equals("answer1_text")) {
                            data.answer1_text = parser.nextText();
                        }
                        if(startTag.equals("answer1_file")) {
                            data.answer1_file = parser.nextText();
                        }
                        if(startTag.equals("answer2_id")) {
                            data.answer2_id = Integer.parseInt(parser.nextText());
                        }
                        if(startTag.equals("answer2_text")) {
                            data.answer2_text = parser.nextText();
                        }
                        if(startTag.equals("answer2_file")) {
                            data.answer2_file = parser.nextText();
                        }
                        if(startTag.equals("answer3_id")) {
                            data.answer3_id = Integer.parseInt(parser.nextText());
                        }
                        if(startTag.equals("answer3_text")) {
                            data.answer3_text = parser.nextText();
                        }
                        if(startTag.equals("answer3_file")) {
                            data.answer3_file = parser.nextText();
                        }
                        if(startTag.equals("answer4_id")) {
                            data.answer4_id = Integer.parseInt(parser.nextText());
                        }
                        if(startTag.equals("answer4_text")) {
                            data.answer4_text = parser.nextText();
                        }
                        if(startTag.equals("answer4_file")) {
                            data.answer4_file = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        String endTag = parser.getName();
                        if(endTag.equals("colorquestiondata")) {
                            arrayList.add(data);
                        }
                        break;
                }
                eventType = parser.next();
            }

        }catch(XmlPullParserException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onSuccess(Integer id, String message) {
        switch (id) {
            case R.id.ic_close: {
                // 닫기 버튼

                if(prev_page())
                {

                }
                else
                {
                    Intent intent = new Intent(this, PsychologyListActivity.class);
                    startActivity(intent);

                    finish();
                }

            }
            break;
            case R.id.bt_result: {
                // 다음 버튼
                if (bCheckResult) {

                    // 현재 저장
                    list_page_selectid.set(curPageIndex, select_item_id);

                    // 다음
                    if(next_page())
                    {

                    }
                    else
                    {
                        NetServiceManager.getinstance().checkColorTest(list_page_selectid);

                        finish();

                        Intent intent = new Intent(this, PsychologyResultActivity.class);
                        startActivity(intent);
                    }
                }
            }
            break;
            case R.id.lay_1: {
                // 1번 선택
                SeleteItem(id, 1);
            }
            break;
            case R.id.lay_2: {
                // 2번 선택
                SeleteItem(id, 2);
            }
            break;
            case R.id.lay_3: {
                // 3번 선택
                SeleteItem(id, 3);
            }
            break;
            case R.id.lay_4: {
                // 1번 선택
                SeleteItem(id, 4);
            }
            break;
        }
    }

    private void MakePage(int page_index)
    {
        QuestionData data = questionData_list.get(page_index);

        viewModel.setTitle_data(data.question);

        viewModel.setText_1_data(data.answer1_text);
        viewModel.setText_2_data(data.answer2_text);
        viewModel.setText_3_data(data.answer3_text);
        viewModel.setText_4_data(data.answer4_text);

        int res_id_1 = this.getResources().getIdentifier(data.answer1_file, "drawable", this.getPackageName());
        setImage(binding.ivIcon1, res_id_1);

        int res_id_2 = this.getResources().getIdentifier(data.answer2_file, "drawable", this.getPackageName());
        setImage(binding.ivIcon2, res_id_2);

        int res_id_3 = this.getResources().getIdentifier(data.answer3_file, "drawable", this.getPackageName());
        setImage(binding.ivIcon3, res_id_3);

        int res_id_4 = this.getResources().getIdentifier(data.answer4_file, "drawable", this.getPackageName());
        setImage(binding.ivIcon4, res_id_4);


        // 이전 버튼 비선택 처리
        if (select_item_id != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewPrev.setBackground(ContextCompat.getDrawable(this, R.drawable.color_bg));
            } else {
                viewPrev.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.color_bg));
            }
        }

        // 이미 선택된 것이 있다면
        int _select_item_id = list_page_selectid.get(page_index);
        if(_select_item_id > 0)
        {
            // 기존에 선택된 아이템이 있다.
            View view_layout = null;

            switch (_select_item_id) {
                case 1: {
                    view_layout = binding.lay1;
                }
                break;
                case 2: {
                    view_layout = binding.lay2;
                }
                break;
                case 3: {
                    view_layout = binding.lay3;
                }
                break;
                case 4: {
                    view_layout = binding.lay4;
                }
                break;
            }

            if(view_layout != null)
            {
                viewPrev = view_layout;

                // 선택 표시
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view_layout.setBackground(ContextCompat.getDrawable(this, R.drawable.color_bg_selected));
                } else {
                    view_layout.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.color_bg_selected));
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    binding.btResult.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_colcr_able));
                } else {
                    binding.btResult.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.btn_colcr_able));
                }

                bCheckResult = true;
            }
        }
        else
        {
            if (select_item_id != -1) {
                // 결과 버튼 활성화
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    binding.btResult.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_colcr_disable));
                } else {
                    binding.btResult.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.btn_colcr_disable));
                }

                bCheckResult = false;
            }
        }
    }

    private void setImage(ImageView view, int res_id)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(ContextCompat.getDrawable(this, res_id));
        } else {
            view.setBackgroundDrawable(ContextCompat.getDrawable(this, res_id));
        }
    }

    private boolean prev_page()
    {
        curPageIndex--;
        if(curPageIndex < 0)
        {
            // 화면 전환

            return false;
        }

        MakePage(curPageIndex);
        select_item_id = list_page_selectid.get(curPageIndex);
        return true;
    }

    private boolean next_page()
    {
        curPageIndex++;
        if(curPageIndex > questionData_list.size() - 1)
        {
            // 결과

            return false;
        }

        MakePage(curPageIndex);
        select_item_id = list_page_selectid.get(curPageIndex);

        return true;
        //SeleteItem(int id, int index)
    }


    private void SeleteItem(int id, int index)
    {
        // 이전 버튼 비선택 처리
        if (select_item_id != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewPrev.setBackground(ContextCompat.getDrawable(this, R.drawable.color_bg));
            } else {
                viewPrev.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.color_bg));
            }
        }

        // 첫 이모티콘 선택시
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewModel.getView().setBackground(ContextCompat.getDrawable(this, R.drawable.color_bg_selected));
        } else {
            viewModel.getView().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.color_bg_selected));
        }

        viewPrev = viewModel.getView();
        select_item_id = index;

        // 결과 버튼 활성화
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            binding.btResult.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_colcr_able));
        } else {
            binding.btResult.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.btn_colcr_able));
        }

        bCheckResult = true;

    }

    @Override
    public void onFailure(Integer id, String message) {

    }
}