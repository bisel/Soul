package com.soulfriends.meditation.model;

import java.util.Iterator;
import java.util.List;

public class MeditationCategory {
    public String name;  // 추천리스트 이름 (감정에 맞는리스트)
    public List<MediationShowContents> contests;

    public int type; // 0 이면 상단, 1 이면 리스트

    // 모든 showcontents지움
    public void allClear()
    {
        // delete showcontents
        for (Iterator<MediationShowContents> iter = contests.iterator(); iter.hasNext(); ) {
            MediationShowContents letter = iter.next();
            iter.remove();
        }
    }
}