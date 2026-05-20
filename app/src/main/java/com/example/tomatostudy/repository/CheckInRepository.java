package com.example.tomatostudy.repository;

import android.content.Context;

import com.example.tomatostudy.database.dao.CheckInDao;
import com.example.tomatostudy.database.model.CheckIn;

import java.util.List;

public class CheckInRepository {

    private final CheckInDao checkInDao;

    public CheckInRepository(Context context) {
        checkInDao = new CheckInDao(context);
    }

    public boolean checkIn(int userId, String checkDate, String content) {
        // 打卡前先判断当天是否已经打卡，避免同一用户同一天重复保存记录。
        if (hasCheckedInToday(userId, checkDate)) {
            return false;
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckDate(checkDate);
        checkIn.setContent(content);
        checkIn.setCreatedTime(System.currentTimeMillis());
        return checkInDao.insertCheckIn(checkIn) > 0;
    }

    public boolean hasCheckedInToday(int userId, String today) {
        // 查询 check_in 表中是否存在当前用户今天的记录，用于控制打卡按钮状态。
        return checkInDao.hasCheckIn(userId, today);
    }

    public int loadTotalCheckInDays(int userId) {
        return checkInDao.countByUserId(userId);
    }

    public List<CheckIn> loadCheckIns(int userId) {
        return checkInDao.getCheckInsByUserId(userId);
    }
}
