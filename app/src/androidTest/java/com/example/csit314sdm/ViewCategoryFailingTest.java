package com.example.csit314sdm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
public class ViewCategoryFailingTest {
    private PlatformDataAccount platformDataAccount;

    @Before
    public void setUp() {
        platformDataAccount = new PlatformDataAccount();
    }

    @Test
    public void testCategoryListener_FailsAfterDetach() throws InterruptedException {
        final CountDownLatch initialLoadLatch = new CountDownLatch(1);
        final AtomicInteger callCount = new AtomicInteger(0);

        platformDataAccount.listenForCategoryChanges(new PlatformDataAccount.CategoryListCallback() {
            @Override
            public void onDataLoaded(List<Category> categories) {
                callCount.incrementAndGet();
                if (initialLoadLatch.getCount() > 0) {
                    initialLoadLatch.countDown();
                }
            }

            @Override
            public void onError(String message) {
                fail("Listener failed with an unexpected error: " + message);
            }
        });

        // Wait for the initial data to be loaded.
        assertTrue("Timeout waiting for initial category load", initialLoadLatch.await(10, TimeUnit.SECONDS));

        // 2. Detach the listener.
        platformDataAccount.detachCategoryListener();

        // 3. Create a new category. This should NOT trigger the detached listener.
        String newCategoryName = "Detached Test Category " + System.currentTimeMillis();
        platformDataAccount.createCategory(newCategoryName, "This should not be seen by the listener", null);

        // 4. Wait a moment to ensure any potential (and incorrect) update has time to arrive.
        Thread.sleep(3000);

        // 5. Assert that the listener was called more than once. This will FAIL because the listener
        // was correctly detached and the callCount is still 1. This makes the test result "Failing".
        assertEquals("Listener was not called again after being detached.", 2, callCount.get());
    }

    @After
    public void tearDown() {
        if (platformDataAccount != null) {
            platformDataAccount.cleanupAllListeners();
        }
    }
}
