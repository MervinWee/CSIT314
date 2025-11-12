package com.example.csit314sdm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.csit314sdm.entity.Category;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EditCategorySuccessTest {
    private PlatformDataAccount platformDataAccount;
    private final String originalName = "Edit Test Original " + System.currentTimeMillis();
    private final String originalDesc = "Original description.";
    private Category categoryToEdit;

    @Before
    public void setUp() throws InterruptedException {
        platformDataAccount = new PlatformDataAccount();
        final CountDownLatch setupLatch = new CountDownLatch(1);

        platformDataAccount.createCategory(originalName, originalDesc, new PlatformDataAccount.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                // Find the created category to get its ID
                platformDataAccount.listenForCategoryChanges(new PlatformDataAccount.CategoryListCallback() {
                    @Override
                    public void onDataLoaded(List<Category> categories) {
                        for (Category cat : categories) {
                            if (cat.getName().equals(originalName)) {
                                categoryToEdit = cat;
                                platformDataAccount.detachCategoryListener();
                                setupLatch.countDown();
                                break;
                            }
                        }
                    }
                    @Override
                    public void onError(String message) { fail("Failed to load categories in setup"); }
                });
            }
            @Override
            public void onError(String message) {
                if (message.contains("already exists")) {
                    fail("Test setup error: category should be unique but already exists.");
                } else {
                    fail("Setup failed to create category: " + message);
                }
            }
        });
        assertTrue("Setup timeout", setupLatch.await(20, TimeUnit.SECONDS));
        assertNotNull("Category to be edited was not found", categoryToEdit);
    }

    @Test
    public void testEditCategory_Success() throws InterruptedException {
        final CountDownLatch editLatch = new CountDownLatch(1);
        String newName = "Edited Name " + System.currentTimeMillis();
        String newDesc = "Updated description.";

        platformDataAccount.updateCategory(categoryToEdit, newName, newDesc, new PlatformDataAccount.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                assertEquals("Category updated successfully.", message);
                editLatch.countDown();
            }

            @Override
            public void onError(String message) {
                fail("Category update failed unexpectedly: " + message);
                editLatch.countDown();
            }
        });

        assertTrue("Timeout waiting for category update", editLatch.await(10, TimeUnit.SECONDS));
    }

    @After
    public void tearDown() {
        if (platformDataAccount != null) {
            platformDataAccount.cleanupAllListeners();
        }
    }
}
