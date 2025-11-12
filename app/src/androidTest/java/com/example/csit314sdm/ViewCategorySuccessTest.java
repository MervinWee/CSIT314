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

@RunWith(AndroidJUnit4.class)
public class ViewCategorySuccessTest {
    private PlatformDataAccount platformDataAccount;
    private final String testCategoryName = "View Test Category " + System.currentTimeMillis();
    private final String testCategoryDesc = "A category for viewing.";

    @Before
    public void setUp() throws InterruptedException {
        platformDataAccount = new PlatformDataAccount();
        // Ensure the category exists before we try to view it
        final CountDownLatch setupLatch = new CountDownLatch(1);
        platformDataAccount.createCategory(testCategoryName, testCategoryDesc, new PlatformDataAccount.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                setupLatch.countDown();
            }

            @Override
            public void onError(String message) {
                // It might already exist from a failed previous run, which is fine for this test.
                if (message.contains("already exists")) {
                    setupLatch.countDown();
                } else {
                    fail("Setup failed: " + message);
                }
            }
        });
        assertTrue("Setup timeout", setupLatch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testViewCategory_Success() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        platformDataAccount.listenForCategoryChanges(new PlatformDataAccount.CategoryListCallback() {
            @Override
            public void onDataLoaded(List<Category> categories) {
                assertNotNull(categories);
                assertFalse("Category list should not be empty", categories.isEmpty());

                boolean found = false;
                for (Category cat : categories) {
                    if (cat.getName().equals(testCategoryName)) {
                        found = true;
                        assertEquals(testCategoryDesc, cat.getDescription());
                        break;
                    }
                }
                assertTrue("Test category was not found in the loaded list", found);
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                fail("listenForCategoryChanges failed with error: " + message);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for category listener", latch.await(10, TimeUnit.SECONDS));
    }

    @After
    public void tearDown() {
        if (platformDataAccount != null) {
            platformDataAccount.cleanupAllListeners();
        }
    }
}
