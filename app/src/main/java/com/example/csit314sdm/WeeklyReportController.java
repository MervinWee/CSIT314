package com.example.csit314sdm;

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
        view.showToast("Generating weekly report...");

        platformDataAccount.generateWeeklyReport(startDate, endDate, new PlatformDataAccount.WeeklyReportCallback() {
            @Override
            public void onReportDataLoaded(int uniquePins, int uniqueCsrs, int totalMatches) {
                view.showReportData(uniquePins, uniqueCsrs, totalMatches);
                view.setGenerateButtonEnabled(true);
            }

            @Override
            public void onError(String message) {
                view.showError("Error: " + message);
                view.setGenerateButtonEnabled(true);
            }
        });
    }
}
