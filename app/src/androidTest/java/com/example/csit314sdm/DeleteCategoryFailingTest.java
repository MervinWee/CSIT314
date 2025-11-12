package com.example.csit314sdm;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class DeleteCategoryFailingTest {

    @Test
    public void testDeleteCategory_Failing() throws InterruptedException {
        PlatformDataAccount platformDataAccount = new PlatformDataAccount();
        final CountDownLatch latch = new CountDownLatch(1);

        // 1. Create a category object with a null or empty ID to trigger the failure.
        Category invalidCategory = new Category("Invalid", "This category has no ID");
        invalidCategory.setId(""); // The ID is invalid.

        platformDataAccount.deleteCategory(invalidCategory, new PlatformDataAccount.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                fail("This test was supposed to fail, but it unexpectedly succeeded.");
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                String expectedMessage = "An incorrect message to ensure the test fails.";
                assertEquals(expectedMessage, message);
                latch.countDown();
            }
        });

        assertTrue("Timeout: The callback was not triggered within 10 seconds.", latch.await(10, TimeUnit.SECONDS));
    }
}
