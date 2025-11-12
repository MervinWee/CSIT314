package com.example.csit314sdm;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.csit314sdm.entity.Category;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class DeleteCategorySuccessTest {
    private PlatformDataAccount platformDataAccount;

    @Test
    public void testDeleteCategory_Success() throws InterruptedException {
        platformDataAccount = new PlatformDataAccount();
        final CountDownLatch finalLatch = new CountDownLatch(1);
        final String categoryName = "Delete-Success-Test-" + System.currentTimeMillis();

        // Step 1: Create a category specifically for this test.
        platformDataAccount.createCategory(categoryName, "Category to be deleted", new PlatformDataAccount.FirebaseCallback() {
            @Override
            public void onSuccess(String createMessage) {
                // Step 2: If creation is successful, find the category to get its full object/ID.
                findAnddeleteCategory(categoryName, finalLatch);
            }

            @Override
            public void onError(String message) {
                fail("SETUP FAILED: Could not create the category to be deleted. Error: " + message);
            }
        });

        // This test waits for the entire create-find-delete process to complete.
        if (!finalLatch.await(30, TimeUnit.SECONDS)) {
            fail("TIMEOUT: The test failed to create and delete the category within 30 seconds.");
        }
    }

    private void findAnddeleteCategory(String categoryName, CountDownLatch finalLatch) {
        platformDataAccount.listenForCategoryChanges(new PlatformDataAccount.CategoryListCallback() {
            private boolean foundAndDeleting = false;

            @Override
            public void onDataLoaded(List<Category> categories) {
                if (foundAndDeleting) return; // Ensure we only try to delete once.

                for (Category cat : categories) {
                    if (cat.getName().equals(categoryName)) {
                        foundAndDeleting = true;
                        platformDataAccount.detachCategoryListener();
                        
                        // Step 3: Once found, delete the category and verify success.
                        platformDataAccount.deleteCategory(cat, new PlatformDataAccount.FirebaseCallback() {
                            @Override
                            public void onSuccess(String deleteMessage) {
                                assertEquals("Category deleted successfully.", deleteMessage);
                                finalLatch.countDown(); // SUCCESS! The test is complete.
                            }

                            @Override
                            public void onError(String message) {
                                fail("DELETION FAILED: The delete operation failed unexpectedly. Error: " + message);
                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onError(String message) {
                fail("FIND FAILED: Could not query categories to find the one to delete. Error: " + message);
            }
        });
    }

    @After
    public void tearDown() {
        if (platformDataAccount != null) {
            platformDataAccount.cleanupAllListeners();
        }
    }
}
