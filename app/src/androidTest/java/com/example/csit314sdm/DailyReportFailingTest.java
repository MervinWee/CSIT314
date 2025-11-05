package com.example.csit314sdm;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class DailyReportFailingTest {

    @Test
    public void testGenerateDailyReport_FailingCase() throws InterruptedException {
        PlatformDataAccount platformDataAccount = new PlatformDataAccount();
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] results = new int[3];

        // 1. Call the method we want to test
        platformDataAccount.generateDailyReport(new Date(), new PlatformDataAccount.DailyReportCallback() {
            @Override
            public void onReportDataLoaded(int newUserCount, int newRequestCount, int completedMatchesCount) {
                results[0] = newUserCount;
                results[1] = newRequestCount;
                results[2] = completedMatchesCount;
                latch.countDown(); // Notify the test that the async operation is complete
            }

            @Override
            public void onError(String message) {
                fail("Report generation failed with error: " + message);
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS); // Wait for the async callback to complete

        // 2. Assert the result (intentionally failing)
        // In a clean test environment, we expect 0 new users for the current day.
        // To demonstrate a failing case, we assert the result is 1.
        assertEquals("Expected 1 new user, but found " + results[0], 1, results[0]);
    }
}
