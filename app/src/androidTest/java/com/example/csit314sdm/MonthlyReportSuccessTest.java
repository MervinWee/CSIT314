package com.example.csit314sdm;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class MonthlyReportSuccessTest {

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
    public void testGenerateMonthlyReport_SuccessCase() throws InterruptedException {
        PlatformDataAccount platformDataAccount = new PlatformDataAccount();
        final CountDownLatch testLatch = new CountDownLatch(1);
        final String[] results = new String[2];

        // Define a future month to ensure it's empty.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // Calendar month is 0-indexed, method expects 1-indexed

        // 1. Call the method we want to test.
        platformDataAccount.generateMonthlyReport(year, month, new PlatformDataAccount.MonthlyReportCallback() {
            @Override
            public void onReportDataLoaded(String topCompany, String mostRequestedService) {
                results[0] = topCompany;
                results[1] = mostRequestedService;
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
        // For a future month, the expected result is "N/A" for both metrics.
        assertEquals("Expected 'N/A' as top company for a future month, but found " + results[0], "N/A", results[0]);
        assertEquals("Expected 'N/A' as most requested service for a future month, but found " + results[1], "N/A", results[1]);
    }
}
