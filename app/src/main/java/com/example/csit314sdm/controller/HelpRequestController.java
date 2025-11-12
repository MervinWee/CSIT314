package com.example.csit314sdm.controller;

import com.example.csit314sdm.entity.Category;
import com.example.csit314sdm.entity.HelpRequest;
import com.google.firebase.firestore.Query;
import java.util.Date;
import java.util.List;

public class HelpRequestController {

    // --- Callbacks (interfaces for the Boundary layer to implement) ---
    public interface HelpRequestsLoadCallback { void onRequestsLoaded(List<HelpRequest> requests); void onDataLoadFailed(String errorMessage); }
    public interface SingleRequestLoadCallback { void onRequestLoaded(HelpRequest request); void onDataLoadFailed(String errorMessage); }
    public interface UpdateCallback { void onUpdateSuccess(); void onUpdateFailure(String errorMessage); }
    public interface DeleteCallback { void onDeleteSuccess(); void onDeleteFailure(String errorMessage); }
    public interface SaveCallback { void onSaveSuccess(); void onSaveFailure(String errorMessage); }
    public interface CategoryListCallback { void onCategoriesLoaded(List<Category> categories); void onDataLoadFailed(String errorMessage); }

    public HelpRequestController() {}

    public void getHelpRequestById(String requestId, String userRole, final SingleRequestLoadCallback callback) {
        HelpRequest.findById(requestId, userRole, new HelpRequest.LoadCallback() {
            @Override
            public void onRequestLoaded(HelpRequest request) {
                if (callback != null) callback.onRequestLoaded(request);
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void updateHelpRequest(HelpRequest request, final UpdateCallback callback) {
        request.update(new HelpRequest.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                if (callback != null) callback.onUpdateSuccess();
            }
            @Override
            public void onUpdateFailure(String errorMessage) {
                if (callback != null) callback.onUpdateFailure(errorMessage);
            }
        });
    }

    public void getCategories(final CategoryListCallback callback) {
        HelpRequest.getCategories(new HelpRequest.CategoryListCallback() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                if (callback != null) callback.onCategoriesLoaded(categories);
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void cancelRequest(String requestId, final DeleteCallback callback) {
        HelpRequest.cancelRequest(requestId, new HelpRequest.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                if (callback != null) callback.onDeleteSuccess();
            }
            @Override
            public void onUpdateFailure(String errorMessage) {
                if (callback != null) callback.onDeleteFailure(errorMessage);
            }
        });
    }

    public Query getFilteredHelpRequestsQuery(String statusFilter, String userId, String userRole) {
        return HelpRequest.getFilteredHelpRequestsQuery(statusFilter, userId, userRole);
    }

    public void getFilteredHelpRequests(String status, String userId, String userRole, final HelpRequestsLoadCallback callback) {
        HelpRequest.getFilteredHelpRequests(status, userId, userRole, new HelpRequest.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                if (callback != null) callback.onRequestsLoaded(requests);
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public Query getMatchHistoryQuery(String category, Date fromDate, Date toDate, String userId) {
        return HelpRequest.getMatchHistoryQuery(category, fromDate, toDate, userId);
    }

    public void getActiveHelpRequests(final HelpRequestsLoadCallback callback) {
        HelpRequest.getActiveHelpRequests(new HelpRequest.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                if (callback != null) callback.onRequestsLoaded(requests);
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void getInProgressRequestsForCsr(final HelpRequestsLoadCallback callback) {
        HelpRequest.getInProgressRequestsForCsr(new HelpRequest.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                if (callback != null) callback.onRequestsLoaded(requests);
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void getSavedHelpRequests(final HelpRequestsLoadCallback callback) {
        HelpRequest.getSavedHelpRequests(new HelpRequest.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                if (callback != null) callback.onRequestsLoaded(requests);
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void saveRequest(String requestId, final SaveCallback callback) {
        HelpRequest.saveRequest(requestId, new HelpRequest.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                if (callback != null) callback.onSaveSuccess();
            }

            @Override
            public void onUpdateFailure(String errorMessage) {
                if (callback != null) callback.onSaveFailure(errorMessage);
            }
        });
    }

    public void unsaveRequest(String requestId, final SaveCallback callback) {
        HelpRequest.unsaveRequest(requestId, new HelpRequest.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                if (callback != null) callback.onSaveSuccess();
            }

            @Override
            public void onUpdateFailure(String errorMessage) {
                if (callback != null) callback.onSaveFailure(errorMessage);
            }
        });
    }

    public void getCompletedHistory(String companyId, String csrId, Date fromDate, Date toDate, String category, final HelpRequestsLoadCallback callback) {
        HelpRequest.getCompletedHistory(companyId, csrId, fromDate, toDate, category, new HelpRequest.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                if (callback != null) callback.onRequestsLoaded(requests);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void searchShortlistedRequests(String keyword, String location, String category, final HelpRequestsLoadCallback callback) {
        HelpRequest.searchShortlistedRequests(keyword, location, category, new HelpRequest.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequest> requests) {
                if (callback != null) callback.onRequestsLoaded(requests);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void acceptRequest(String requestId, String companyId, String csrId, final UpdateCallback callback) {
        HelpRequest.acceptRequest(requestId, companyId, csrId, new HelpRequest.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                if (callback != null) callback.onUpdateSuccess();
            }

            @Override
            public void onUpdateFailure(String errorMessage) {
                if (callback != null) callback.onUpdateFailure(errorMessage);
            }
        });
    }

    public void releaseRequestByCsr(String requestId, final UpdateCallback callback) {
        HelpRequest.releaseRequestByCsr(requestId, new HelpRequest.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                if (callback != null) callback.onUpdateSuccess();
            }

            @Override
            public void onUpdateFailure(String errorMessage) {
                if (callback != null) callback.onUpdateFailure(errorMessage);
            }
        });
    }

    public void releaseRequestByPin(String requestId, final UpdateCallback callback) {
        HelpRequest.releaseRequestByPin(requestId, new HelpRequest.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                if (callback != null) callback.onUpdateSuccess();
            }

            @Override
            public void onUpdateFailure(String errorMessage) {
                if (callback != null) callback.onUpdateFailure(errorMessage);
            }
        });
    }

    public void updateRequestStatus(String requestId, String newStatus, final UpdateCallback callback) {
        HelpRequest.updateRequestStatus(requestId, newStatus, new HelpRequest.UpdateCallback() {
            @Override
            public void onUpdateSuccess() {
                if (callback != null) callback.onUpdateSuccess();
            }

            @Override
            public void onUpdateFailure(String errorMessage) {
                if (callback != null) callback.onUpdateFailure(errorMessage);
            }
        });
    }

    // --- Data Accessor Dispatchers ---
    public String getCategory(HelpRequest request) { return request.getCategory(); }
    public String getStatus(HelpRequest request) { return request.getStatus(); }
    public String getDescription(HelpRequest request) { return request.getDescription(); }
    public String getLocation(HelpRequest request) { return request.getLocation(); }
    public String getPreferredTime(HelpRequest request) { return request.getPreferredTime(); }
    public String getUrgencyLevel(HelpRequest request) { return request.getUrgencyLevel(); }
    public Date getCreationTimestamp(HelpRequest request) { return request.getCreationTimestamp(); }
    public String getAcceptedByCsrId(HelpRequest request) { return request.getAcceptedByCsrId(); }
    public String getPinName(HelpRequest request) { return request.getPinName(); }
    public String getPinPhoneNumber(HelpRequest request) { return request.getPinPhoneNumber(); }
    public String getPinShortId(HelpRequest request) { return request.getPinShortId(); }
    public long getViewCount(HelpRequest request) { return request.getViewCount(); }
    public int getShortlistCount(HelpRequest request) {
        if (request.getSavedByCsrId() != null) {
            return request.getSavedByCsrId().size();
        }
        return 0;
    }
}
