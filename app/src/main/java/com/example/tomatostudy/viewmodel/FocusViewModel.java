package com.example.tomatostudy.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.tomatostudy.database.model.FocusRecord;
import com.example.tomatostudy.database.model.User;
import com.example.tomatostudy.repository.FocusRepository;
import com.example.tomatostudy.repository.UserRepository;
import com.example.tomatostudy.util.TimeZoneUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FocusViewModel extends AndroidViewModel {

    private final FocusRepository focusRepository;
    private final UserRepository userRepository;

    public FocusViewModel(@NonNull Application application) {
        super(application);
        focusRepository = new FocusRepository(application);
        userRepository = new UserRepository(application);
    }

    public boolean saveFocusRecord(int taskId,
                                   String taskTitle,
                                   long startTime,
                                   long endTime,
                                   int durationMinutes,
                                   boolean completed) {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null || taskId <= 0 || startTime <= 0 || endTime <= 0) {
            return false;
        }

        FocusRecord record = new FocusRecord();
        record.setUserId(currentUser.getId());
        record.setTaskId(taskId);
        record.setTaskTitle(taskTitle);
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setDurationMinutes(durationMinutes);
        record.setCompleted(completed);
        record.setCreatedDate(formatDate(endTime));
        return focusRepository.saveFocusRecord(record) > 0;
    }

    private String formatDate(long timeMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        dateFormat.setTimeZone(TimeZoneUtils.CHINA_TIME_ZONE);
        return dateFormat.format(new Date(timeMillis));
    }
}
