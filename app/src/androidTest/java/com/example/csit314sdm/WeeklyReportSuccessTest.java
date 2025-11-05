package com.example.csit314sdm;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class WeeklyReportSuccessTest {

    private FirebaseAuth mAuth;

    @Before
    public void setup() throws InterruptedException {
        mAuth = FirebaseAuth.getInstance();
        final CountDownLatch authLatch = new CountDownLatch(1);

        mAuth.signInWithEmailAndPassword("test@example.com", "password123")
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        fail("Test setup failed: Could not sign in test user.");
                    }
                    authLatch.countDown();
                });
        boolean signedIn = authLatch.await(10, TimeUnit.SECONDS);
        assertTrue("Test setup failed: Sign in timed out.", signedIn);
    }

    @After
    public void teardown() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    @Test
    public void testGenerateWeeklyReport_SuccessCase() throws InterruptedException {
        PlatformDataAccount platformDataAccount = new PlatformDataAccount();
        final CountDownLatch testLatch = new CountDownLatch(1);
        final int[] results = new int[3];

        // Define a date range in the future to ensure it's empty.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        Date startDate = cal.getTime();
        cal.add(Calendar.DATE, 7);
        Date endDate = cal.getTime();

        // 1. Call the method we want to test.
        platformDataAccount.generateWeeklyReport(startDate, endDate, new PlatformDataAccount.WeeklyReportCallback() {
            @Override
            public void onReportDataLoaded(int uniquePins, int uniqueCsrs, int totalMatches) {
                results[0] = uniquePins;
                results[1] = uniqueCsrs;
                results[2] = totalMatches;
                testLatch.countDown();
            }

            @Override
            public void onError(String message) {
                fail("Report generation failed with error: " + message);
                testLatch.countDown();
            }
        });

        boolean latchCompleted = testLatch.await(10, TimeUnit.SECONDS);
        assertTrue("The async callback did not complete in time.", latchCompleted);

        // 2. Assert the result (corrected for success).
        // For a future week, all metrics should be 0.
        assertEquals("Expected 0 unique PINs for a future week, but found " + results[0], 0, results[0]);
        assertEquals("Expected 0 unique CSRs for a future week, but found " + results[1], 0, results[1]);
        assertEquals("Expected 0 total matches for a future week, but found " + results[2], 0, results[2]);
    }
}
