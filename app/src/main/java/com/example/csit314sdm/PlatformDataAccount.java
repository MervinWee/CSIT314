package com.example.csit314sdm;

import java.util.Date;
import java.util.List;

public class PlatformDataAccount {

    // Daily Report
    public int getNewUserCount(Date date) {
        // TODO: Implement logic to get new user count for the given date
        return 0;
    }

    public int getNewRequestCount(Date date) {
        // TODO: Implement logic to get new request count for the given date
        return 0;
    }

    public int getCompletedMatchesCount(Date date) {
        // TODO: Implement logic to get completed matches count for the given date
        return 0;
    }

    // Weekly Report
    public int getTotalMatch(Date startDate, Date endDate) {
        // TODO: Implement logic to get total matches between the given dates
        return 0;
    }

    public int getUniqueActivePINs(Date startDate, Date endDate) {
        // TODO: Implement logic to get unique active PINs between the given dates
        return 0;
    }

    public int getUniqueActiveCRSs(Date startDate, Date endDate) {
        // TODO: Implement logic to get unique active CSRs between the given dates
        return 0;
    }

    // Monthly Report
    public String getTopPerformingCompany(int year, int month) {
        // TODO: Implement logic to get the top performing company for the given month and year
        return null;
    }

    public String getMostRequestedService(int year, int month) {
        // TODO: Implement logic to get the most requested service for the given month and year
        return null;
    }
}
