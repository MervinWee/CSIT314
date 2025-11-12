package com.example.csit314sdm;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class CreateCategoryFailingTest {

    @Test
    public void testCreateCategory_FailingCase() throws InterruptedException {
        PlatformDataAccount platformDataAccount = new PlatformDataAccount();
        final CountDownLatch latch = new CountDownLatch(2);
        String categoryName = "Duplicate Category Test";
        String categoryDescription = "A test category for duplication.";

        // First, create the category
        platformDataAccount.createCategory(categoryName, categoryDescription, new PlatformDataAccount.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                // This one should succeed
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                // If the category already exists from a previous run, that's okay for this test.
                latch.countDown();
            }
        });

        // Wait a moment for the first creation to complete
        latch.await(10, TimeUnit.SECONDS);

        // Now, try to create it again, expecting failure
        platformDataAccount.createCategory(categoryName, categoryDescription, new PlatformDataAccount.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                fail("Category creation should have failed for a duplicate category.");
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                assertNotNull(message);
                assertTrue(message.contains("already exists"));
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for duplicate category creation check", latch.await(10, TimeUnit.SECONDS));
    }
}
