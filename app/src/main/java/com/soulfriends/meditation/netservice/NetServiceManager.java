package com.soulfriends.meditation.netservice;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.soulfriends.meditation.R;
import com.soulfriends.meditation.model.MediationShowContents;
import com.soulfriends.meditation.model.MeditationCategory;
import com.soulfriends.meditation.model.MeditationContents;
import com.soulfriends.meditation.model.MeditationShowCategorys;
import com.soulfriends.meditation.model.UserProfile;
import com.soulfriends.meditation.parser.CategoryData;
import com.soulfriends.meditation.parser.ColorData;
import com.soulfriends.meditation.parser.EmotionListData;
import com.soulfriends.meditation.parser.QuestionData;
import com.soulfriends.meditation.parser.ResultData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class NetServiceManager {
    private NetServiceManager() {
    }

    private static class instanceclass {
        private static final NetServiceManager instance = new NetServiceManager();
    }

    public static NetServiceManager getinstance(){
        return instanceclass.instance;
    }

    // member variables
    private final String genre1 = "명상";
    private final String genre2 = "수면";
    private final String genre3 = "음악";
    private final String favoriteCategoryId = "50";
    private final int maxFavoriteContentsNum = 30;

    public ArrayList<MeditationContents> mContentsList = new ArrayList<MeditationContents>();
    private DatabaseReference mfbDBRef;
    private UserProfile mUserProfile = new UserProfile();

    private boolean testFileDBMode = false;
    private Resources mAppRes = null;

    private final int mNewContentsDelayDay = 1;
    public int GetNewContentsDelayDay(){
        return mNewContentsDelayDay;
    }

    // 현재 선택된 MeditationContents
    private MeditationContents cur_contents;
    public MeditationContents getCur_contents() {
        return cur_contents;
    }

    public void setCur_contents(MeditationContents cur_contents) {
        this.cur_contents = cur_contents;
    }

    // 2020.12.05 start
    MeditationCategory   mEmotionMeditationCategory = null;
    MeditationCategory   mEmotionSleepCategory = null;
    MeditationCategory   mEmotionMusicCategory = null;
    // 2020.12.05 end

    // 심리 검사 데이터
    private ArrayList<ResultData> resultData_list;
    public ResultData getResultData(int id) {
        return resultData_list.get(id);
    }

    // Color DataList
    private ArrayList<ColorData> mColorDataList;
    public ArrayList<ColorData> getColorDataList() {return mColorDataList;}

    // Question DataList
    private ArrayList<QuestionData> mQuestionDataList;
    public ArrayList<QuestionData> getQuestionDataList() {return mQuestionDataList;}

    // Category DataList
    private ArrayList<CategoryData> mCategoryDataList;
    public ArrayList<CategoryData> getCategoryDataList() {return mCategoryDataList;}

    // emotion meditation dataList
    private ArrayList<EmotionListData> mEmotionListMeditationDataList;
    public ArrayList<EmotionListData> getEmotionListMeditationDataList() {return mEmotionListMeditationDataList;}

    // emotion sleep DataList
    private ArrayList<EmotionListData> mEmotionListSleepDataList;
    public ArrayList<EmotionListData> getEmotionListSleepDataList() {return mEmotionListSleepDataList;}

    // emotion music DataList
    private ArrayList<EmotionListData> mEmotionListMusicDataList;
    public ArrayList<EmotionListData> getEmotionListMusicDataList() {return mEmotionListMusicDataList;}

    // 초기화 관련 변수 초기화
    public void init(Resources AppRes) {
        mfbDBRef = FirebaseDatabase.getInstance().getReference();

        // 컬러데이터, 컬러테스트질문, 카데고리데이터
        mColorDataList = xmlColorDataParser(R.xml.colordata_result,AppRes);
        mQuestionDataList = xmlQuestionDataParser(R.xml.questiondata_result,AppRes);
        mCategoryDataList = xmlCategoryDataParser(R.xml.categorydata_result,AppRes);

        // 감정리스트_명상,수면,음악 데이터
        mEmotionListMeditationDataList = xmlEmotionListDataParser(R.xml.emotionlist_meditation_result,AppRes);
        mEmotionListSleepDataList = xmlEmotionListDataParser(R.xml.emotionlist_sleep_result,AppRes);
        mEmotionListMusicDataList = xmlEmotionListDataParser(R.xml.emotionlist_music_result,AppRes);

        mAppRes = AppRes;
    }


    public UserProfile getUserProfile() {
        return mUserProfile;
    }

    //=====================================================
    // nickname duplicated 처리
    //=====================================================
    private OnRecvValNickNameListener mOnRecvValNickNameLister = null;
    public interface OnRecvValNickNameListener {
        void onRecvValNickName(boolean validate);
    }
    public void setOnRecvValNickNameListener(OnRecvValNickNameListener listenfunc){
        mOnRecvValNickNameLister = listenfunc;
    }

    // 중복이 안되어 있으면 해당 NickName은 허용 가능하므로 true, 이미 있으면 false를 반환
    public void sendValNickName(String nickName){
        if(this.mOnRecvValNickNameLister != null){
            mfbDBRef.child("nick").child(nickName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        mOnRecvValNickNameLister.onRecvValNickName(false);
                    }
                    else{
                        mOnRecvValNickNameLister.onRecvValNickName(true);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Error처리도 해야 한다.
                }
            });
        }
    }

    //========================================================
    // profile 정보 던져서 서버에 저장
    //========================================================
    private OnRecvValProfileListener mRecvValProfileListener = null;
    public interface OnRecvValProfileListener {
        void onRecvValProfile(boolean validate);
    }
    public void setOnRecvValProfileListener(OnRecvValProfileListener listenfunc){
        mRecvValProfileListener = listenfunc;
    }
    public void sendValProfile(UserProfile profile){
        if(this.mRecvValProfileListener != null){
            mfbDBRef.child("users").child(profile.uid).setValue(profile).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mfbDBRef.child("nick").child(profile.nickname).setValue(profile.uid);
                    mRecvValProfileListener.onRecvValProfile(true);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mRecvValProfileListener.onRecvValProfile(false);
                }
            });
        }
    }

    //==============================================================================
    // 유저 프로파일 정보 받기
    //      true : 유저정보 정상적으로 받고, mUserProfile에 저장,
    //      false, 0: 유저 정보가 없음, false, 1000 : error나옴.
    //==============================================================================
    private OnRecvProfileListener mRecvProfileListener = null;
    public interface OnRecvProfileListener {
        void onRecvProfile(boolean validate,int errorcode);
    }
    public void setOnRecvProfileListener(OnRecvProfileListener listenfunc){
        mRecvProfileListener = listenfunc;
    }
    public void recvUserProfile(String uid){
        mfbDBRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    getinstance().mUserProfile = (UserProfile)snapshot.getValue(UserProfile.class);
                    mRecvProfileListener.onRecvProfile(true,0);
                }
                else{
                    // 없는 경우 데이터가 올라가지 않음, false이지만 errcode 0
                    mRecvProfileListener.onRecvProfile(false,0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error처리도 해야 한다.
                //mRecvContentsListener.onRecvContents(false);
                mRecvProfileListener.onRecvProfile(false,1000);
            }
        });
    }

    //==============================================================================
    // 해당 Movie정보를 서버에게 받아야 한다. (로그인할때에만 받아야 한다. 하루 기준)
    // 해당 정보를 다 받을떄까지 콜백처리 해야 함.
    //==============================================================================
    private OnRecvContentsListener mRecvContentsListener = null;
    public interface OnRecvContentsListener {
        void onRecvContents(boolean validate);
    }
    public void setOnRecvContentsListener(OnRecvContentsListener listenfunc){
        mRecvContentsListener = listenfunc;
    }

    public void recvContentsExt(){
        mfbDBRef.child("meditation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot meditationSnapshot: snapshot.getChildren()) {
                        MeditationContents contentsdata = meditationSnapshot.getValue(MeditationContents.class);

                        // audio
                        contentsdata.audio =   NetServiceUtility.audiofiledir + contentsdata.audio + NetServiceUtility.audioextenstion;

                        // thumnail
                        contentsdata.thumbnail = NetServiceUtility.thumnaildir + contentsdata.thumbnail  + NetServiceUtility.imgextenstion;

                        // bg image
                        contentsdata.bgimg =   NetServiceUtility.bgimgdir + contentsdata.bgimg + NetServiceUtility.imgextenstion;

                        // showtype결정
                        if(contentsdata.artist.equals("0")){
                            if(!contentsdata.author.equals("0")){
                                contentsdata.showtype = 1;
                            }else{
                                contentsdata.showtype = 0;
                            }
                        }else{
                            contentsdata.showtype = 2;
                        }

                        mContentsList.add(contentsdata);
                    }
                    mRecvContentsListener.onRecvContents(true);
                }
                else{
                    mRecvContentsListener.onRecvContents(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mRecvContentsListener.onRecvContents(false);
            }
        });
    }

    //=========================================================================================
    // 감정 테스트 여부를 파악해서 홈에 관련된 카데고리 정보를 반환한다.
    // 감정 테스트를 했으면 UserProfile 구조체의 emotionid 정보를 통해 선택한 감정을 확인한다.
    //=========================================================================================
    public MeditationShowCategorys returnHomeShowCategorys(boolean mIsDoneTest){
        MeditationShowCategorys newShowCategorys = new MeditationShowCategorys();
        newShowCategorys.showcategorys = new ArrayList<MeditationCategory>();

         //  해당 감정의 id를 이용해서 매칭 태그 string list를 얻고 이것을 통해서 콘텐츠의
         //  healingtag에 같은 것이 하나라도 있으면 선택이 되는 것임.
         if(mIsDoneTest){
             for(int i = 0; i < 3; i++){
                 String genre = "";
                 if(i == 0) {
                     genre = genre1;
                 }else if(i == 1){
                     genre = genre2;
                 }else{
                     genre = genre3;
                 }

                 MeditationCategory emotionCategory =  getLocalEmotionTestCategory(genre);  // 2020.12.05
                 if(emotionCategory != null){
                     newShowCategorys.showcategorys.add(emotionCategory);
                 }
             }
         }

         //  즐겨찾기한 콘텐츠
         MeditationCategory favoriteCategory = getFavoriteCategory(0);
         if(favoriteCategory != null){
             newShowCategorys.showcategorys.add(favoriteCategory);
         }

         // 3. 인기리스트(12월 버전에서는 제외)

         // 4. 고정리스트 콘텐츠들
         List<String> inputContentsCategorysIds = asList("51","52","53","54","63","67","55","56","57","58","59","64","65");
         procContentsCategorys(inputContentsCategorysIds,newShowCategorys);

         return newShowCategorys;
    }

    // 명상 카테고리를 얻는다.
    public MeditationShowCategorys returnMeditationShowCategorys(boolean mIsDoneTest) {
        MeditationShowCategorys newShowCategorys = new MeditationShowCategorys();
        newShowCategorys.showcategorys = new ArrayList<MeditationCategory>();

        if(mIsDoneTest){
            MeditationCategory emotionMeditationCategory =   getLocalEmotionTestCategory(genre1);  // 2020.12.05
            if(emotionMeditationCategory != null){
                newShowCategorys.showcategorys.add(emotionMeditationCategory);
            }
        }

        //    즐겨찾기한 콘텐츠
        MeditationCategory favoriteCategory = getFavoriteCategory(1);
        if(favoriteCategory != null){
            newShowCategorys.showcategorys.add(favoriteCategory);
        }

        List<String> inputContentsCategorysIds = asList("51","52","55","56","57","58");
        procContentsCategorys(inputContentsCategorysIds,newShowCategorys);

        return newShowCategorys;
    }


    // 수면  카테고리를 얻는다.
    public MeditationShowCategorys returnSleepShowCategorys(boolean mIsDoneTest) {
        MeditationShowCategorys newShowCategorys = new MeditationShowCategorys();
        newShowCategorys.showcategorys = new ArrayList<MeditationCategory>();

        if(mIsDoneTest){
            MeditationCategory emotionSleepCategory =   getLocalEmotionTestCategory(genre2);  // 2020.12.05;
            if(emotionSleepCategory != null){
                newShowCategorys.showcategorys.add(emotionSleepCategory);
            }
        }

        // 즐겨찾기한 콘텐츠
        // userprofile의 favorites.contents가 하나라도 있으면 만든다.
        MeditationCategory favoriteCategory = getFavoriteCategory(2);
        if(favoriteCategory != null){
            newShowCategorys.showcategorys.add(favoriteCategory);
        }

        List<String> inputContentsCategorysIds = asList("54","63","67","64");
        procContentsCategorys(inputContentsCategorysIds,newShowCategorys);
        return newShowCategorys;
    }

    // 음악  카테고리를 얻는다.
    public MeditationShowCategorys returnMusicShowCategorys(boolean mIsDoneTest) {
        MeditationShowCategorys newShowCategorys = new MeditationShowCategorys();
        newShowCategorys.showcategorys = new ArrayList<MeditationCategory>();

        if(mIsDoneTest){
            MeditationCategory emotionMusicCategory =   getLocalEmotionTestCategory(genre3);  // 2020.12.05;
            if(emotionMusicCategory != null){
                newShowCategorys.showcategorys.add(emotionMusicCategory);
            }
        }

        //    즐겨찾기한 콘텐츠
        //    userprofile의 favorites.contents가 하나라도 있으면 만든다.
        MeditationCategory favoriteCategory = getFavoriteCategory(3);
        if(favoriteCategory != null){
            newShowCategorys.showcategorys.add(favoriteCategory);
        }

        List<String> inputContentsCategorysIds = asList("59","59","65");
        procContentsCategorys(inputContentsCategorysIds,newShowCategorys);
        return newShowCategorys;
    }

    // 감정테스트후에 감정에 따른 감정테스트 결과에 대한 카데고리를 알려준다.
    private MeditationCategory getEmotionTestCategory(String genre)
    {
        // 명상
        String curemotionid = Integer.toString(mUserProfile.emotiontype);

        int dataNum = 0; // mEmotionListMeditationDataList.size();
        Map<String,MeditationContents> contentsList = new HashMap<>();

        // 카테고리 정보를 얻어야 한다.
        CategoryData curCategoryData = null;
        ArrayList<EmotionListData> curEmotionListDataList = null;

        // 주어진 장르와 같은 DataList를 얻는다.
        if(genre.equals(genre1)) {
            curCategoryData = getCategoryDataExt(curemotionid, genre1);
            curEmotionListDataList = mEmotionListMeditationDataList;
        }else if(genre.equals(genre2)){
            curCategoryData = getCategoryDataExt(curemotionid, genre2);
            curEmotionListDataList = mEmotionListSleepDataList;
        }else if(genre.equals(genre3)){
            curCategoryData = getCategoryDataExt(curemotionid, genre3);
            curEmotionListDataList = mEmotionListMusicDataList;
        }else{
            return null;
        }

        dataNum = curEmotionListDataList.size();
        MeditationCategory mCurCategory = null;

        // 1. 감정 태그를 먼저 찾는다.
        List<String> healingtaglist = null;
        for(int i = 0; i < dataNum; i++) {
            EmotionListData mData = curEmotionListDataList.get(i);
            if (curemotionid.equals(mData.emotionid)) {
                healingtaglist = asList(mData.healingtag.split(","));
                break;
            }
        }

        // 2. 찾은 감정태그를 이용해서 콘텐츠의 태그들과 비교한다.
        if(healingtaglist == null)
            return null;

        int contentsNum = mContentsList.size();
        int subDataNum = healingtaglist.size();

        for(int k = 0; k < contentsNum; k++){
            MeditationContents curContents = mContentsList.get(k);
            List<String> contentshealingtaglist = asList(curContents.healingtag.split(","));
            int healingtagNum = contentshealingtaglist.size();

            boolean bFindContents = false;

            // healingtag 하나라도 같으면 넣으면 된다.
            for(int j = 0; j < subDataNum; j++) {
                String tagString = healingtaglist.get(j);

                for (int idx = 0; idx < healingtagNum; idx++) {
                    String curHealingTag = contentshealingtaglist.get(idx);
                    if (tagString.equals(curHealingTag) && curContents.genre.equals(genre)) {   // genre비교 안되어 있었음.
                        // 해당 HealingTag가 Map에 있는 지 조사
                        if (!contentsList.containsKey(curContents.uid)) {
                            contentsList.put(curContents.uid, curContents);
                            bFindContents = true;
                            break;
                        }
                    }
                }

                if(bFindContents == true)
                    break;
            }
        }

        // 완료된후에 Map에 들어가 있는 원소들을 모두 돌면서 contentsid를 얻고
        if(contentsList.size()> 0){
            mCurCategory = new MeditationCategory();
            mCurCategory.name = curCategoryData.name;
            mCurCategory.contests = new ArrayList<MediationShowContents>();
        }

        //======================================================================
        // Map을 랜덤하게 일정수를 넣도록 하는것이 좋다. 우선은 Max치만 넣게 한다.
        //======================================================================
        int curNum = 0;
        for (Map.Entry<String, MeditationContents> entry : contentsList.entrySet()) {
            if(curNum < curCategoryData.maxnum) {
                String curkey = entry.getKey();  // contents id
                MediationShowContents newEntity = new MediationShowContents();
                newEntity.entity = entry.getValue();
                mCurCategory.contests.add(newEntity);
                curNum++;
            }
        }

        // 2020.12.05
        if(mCurCategory != null &&  mCurCategory.contests.size() > 0){
            Collections.shuffle(mCurCategory.contests);
        }

        return mCurCategory;
    }




    // selectType 0 : all 1: 명상, 2: 수면 3 : 음악
    // 종류별로 즐겨찾기한 콘텐츠들을 알려준다.
    private MeditationCategory getFavoriteCategory(int seletType)
    {
        int favoritesContentsNum = mUserProfile.favoriteslist.size();
        if(favoritesContentsNum > 0){
            CategoryData entityData = getCategoryData(favoriteCategoryId);

            if(entityData != null){
                MeditationCategory favoriteCategory = new MeditationCategory();
                favoriteCategory.name = entityData.name;
                favoriteCategory.contests = new ArrayList<MediationShowContents>();

                // 유저프로파일에 있는 favorites 콘텐츠를 통해서 카데고리 구성
                boolean checkedEntity = false;
                int contentsNum = 0;

                for (Map.Entry<String, Boolean> entry : mUserProfile.favoriteslist.entrySet()) {
                    String curkey = entry.getKey();  // contents id
                    checkedEntity = false;
                    MeditationContents entity = getMeditationContents(curkey);

                    if(seletType == 1){
                        if(entity.genre.equals(genre1))  checkedEntity = true;
                    }else if(seletType == 2){
                        if(entity.genre.equals(genre2))  checkedEntity = true;
                    }else if(seletType == 3){
                        if(entity.genre.equals(genre3))  checkedEntity = true;
                    }else{
                        checkedEntity = true;
                    }

                    if(entity != null && checkedEntity == true){
                        if(contentsNum < maxFavoriteContentsNum){
                            MediationShowContents newEntity = new MediationShowContents();
                            newEntity.entity = entity;
                            favoriteCategory.contests.add(newEntity);
                            contentsNum++;
                        }
                    }
                }

                if(favoriteCategory.contests.size() > 0)
                    return favoriteCategory;
            }
        }
        return null;
    }

    // 주어진 Categoryid 집합들에 맞는 콘텐츠들을 모아서 한꺼번에 처리한다.
    private void procContentsCategorys(List<String> categoryids,MeditationShowCategorys newShowCategorys)
    {
        int dataNum = categoryids.size();
        for(int i = 0; i < dataNum; i++){
            MeditationCategory contentsCategory = getContentsCategory(categoryids.get(i));
            if(contentsCategory != null){
                newShowCategorys.showcategorys.add(contentsCategory);
            }
        }
    }

    // 주어진 단일 Categoryid에 맞는 콘텐츠들을 모은다.
    private MeditationCategory getContentsCategory(String categoryid)
    {
        int dataNum = mContentsList.size();
        int curNum = 0;
        CategoryData entityData = getCategoryData(categoryid);

        if(entityData != null && dataNum > 0) {
            MeditationCategory contentsCategory = new MeditationCategory();
            contentsCategory.name = entityData.name;
            contentsCategory.contests = new ArrayList<MediationShowContents>();

            for(int i = 0; i < dataNum; i++){
                MeditationContents contents = mContentsList.get(i);
                if(contents.categoryid.equals(entityData.id)){
                    if(curNum < entityData.maxnum){
                        MediationShowContents newEntity = new MediationShowContents();
                        newEntity.entity = contents;
                        contentsCategory.contests.add(newEntity);
                        curNum++;
                    }
                }
            }

            if(contentsCategory.contests.size() == 0){
                contentsCategory.contests = null;
                contentsCategory = null;
                return null;
            }

            return contentsCategory;
        }
        return null;
    }

    // 2020.12.05 start
    // 새로운 정보를 얻을때에 사용한다.
    public void reqEmotionAllContents(){
        mEmotionMeditationCategory = getEmotionTestCategory(genre1);
        mEmotionSleepCategory = getEmotionTestCategory(genre2);
        mEmotionMusicCategory = getEmotionTestCategory(genre3);
    }

    private MeditationCategory getLocalEmotionTestCategory(String genre)
    {
        if(genre.equals(genre1)){
            return mEmotionMeditationCategory;
        }else if(genre.equals(genre2)){
            return mEmotionSleepCategory;
        }else if(genre.equals(genre3)){
            return mEmotionMusicCategory;
        }

        return null;
    }
    // 2020.12.05 end

    // 주어진 타입에 따라서 관련 MeditationShowCategorys을 알려준다.
    // 1: home  2: 명상  3: 수면, 4: 음악 5: 테스트용
    public MeditationShowCategorys reqMediationType(int type, boolean mIsDoneTest){
        switch (type){
            case 1 :  // home
                return returnHomeShowCategorys(mIsDoneTest);
            case 2:   // 명상
                return returnMeditationShowCategorys(mIsDoneTest);
            case 3:   // 수면
                return returnSleepShowCategorys(mIsDoneTest);
            case 4:   // 음악
                return returnMusicShowCategorys(mIsDoneTest);
            case 5:   // Test 용
                // home에 대한 정보를 만들어서 처리한다.
                MeditationShowCategorys newShowCategorys = new MeditationShowCategorys();
                newShowCategorys.showcategorys = new ArrayList<MeditationCategory>();

                MeditationCategory newCategory = new MeditationCategory();
                newCategory.name = "오늘을 찾는 명상";
                newCategory.contests = new ArrayList<MediationShowContents>();

                int sizeNum = NetServiceManager.getinstance().mContentsList.size();
                for(int i = 0; i <  sizeNum ; i++){
                    if(NetServiceManager.getinstance().mContentsList.get(i).genre.equals("명상")){
                        MediationShowContents entity = new MediationShowContents();
                        entity.entity = NetServiceManager.getinstance().mContentsList.get(i);
                        newCategory.contests.add(entity);
                    }
                }

                newShowCategorys.showcategorys.add(newCategory);

                newCategory = new MeditationCategory();
                newCategory.name = "오늘을 찾는 수면";
                newCategory.contests = new ArrayList<MediationShowContents>();

                sizeNum = NetServiceManager.getinstance().mContentsList.size();
                for(int i = 0; i <  sizeNum ; i++){
                    if(NetServiceManager.getinstance().mContentsList.get(i).genre.equals("수면")){
                        MediationShowContents entity = new MediationShowContents();
                        entity.entity = NetServiceManager.getinstance().mContentsList.get(i);
                        newCategory.contests.add(entity);
                    }
                }

                newShowCategorys.showcategorys.add(newCategory);

                newCategory = new MeditationCategory();
                newCategory.name = "오늘을 찾는 음악";
                newCategory.contests = new ArrayList<MediationShowContents>();

                sizeNum = NetServiceManager.getinstance().mContentsList.size();
                for(int i = 0; i <  sizeNum ; i++){
                    if(NetServiceManager.getinstance().mContentsList.get(i).genre.equals("음악")){
                        MediationShowContents entity = new MediationShowContents();
                        entity.entity = NetServiceManager.getinstance().mContentsList.get(i);
                        newCategory.contests.add(entity);
                    }
                }

                newShowCategorys.showcategorys.add(newCategory);
                return newShowCategorys;
            default:
                break;
        }
        return null;
    }

    //==============================================================================================
    //  해당 콘텐츠의 좋아요, 싫어요 결정. reactiionCode 0: Default 1 : 좋아요, 2: 싫어요
    // ex) NetServiceManager.getinstance().sendFavoriteEvent("dkefddfeassss","10001",1);
    //==============================================================================================
    public void sendFavoriteEvent(String uid, String contentid, int reactionCode){
        FirebaseDatabase.getInstance().getReference("meditation").child(contentid)
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        MeditationContents infoData =  currentData.getValue(MeditationContents.class);

                        if (infoData == null) {
                            return Transaction.success(currentData);
                        }

                        /// sync local contents list
                        int itemsize= mContentsList.size();
                        MeditationContents findContents = null;
                        for(int i = 0; i < itemsize;i++) {
                            MeditationContents contents = mContentsList.get(i);
                            if(contents.uid.equals(contentid)){
                                findContents =  contents;
                            }
                        }
                        if(findContents != null){
                            Integer curstate = findContents.states.get(uid);
                            if(findContents.states.containsKey(uid)){
                                // 기존의 값을 입력값을 기준으로 해서 상태 업데이트
                                int prevReaction = curstate.intValue();

                                // remove는 될수가 없다. Add만 있다. 따라서 비교해서 수정만 하면 된다.
                                // 우선 이전것을 - 한다. 그리고  현재 것을 +한다.
                                if(prevReaction == 1){
                                    findContents.favoritecnt -= 1;
                                }else if(prevReaction == 2){
                                    findContents.hatecnt -= 1;
                                }

                                if(reactionCode == 1){
                                    findContents.favoritecnt += 1;
                                }else if(reactionCode == 2){
                                    findContents.hatecnt += 1;
                                }

                                if(findContents.favoritecnt < 0 ) findContents.favoritecnt = 0;
                                if(findContents.hatecnt < 0 ) findContents.hatecnt = 0;


                            }else {
                                if(reactionCode == 1){
                                    findContents.favoritecnt += 1;
                                    findContents.states.put(uid,reactionCode);
                                }else if(reactionCode == 2){
                                    findContents.hatecnt += 1;
                                    findContents.states.put(uid,reactionCode);
                                }else{
                                    // error
                                    return null;
                                }
                            }
                        }


                        Integer curstate = infoData.states.get(uid);

                        if(infoData.states.containsKey(uid)){
                            // 기존의 값을 입력값을 기준으로 해서 상태 업데이트
                            int prevReaction = curstate.intValue();

                            // remove는 될수가 없다. Add만 있다. 따라서 비교해서 수정만 하면 된다.
                            // 우선 이전것을 - 한다. 그리고  현재 것을 +한다.
                            if(prevReaction == 1){
                                infoData.favoritecnt -= 1;
                            }else if(prevReaction == 2){
                                infoData.hatecnt -= 1;
                            }

                            if(reactionCode == 1){
                                infoData.favoritecnt += 1;
                            }else if(reactionCode == 2){
                                infoData.hatecnt += 1;
                            }

                            if(infoData.favoritecnt < 0 ) infoData.favoritecnt = 0;
                            if(infoData.hatecnt < 0 ) infoData.hatecnt = 0;


                        }else {
                            if(reactionCode == 1){
                                infoData.favoritecnt += 1;
                                infoData.states.put(uid,reactionCode);
                            }else if(reactionCode == 2){
                                infoData.hatecnt += 1;
                                infoData.states.put(uid,reactionCode);
                            }else{
                                // error
                                return null;
                            }
                        }

                        if(infoData.favoritecnt < 0 ) infoData.favoritecnt = 0;
                        if(infoData.hatecnt < 0 ) infoData.hatecnt = 0;

                        currentData.setValue(infoData);
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    }
                });
    }

    // 해당 콘텐츠의 해당 유저의 favorite여부 0: Default 1 : 좋아요, 2: 싫어요
    public int reqContentsFavoriteEvent(String uid, String contentid)
    {
        int itemsize= mContentsList.size();

        for(int i = 0; i < itemsize;i++) {
            MeditationContents contents = mContentsList.get(i);
            if(contents.uid.equals(contentid)){
                Integer value =  contents.states.get(uid);
                if(value == null){
                    return 0;
                }
                return value.intValue();
            }
        }

        return 0;
    }

    // 즐겨찾기 여부 : false - 즐겨찾기 안되어 있음. true - 즐겨찾기 되어있음
    public Boolean reqFavoriteContents(String contentid)
    {
        if(mUserProfile.favoriteslist.containsKey(contentid))
            return true;

        return false;
    }

    //  즐겨찾기 설정 함수 isfavorite : true(즐겨찾기 ON),false(즐겨찾기 off)
    public void sendFavoriteContents(String contentid,Boolean isfavorite){
        boolean isHaveContents = false;

        if(mUserProfile.favoriteslist.containsKey(contentid)){
            isHaveContents = true;
        }

        if(isfavorite == true){
            // 기존에 값이 있는지 확인 필요
            if(!isHaveContents){
                // 값을 넣어 주어야 함.
                mUserProfile.favoriteslist.put(contentid,true);
                // 서버에 값을 넣어주어야 한다.
                mfbDBRef.child("users").child(mUserProfile.uid).setValue(mUserProfile);
            }
        }else{
            // 있는 놈만 지운다.
            if(isHaveContents) {
                mUserProfile.favoriteslist.remove(contentid);
            }
            mfbDBRef.child("users").child(mUserProfile.uid).setValue(mUserProfile);
        }
    }

    // 성격검사 , selectid 1~16 : 감정
    public void checkFeelTest(int selectid)
    {
        mUserProfile.emotiontype = selectid;
        mfbDBRef.child("users").child(mUserProfile.uid).setValue(mUserProfile);
    }

    // 컬러 검사. 선택한 colorList 1~4선택된 6개로 온다.
    // 해당 내용을 바탕으로 해서 감정 평가 해서 mUserProfile.emotiontype 에 넣어준다.

    public Map<String,Integer> colorTestResult = new HashMap<>();

    // emotionid 값을 string으로 준다. 만약에 0이 오면 문제가 있는 것임.
    public String checkColorTest(List<Integer> answerList)
    {
        colorTestResult.clear();

        int questionNum = answerList.size();

        for(int i = 0; i < questionNum; i++){
            int curValue = answerList.get(i).intValue();

            QuestionData curQuestion = mQuestionDataList.get(i);

            int curSelColorId = getCurAnswerId(curQuestion,curValue);

            ColorData curData = getColorData(curSelColorId);

            // 선택한 colorData의 감정과 score를 저장
            if(curData != null){
                // 이미 해당 감정 데이터가 있는 경우에는 기존값을 얻어와서 더해 준다.
                int curScore = curData.score;

                if(colorTestResult.containsKey(curData.emotionid)){
                    int preScore = colorTestResult.get(curData.emotionid).intValue();
                    curScore += preScore;
                }
                colorTestResult.put(curData.emotionid,curScore);
            }else{
                Log.d("checkColorTest","error curData!");
                continue;
            }
        }

        // Value가 가장 높은 Key값을 알아내면 그것이 해당 감정 id이다.
        Map<String,Integer> result = sortMapByValue(colorTestResult);
        String findemotionid =  "0";
        for (Map.Entry<String, Integer> entry : result.entrySet()) {
            Log.d("checkColorTest","Key : "+ entry.getKey() + " Value : "+entry.getValue());
            String curkey = entry.getKey();
            int curValue = entry.getValue().intValue();
            findemotionid = entry.getKey();
        }

        mUserProfile.emotiontype = Integer.parseInt(findemotionid);
        // 최종 ID
        return findemotionid;
    }

    // 맵에 대한 오른차순 정렬
    public static LinkedHashMap<String, Integer> sortMapByValue(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> entries = new LinkedList<>(map.entrySet());
        Collections.sort(entries, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    // selidx 1~4
    public int getCurAnswerId(QuestionData curQuestion, int curValue)
    {
        int curSelColorId  = 0;

        if(curValue == 1){
            curSelColorId = curQuestion.answer1_id;
        }else if(curValue == 2){
            curSelColorId = curQuestion.answer2_id;
        }else if(curValue == 3){
            curSelColorId = curQuestion.answer3_id;
        }else if(curValue == 4){
            curSelColorId = curQuestion.answer4_id;
        }else{
            Log.d("checkColorTest","error checkColorTest!");
        }

        return curSelColorId;
    }

    public ColorData getColorData(int curSelColorId){
        // 해당 curSelColorId을 통해서 해당 Color를 찾아서 그 Color의 emotionid와 score를 저장해야한다.
        int colorDataNum = mColorDataList.size();
        for(int j = 0 ; j < colorDataNum; j++){
            ColorData entity = mColorDataList.get(j);
            if( entity.id == curSelColorId){
                return entity;
            }
        }

        return null;
    }

    // function 8. 카데고리 Data알려줌.
    public CategoryData getCategoryData(String curSelCategoryId){
        // 해당 curSelColorId을 통해서 해당 Color를 찾아서 그 Color의 emotionid와 score를 저장해야한다.
        int DataNum = mCategoryDataList.size();
        for(int j = 0 ; j < DataNum; j++){
            CategoryData entity = mCategoryDataList.get(j);
            if( entity.id.equals(curSelCategoryId)){
                return entity;
            }
        }
        return null;
    }

    // CategoryData를 emotionid와 genre를 통해서 Data를 알려줌.
    public CategoryData getCategoryDataExt(String emotionid, String genre){
        // 해당 curSelColorId을 통해서 해당 Color를 찾아서 그 Color의 emotionid와 score를 저장해야한다.
        int DataNum = mCategoryDataList.size();
        for(int j = 0 ; j < DataNum; j++){
            CategoryData entity = mCategoryDataList.get(j);
            if( entity.emotionid.equals(emotionid) && entity.genre.equals(genre)){
                return entity;
            }
        }
        return null;
    }

    // func contents를 얻기 위해서 사용. 그런데 문제는 부모일경우의 처리 후에 필요.
    public MeditationContents getMeditationContents(String contentid){
        int dataNum = mContentsList.size();
        for(int i = 0; i < dataNum; i++){
            if(mContentsList.get(i).uid.equals(contentid))
                return mContentsList.get(i);
        }
        return null;
    }

    private XmlResourceParser parser;

    // emotionlist data 3개를 parsing하는 함수
    public ArrayList<EmotionListData> xmlEmotionListDataParser(int res_id,Resources res) {
        ArrayList<EmotionListData> arrayList = new ArrayList<EmotionListData>();
        parser = res.getXml(res_id);
        try {
            int eventType = parser.getEventType();
            EmotionListData data = null;

            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String startTag = parser.getName();
                        if(startTag.equals("emotiondata")) {
                            data = new EmotionListData();
                        }
                        if(startTag.equals("id")) {
                            data.id = parser.nextText();
                        }
                        if(startTag.equals("voiceemotion")) {
                            data.voiceemotion = parser.nextText();
                        }
                        if(startTag.equals("softemotion")) {
                            data.softemotion = parser.nextText();
                        }
                        if(startTag.equals("emotionid")) {
                            data.emotionid = parser.nextText();
                        }
                        if(startTag.equals("genre")) {
                            data.genre = parser.nextText();
                        }
                        if(startTag.equals("healingtag")) {
                            data.healingtag = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        String endTag = parser.getName();
                        if(endTag.equals("emotiondata")) {
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

    public ArrayList<CategoryData> xmlCategoryDataParser(int res_id,Resources res){
        ArrayList<CategoryData> arrayList = new ArrayList<CategoryData>();
        parser = res.getXml(res_id);
        try {
            int eventType = parser.getEventType();
            CategoryData data = null;

            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String startTag = parser.getName();
                        if(startTag.equals("categorydata")) {
                            data = new CategoryData();
                        }
                        if(startTag.equals("id")) {
                            data.id = parser.nextText();
                        }
                        if(startTag.equals("name")) {
                            data.name = parser.nextText();
                        }
                        if(startTag.equals("priority")) {
                            data.priority = parser.nextText();
                        }
                        if(startTag.equals("maxnum")) {
                            data.maxnum = Integer.parseInt(parser.nextText());
                        }
                        if(startTag.equals("emotionid")) {
                            data.emotionid = parser.nextText();
                        }
                        if(startTag.equals("genre")) {
                            data.genre = parser.nextText();
                        }
                        if(startTag.equals("fix")) {
                            data.fix = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        String endTag = parser.getName();
                        if(endTag.equals("categorydata")) {
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

    public ArrayList<ColorData> xmlColorDataParser(int res_id,Resources res){
        ArrayList<ColorData> arrayList = new ArrayList<ColorData>();
        parser = res.getXml(res_id);
        try {
            int eventType = parser.getEventType();
            ColorData data = null;

            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String startTag = parser.getName();
                        if(startTag.equals("colordata")) {
                            data = new ColorData();
                        }
                        if(startTag.equals("id")) {
                            data.id = Integer.parseInt(parser.nextText());
                        }
                        if(startTag.equals("colorsphere")) {
                            data.colorsphere = parser.nextText();
                        }
                        if(startTag.equals("emotion")) {
                            data.emotion = parser.nextText();
                        }
                        if(startTag.equals("emotionid")) {
                            data.emotionid = parser.nextText();
                        }
                        if(startTag.equals("score")) {
                            data.score = Integer.parseInt(parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        String endTag = parser.getName();
                        if(endTag.equals("colordata")) {
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


    //private ArrayList<QuestionData> mQuestionDataList;
    private ArrayList<QuestionData> xmlQuestionDataParser(int res_id,Resources res)  {
        ArrayList<QuestionData> arrayList = new ArrayList<QuestionData>();
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

    public ArrayList<ResultData> xmlParser(int res_id, Resources res)  {

        ArrayList<ResultData> arrayList = new ArrayList<ResultData>();
        parser = res.getXml(res_id);

        try {
            int eventType = parser.getEventType();
            ResultData data = null;

            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String startTag = parser.getName();
                        if(startTag.equals("resultdata")) {
                            data = new ResultData();
                        }
                        if(startTag.equals("id")) {
                            data.id = Integer.parseInt(parser.nextText());
                        }
                        if(startTag.equals("emotion")) {
                            data.emotion = parser.nextText();
                        }
                        if(startTag.equals("emotionimg")) {
                            data.emotionimg = parser.nextText();
                        }
                        if(startTag.equals("state")) {
                            data.state = parser.nextText();
                        }
                        if(startTag.equals("desc")) {
                            data.desc = parser.nextText();
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        String endTag = parser.getName();
                        if(endTag.equals("resultdata")) {
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

        resultData_list = arrayList;

        return arrayList;
    }


    // 유저프로필 : 세션 카운트 1증가, 플레이 시간 업데이트
    public void Update_UserProfile_Play(int playtime)
    {
        if(mUserProfile == null) return;

        mUserProfile.playtime += playtime;
        mUserProfile.sessionnum += 1;

//        sendValProfile(mUserProfile);
//        NetServiceManager.getinstance().setOnRecvValProfileListener(new NetServiceManager.OnRecvValProfileListener() {
//            @Override
//            public void onRecvValProfile(boolean validate)
//            {
//                if(validate == true){
//
//                    int xx = 0;
//                }else{
//
//                    int yy = 0;
//
//                }
//            }
//        });
    }

    // local contents list update
    public void sendFavoriteLocalEvent(String uid, String contentid, int reactionCode) {
        MeditationContents localContents = this.getMeditationContents(contentid);
        if(localContents != null){
            if(localContents.states.containsKey(uid)){
                // 기존값이 있느데 같을 경우
                // 기존값이 있는데 다를 경우
                Integer prev = localContents.states.get(uid);
                if(prev.intValue() != reactionCode){
                    if(reactionCode == 1){
                        // 기존코드가 reactionCode 2
                        localContents.favoritecnt++;
                        localContents.hatecnt--;
                        localContents.states.put(uid,reactionCode);

                    }else if(reactionCode == 2){
                        // 기존코드가 reactionCode 1
                        localContents.favoritecnt--;
                        localContents.hatecnt++;
                        localContents.states.put(uid,reactionCode);
                    }
                }
            }else{
                // 기존값이 없을 경우
                if(reactionCode == 1){
                    localContents.favoritecnt++;
                    localContents.states.put(uid,reactionCode);
                }else if(reactionCode == 2){
                    // 기존코드가 reactionCode 1
                    localContents.hatecnt++;
                    localContents.states.put(uid,reactionCode);
                }

            }
        }
    }
}



