package com.example.csit314sdm.controller;

import com.example.csit314sdm.PlatformDataAccount;

public class MonthlyReportController {

    private final View view;
    private final PlatformDataAccount platformDataAccount;

    public interface View {
        void showReportData(String topCompany, String mostRequestedService);
        void showError(String message);
        void setGenerateButtonEnabled(boolean enabled);
        void showToast(String message);
    }

    public MonthlyReportController(View view, PlatformDataAccount platformDataAccount) {
        this.view = view;
        this.platformDataAccount = platformDataAccount;
    }

    public void generateReport(int year, int month) {
        view.setGenerateButtonEnabled(false);

        platformDataAccount.generateMonthlyReport(year, month, new PlatformDataAccount.MonthlyReportCallback() {
            @Override
            public void onReportDataLoaded(String topCompany, String mostRequestedService) {
                view.showReportData(topCompany, mostRequestedService);
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
