// FINAL, UPGRADED, AND CLICKABLE ADAPTER
package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HelpRequestAdapter extends RecyclerView.Adapter<HelpRequestAdapter.RequestViewHolder> {

    private List<HelpRequest> requestList;
    private OnItemClickListener listener;

    // The interface for handling clicks is now restored.
    public interface OnItemClickListener {
        void onItemClick(HelpRequest request);
    }

    // The method to set the listener is now restored.
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public HelpRequestAdapter(List<HelpRequest> requestList) {
        this.requestList = requestList;
    }

    public void setRequests(List<HelpRequest> requests) {
        this.requestList = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // --- FIX: Use the new, more complex layout file ---
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_card, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        // Pass the listener to the ViewHolder's bind method.
        holder.bind(requestList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        // --- FIX: Declare all the views from the new layout ---
        TextView tvRequestTitle, tvRequestDescription, tvPostedDate, tvRequestStatus;
        Button btnViewDetails;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            // --- FIX: Find all the views by their ID in the new layout ---
            tvRequestTitle = itemView.findViewById(R.id.tvRequestTitle);
            tvRequestDescription = itemView.findViewById(R.id.tvRequestDescription);
            tvPostedDate = itemView.findViewById(R.id.tvPostedDate);
            tvRequestStatus = itemView.findViewById(R.id.tvRequestStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(final HelpRequest request, final OnItemClickListener listener) {
            // Populate the views
            tvRequestTitle.setText(request.getRequestType());
            tvRequestDescription.setText(request.getDescription());
            tvRequestStatus.setText(request.getStatus());

            if (request.getCreationTimestamp() != null) {
                long diff = System.currentTimeMillis() - request.getCreationTimestamp().getTime();
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                if (days == 0) tvPostedDate.setText("Posted today");
                else if (days == 1) tvPostedDate.setText("Posted yesterday");
                else tvPostedDate.setText("Posted " + days + " days ago");
            } else {
                tvPostedDate.setText("");
            }

            // --- FIX: The click listener logic is now restored ---
            if (btnViewDetails != null) {
                btnViewDetails.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(request);
                    }
                });
            }
        }
    }
}
