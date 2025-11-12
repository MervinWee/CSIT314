package com.example.csit314sdm;

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

    // --- Constructor (now empty) ---
    public HelpRequestController() {}

    // --- Dispatcher Methods (delegating calls to the HelpRequest Active Record) ---

    public void getHelpRequestById(String requestId, String userRole, final SingleRequestLoadCallback callback) {
        HelpRequest.findById(requestId, userRole, callback);
    }

    public void updateHelpRequest(HelpRequest request, final UpdateCallback callback) {
        request.update(callback);
    }

    public void getCategories(final CategoryListCallback callback) {
        HelpRequest.getCategories(callback);
    }

    public void cancelRequest(String requestId, final DeleteCallback callback) {
        HelpRequest.cancelRequest(requestId, callback);
    }

    public Query getFilteredHelpRequestsQuery(String statusFilter) {
        return HelpRequest.getFilteredHelpRequestsQuery(statusFilter);
    }

    public Query getMatchHistoryQuery(String category, Date fromDate, Date toDate) {
        return HelpRequest.getMatchHistoryQuery(category, fromDate, toDate);
    }

    public void getActiveHelpRequests(final HelpRequestsLoadCallback callback) {
        HelpRequest.getActiveHelpRequests(callback);
    }

    public void getInProgressRequestsForCsr(final HelpRequestsLoadCallback callback) {
        HelpRequest.getInProgressRequestsForCsr(callback);
    }

    public void getCompletedHistory(String companyId, Date fromDate, Date toDate, String category, final HelpRequestsLoadCallback callback) {
        HelpRequest.getCompletedHistory(companyId, fromDate, toDate, category, callback);
    }

    public void acceptRequest(String requestId, String companyId, String csrId, final UpdateCallback callback) {
        HelpRequest.acceptRequest(requestId, companyId, csrId, callback);
    }

    public void releaseRequestByCsr(String requestId, final UpdateCallback callback) {
        HelpRequest.releaseRequestByCsr(requestId, callback);
    }

    public void releaseRequestByPin(String requestId, final UpdateCallback callback) {
        HelpRequest.releaseRequestByPin(requestId, callback);
    }

    public void updateRequestStatus(String requestId, String newStatus, final UpdateCallback callback) {
        HelpRequest.updateRequestStatus(requestId, newStatus, callback);
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
