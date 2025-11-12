package com.example.csit314sdm;

import com.google.firebase.firestore.Query;
import java.util.Date;
import java.util.List;

public class HelpRequestController {

    // --- Callbacks (interfaces for the Boundary layer to implement) ---
    public interface HelpRequestsLoadCallback { void onRequestsLoaded(List<HelpRequestEntity> requests); void onDataLoadFailed(String errorMessage); }
    public interface SingleRequestLoadCallback { void onRequestLoaded(HelpRequestEntity request); void onDataLoadFailed(String errorMessage); }
    public interface UpdateCallback { void onUpdateSuccess(); void onUpdateFailure(String errorMessage); }
    public interface DeleteCallback { void onDeleteSuccess(); void onDeleteFailure(String errorMessage); }
    public interface SaveCallback { void onSaveSuccess(); void onSaveFailure(String errorMessage); }
    public interface CategoryListCallback { void onCategoriesLoaded(List<Category> categories); void onDataLoadFailed(String errorMessage); }

    public HelpRequestController() {}

    public void getHelpRequestById(String requestId, String userRole, final SingleRequestLoadCallback callback) {
        HelpRequestEntity.findById(requestId, userRole, new HelpRequestEntity.LoadCallback() {
            @Override
            public void onRequestLoaded(HelpRequestEntity request) {
                if (callback != null) callback.onRequestLoaded(request);
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void updateHelpRequest(HelpRequestEntity request, final UpdateCallback callback) {
        request.update(new HelpRequestEntity.UpdateCallback() {
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
        HelpRequestEntity.getCategories(new HelpRequestEntity.CategoryListCallback() {
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
        HelpRequestEntity.cancelRequest(requestId, new HelpRequestEntity.UpdateCallback() {
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
        return HelpRequestEntity.getFilteredHelpRequestsQuery(statusFilter, userId, userRole);
    }

    public void getFilteredHelpRequests(String status, String userId, String userRole, final HelpRequestsLoadCallback callback) {
        HelpRequestEntity.getFilteredHelpRequests(status, userId, userRole, new HelpRequestEntity.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                if (callback != null) callback.onRequestsLoaded(requests);
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public Query getMatchHistoryQuery(String category, Date fromDate, Date toDate, String userId) {
        return HelpRequestEntity.getMatchHistoryQuery(category, fromDate, toDate, userId);
    }

    public void getActiveHelpRequests(final HelpRequestsLoadCallback callback) {
        HelpRequestEntity.getActiveHelpRequests(new HelpRequestEntity.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                if (callback != null) callback.onRequestsLoaded(requests);
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void getInProgressRequestsForCsr(final HelpRequestsLoadCallback callback) {
        HelpRequestEntity.getInProgressRequestsForCsr(new HelpRequestEntity.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                if (callback != null) callback.onRequestsLoaded(requests);
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void getSavedHelpRequests(final HelpRequestsLoadCallback callback) {
        HelpRequestEntity.getSavedHelpRequests(new HelpRequestEntity.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                if (callback != null) callback.onRequestsLoaded(requests);
            }
            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void saveRequest(String requestId, final SaveCallback callback) {
        HelpRequestEntity.saveRequest(requestId, new HelpRequestEntity.UpdateCallback() {
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
        HelpRequestEntity.unsaveRequest(requestId, new HelpRequestEntity.UpdateCallback() {
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
        HelpRequestEntity.getCompletedHistory(companyId, csrId, fromDate, toDate, category, new HelpRequestEntity.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                if (callback != null) callback.onRequestsLoaded(requests);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void searchShortlistedRequests(String keyword, String location, String category, final HelpRequestsLoadCallback callback) {
        HelpRequestEntity.searchShortlistedRequests(keyword, location, category, new HelpRequestEntity.ListCallback() {
            @Override
            public void onRequestsLoaded(List<HelpRequestEntity> requests) {
                if (callback != null) callback.onRequestsLoaded(requests);
            }

            @Override
            public void onDataLoadFailed(String errorMessage) {
                if (callback != null) callback.onDataLoadFailed(errorMessage);
            }
        });
    }

    public void acceptRequest(String requestId, String companyId, String csrId, final UpdateCallback callback) {
        HelpRequestEntity.acceptRequest(requestId, companyId, csrId, new HelpRequestEntity.UpdateCallback() {
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
        HelpRequestEntity.releaseRequestByCsr(requestId, new HelpRequestEntity.UpdateCallback() {
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
        HelpRequestEntity.releaseRequestByPin(requestId, new HelpRequestEntity.UpdateCallback() {
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
        HelpRequestEntity.updateRequestStatus(requestId, newStatus, new HelpRequestEntity.UpdateCallback() {
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
    public String getCategory(HelpRequestEntity request) { return request.getCategory(); }
    public String getStatus(HelpRequestEntity request) { return request.getStatus(); }
    public String getDescription(HelpRequestEntity request) { return request.getDescription(); }
    public String getLocation(HelpRequestEntity request) { return request.getLocation(); }
    public String getPreferredTime(HelpRequestEntity request) { return request.getPreferredTime(); }
    public String getUrgencyLevel(HelpRequestEntity request) { return request.getUrgencyLevel(); }
    public Date getCreationTimestamp(HelpRequestEntity request) { return request.getCreationTimestamp(); }
    public String getAcceptedByCsrId(HelpRequestEntity request) { return request.getAcceptedByCsrId(); }
    public String getPinName(HelpRequestEntity request) { return request.getPinName(); }
    public String getPinPhoneNumber(HelpRequestEntity request) { return request.getPinPhoneNumber(); }
    public String getPinShortId(HelpRequestEntity request) { return request.getPinShortId(); }
    public long getViewCount(HelpRequestEntity request) { return request.getViewCount(); }
    public int getShortlistCount(HelpRequestEntity request) {
        if (request.getSavedByCsrId() != null) {
            return request.getSavedByCsrId().size();
        }
        return 0;
    }
}
