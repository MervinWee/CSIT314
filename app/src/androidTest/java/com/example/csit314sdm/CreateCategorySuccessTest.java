package com.example.csit314sdm;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class CreateCategorySuccessTest {

    @Test
    public void testCreateCategory_Success() throws InterruptedException {
        PlatformDataAccount platformDataAccount = new PlatformDataAccount();
        final CountDownLatch latch = new CountDownLatch(1);
        // Use a unique name to ensure the test passes
        String categoryName = "Test Category " + System.currentTimeMillis();
        String categoryDescription = "A test category description.";

        platformDataAccount.createCategory(categoryName, categoryDescription, new PlatformDataAccount.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                assertNotNull(message);
                assertFalse(message.isEmpty());
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                fail("Category creation failed with error: " + message);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for category creation", latch.await(10, TimeUnit.SECONDS));
    }
}
