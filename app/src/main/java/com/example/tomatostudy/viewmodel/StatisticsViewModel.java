package com.example.tomatostudy.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.tomatostudy.database.model.FocusDailyTrendItem;
import com.example.tomatostudy.database.model.FocusDurationItem;
import com.example.tomatostudy.database.model.FocusTimePeriodItem;
import com.example.tomatostudy.database.model.User;
import com.example.tomatostudy.repository.FocusRepository;
import com.example.tomatostudy.repository.UserRepository;
import com.example.tomatostudy.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {

    private final FocusRepository focusRepository;
    private final UserRepository userRepository;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        focusRepository = new FocusRepository(application);
        userRepository = new UserRepository(application);
    }

    public TotalFocusData loadTotalFocusData() {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            return new TotalFocusData(0, 0, 0);
        }

        int totalCount = focusRepository.loadTotalFocusCount(currentUser.getId());
        int totalMinutes = focusRepository.loadTotalFocusMinutes(currentUser.getId());
        int focusDays = focusRepository.loadTotalFocusDays(currentUser.getId());
        int averageDailyMinutes = focusDays == 0 ? 0 : totalMinutes / focusDays;
        return new TotalFocusData(totalCount, totalMinutes, averageDailyMinutes);
    }

    public void loadTotalFocusDataAsync(final AppExecutors.Callback<TotalFocusData> callback) {
        AppExecutors.executeOnIo(new Runnable() {
            @Override
            public void run() {
                final TotalFocusData data = loadTotalFocusData();
                AppExecutors.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onComplete(data);
                        }
                    }
                });
            }
        });
    }

    public DailyFocusData loadDailyFocusData(String date) {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            return new DailyFocusData(0, 0);
        }

        int dailyCount = focusRepository.loadDailyFocusCount(currentUser.getId(), date);
        int dailyMinutes = focusRepository.loadDailyFocusMinutes(currentUser.getId(), date);
        return new DailyFocusData(dailyCount, dailyMinutes);
    }

    public void loadDailyFocusDataAsync(final String date,
                                        final AppExecutors.Callback<DailyFocusData> callback) {
        AppExecutors.executeOnIo(new Runnable() {
            @Override
            public void run() {
                final DailyFocusData data = loadDailyFocusData(date);
                AppExecutors.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onComplete(data);
                        }
                    }
                });
            }
        });
    }

    public List<FocusDurationItem> loadDailyTaskDurationDistribution(String date) {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }

        return focusRepository.loadDailyTaskDurationDistribution(currentUser.getId(), date);
    }

    public void loadDailyTaskDurationDistributionAsync(final String date,
                                                       final AppExecutors.Callback<List<FocusDurationItem>> callback) {
        AppExecutors.executeOnIo(new Runnable() {
            @Override
            public void run() {
                final List<FocusDurationItem> items = loadDailyTaskDurationDistribution(date);
                AppExecutors.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onComplete(items);
                        }
                    }
                });
            }
        });
    }

    public List<FocusTimePeriodItem> loadMonthlyTimePeriodDistribution(long monthStartTime,
                                                                       long nextMonthStartTime) {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }

        return focusRepository.loadMonthlyTimePeriodDistribution(
                currentUser.getId(),
                monthStartTime,
                nextMonthStartTime
        );
    }

    public void loadMonthlyTimePeriodDistributionAsync(final long monthStartTime,
                                                       final long nextMonthStartTime,
                                                       final AppExecutors.Callback<List<FocusTimePeriodItem>> callback) {
        AppExecutors.executeOnIo(new Runnable() {
            @Override
            public void run() {
                final List<FocusTimePeriodItem> items = loadMonthlyTimePeriodDistribution(
                        monthStartTime,
                        nextMonthStartTime
                );
                AppExecutors.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onComplete(items);
                        }
                    }
                });
            }
        });
    }

    public List<FocusDailyTrendItem> loadMonthlyFocusTrend(long monthStartTime,
                                                           long nextMonthStartTime,
                                                           int daysInMonth) {
        User currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }

        return focusRepository.loadMonthlyFocusTrend(
                currentUser.getId(),
                monthStartTime,
                nextMonthStartTime,
                daysInMonth
        );
    }

    public void loadMonthlyFocusTrendAsync(final long monthStartTime,
                                           final long nextMonthStartTime,
                                           final int daysInMonth,
                                           final AppExecutors.Callback<List<FocusDailyTrendItem>> callback) {
        AppExecutors.executeOnIo(new Runnable() {
            @Override
            public void run() {
                final List<FocusDailyTrendItem> items = loadMonthlyFocusTrend(
                        monthStartTime,
                        nextMonthStartTime,
                        daysInMonth
                );
                AppExecutors.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onComplete(items);
                        }
                    }
                });
            }
        });
    }

    public static class TotalFocusData {

        private final int totalCount;
        private final int totalMinutes;
        private final int averageDailyMinutes;

        public TotalFocusData(int totalCount, int totalMinutes, int averageDailyMinutes) {
            this.totalCount = totalCount;
            this.totalMinutes = totalMinutes;
            this.averageDailyMinutes = averageDailyMinutes;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getTotalMinutes() {
            return totalMinutes;
        }

        public int getAverageDailyMinutes() {
            return averageDailyMinutes;
        }
    }

    public static class DailyFocusData {

        private final int dailyCount;
        private final int dailyMinutes;

        public DailyFocusData(int dailyCount, int dailyMinutes) {
            this.dailyCount = dailyCount;
            this.dailyMinutes = dailyMinutes;
        }

        public int getDailyCount() {
            return dailyCount;
        }

        public int getDailyMinutes() {
            return dailyMinutes;
        }
    }
}
