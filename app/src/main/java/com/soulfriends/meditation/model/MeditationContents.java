package com.soulfriends.meditation.model;

import java.util.HashMap;
import java.util.Map;

public class MeditationContents {
    public String uid;           // 고유 id
    public String title;         // 콘텐츠 제목
    public String genre;         // 콘텐츠 장르 : 명상, 수면 등등
    public String categoryid;    // 해당 콘텐츠의 Category id
    public String audio;         // audio 파일
    public String thumbnail;      // thumnail 이미지
    public String playtime;         // 콘텐츠 재생 시간(s)
    public String releasedate;      // 콘텐츠 릴리즈 날짜. ex)2020-11-26
    public String publisher;        // 제작사
    public String publishericon;        // 제작사
    public String publisherintro;   // 제작사 소개
    public String appname;          // 제공되는 어플 이름
    public String emotion;          // 감정 내용
    public String narrtor;       // 나레이터
    public String author;        // 저자
    public String intro;         // 콘텐츠 소개
    public String parentuid;     // 부모 콘텐츠 여부 , 0 : 부모 없음. 0보다 큰 uid는 해당 부모 콘텐츠 uid
    public String bgimg;         // 배경 이미지.
    public String healingtag;    // 힐링 태그
    public String childlist;     // 자식 콘텐츠 uid들

    public String artist;        // 아티스트  -> 아티스트 추가
    public int showtype = 0;     // 플레이 화면에서 "나레이터 저자"순으로 보이면 1, "아티스트"만 보이면 2, 아무것도 안보이면 ,0
    //=============================================================
    public int favoritecnt = 0;
    public int hatecnt = 0;      // 2020.11.25 처리  싫어요 cnt
    public Map<String, Integer> states = new HashMap<>(); // 2020.11.25 처리

    public String thumbnail_uri;      // thumnail http 용 url
}
