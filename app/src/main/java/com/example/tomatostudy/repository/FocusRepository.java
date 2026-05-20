package com.example.tomatostudy.repository;

import android.content.Context;

import com.example.tomatostudy.database.dao.FocusDao;
import com.example.tomatostudy.database.model.FocusRecord;

import java.util.List;

public class FocusRepository {

    private final FocusDao focusDao;

    public FocusRepository(Context context) {
        focusDao = new FocusDao(context);
    }

    public long saveFocusRecord(FocusRecord record) {
        return focusDao.insertFocusRecord(record);
    }

    public List<FocusRecord> loadUserFocusRecords(int userId) {
        return focusDao.getRecordsByUserId(userId);
    }

    public List<FocusRecord> loadTaskFocusRecords(int userId, int taskId) {
        return focusDao.getRecordsByTaskId(userId, taskId);
    }

    public List<FocusRecord> loadDailyFocusRecords(int userId, String date) {
        return focusDao.getRecordsByDate(userId, date);
    }

    public int loadTotalFocusCount(int userId) {
        return focusDao.getTotalFocusCount(userId);
    }

    public int loadTotalFocusMinutes(int userId) {
        return focusDao.getTotalFocusMinutes(userId);
    }

    public int loadDailyFocusCount(int userId, String date) {
        return focusDao.getDailyFocusCount(userId, date);
    }

    public int loadDailyFocusMinutes(int userId, String date) {
        return focusDao.getDailyFocusMinutes(userId, date);
    }
}
