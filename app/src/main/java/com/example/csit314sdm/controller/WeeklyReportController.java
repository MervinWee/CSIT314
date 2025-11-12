package com.example.csit314sdm.controller;

import com.example.csit314sdm.PlatformDataAccount;

import java.util.Date;

public class WeeklyReportController {

    private final View view;
    private final PlatformDataAccount platformDataAccount;

    public interface View {
        void showReportData(int uniquePins, int uniqueCsrs, int totalMatches);
        void showError(String message);
        void setGenerateButtonEnabled(boolean enabled);
        void showToast(String message);
    }

    public WeeklyReportController(View view, PlatformDataAccount platformDataAccount) {
        this.view = view;
        this.platformDataAccount = platformDataAccount;
    }

    public void generateReport(Date startDate, Date endDate) {
        view.setGenerateButtonEnabled(false);

        platformDataAccount.generateWeeklyReport(startDate, endDate, new PlatformDataAccount.WeeklyReportCallback() {
            @Override
            public void onReportDataLoaded(int uniquePins, int uniqueCsrs, int totalMatches) {
                view.showReportData(uniquePins, uniqueCsrs, totalMatches);
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
