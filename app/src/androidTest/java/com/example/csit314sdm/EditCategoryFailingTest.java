package com.example.csit314sdm;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EditCategoryFailingTest {

    @Test
    public void testEditCategory_Failing_InvalidId() throws InterruptedException {
        PlatformDataAccount platformDataAccount = new PlatformDataAccount();
        final CountDownLatch latch = new CountDownLatch(1);

        Category invalidCategory = new Category("Original Name", "Original Desc");
        invalidCategory.setId(null);

        String newName = "New Name";
        String newDesc = "New Desc";

        platformDataAccount.updateCategory(invalidCategory, newName, newDesc, new PlatformDataAccount.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                // This block should not run. If it does, the test should fail immediately.
                fail("This test was expected to fail, but it succeeded.");
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                String incorrectExpectedMessage = "An incorrect message to guarantee the test fails.";
                assertEquals(incorrectExpectedMessage, message);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for the callback", latch.await(10, TimeUnit.SECONDS));
    }
}
