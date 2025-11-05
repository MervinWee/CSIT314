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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class DailyReportSuccessTest {

    private FirebaseAuth mAuth;

    /**
     * This method runs before each test to sign in a test user.
     */
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

    /**
     * This method runs after each test to sign out the user.
     */
    @After
    public void teardown() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    @Test
    public void testGenerateDailyReport_SuccessCase_ForFutureDate() throws InterruptedException {
        PlatformDataAccount platformDataAccount = new PlatformDataAccount();
        final CountDownLatch testLatch = new CountDownLatch(1);
        final int[] results = new int[3];

        // Get a date for tomorrow, which is guaranteed to have no data.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        Date tomorrow = cal.getTime();

        // 1. Call the method we want to test for a future date.
        platformDataAccount.generateDailyReport(tomorrow, new PlatformDataAccount.DailyReportCallback() {
            @Override
            public void onReportDataLoaded(int newUserCount, int newRequestCount, int completedMatchesCount) {
                results[0] = newUserCount;
                results[1] = newRequestCount;
                results[2] = completedMatchesCount;
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

        // 2. Assert the result. It should correctly be 0 for a future date.
        assertEquals("Expected 0 new users for tomorrow, but found " + results[0], 0, results[0]);
        assertEquals("Expected 0 new requests for tomorrow, but found " + results[1], 0, results[1]);
        assertEquals("Expected 0 completed matches for tomorrow, but found " + results[2], 0, results[2]);
    }
}
