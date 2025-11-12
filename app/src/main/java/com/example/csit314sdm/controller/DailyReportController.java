package com.example.csit314sdm.controller;

import com.example.csit314sdm.PlatformDataAccount;

import java.util.Date;

public class DailyReportController {

    private final View view;
    private final PlatformDataAccount platformDataAccount;

    public interface View {
        void showReportData(int newUserCount, int newRequestCount, int completedMatchesCount);
        void showError(String message);
        void setGenerateButtonEnabled(boolean enabled);
        void showToast(String message);
    }

    public DailyReportController(View view, PlatformDataAccount platformDataAccount) {
        this.view = view;
        this.platformDataAccount = platformDataAccount;
    }

    public void generateReport(Date selectedDate) {
        view.setGenerateButtonEnabled(false);

        platformDataAccount.generateDailyReport(selectedDate, new PlatformDataAccount.DailyReportCallback() {
            @Override
            public void onReportDataLoaded(int newUserCount, int newRequestCount, int completedMatchesCount) {
                view.showReportData(newUserCount, newRequestCount, completedMatchesCount);
                view.setGenerateButtonEnabled(true);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
                view.setGenerateButtonEnabled(true);
            }
        });
    }
}
