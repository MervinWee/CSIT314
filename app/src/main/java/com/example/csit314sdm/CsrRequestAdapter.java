// File: app/src/main/java/com/example/csit314sdm/CsrRequestAdapter.java
package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CsrRequestAdapter extends RecyclerView.Adapter<CsrRequestAdapter.RequestViewHolder> {

    private List<HelpRequest> requests;
    private OnItemClickListener listener;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(HelpRequest request);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CsrRequestAdapter(List<HelpRequest> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ASSUMPTION: You have a layout file named 'item_request_csr.xml'
        // If your layout file has a different name, change it here.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_csr, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        HelpRequest request = requests.get(position);
        holder.bind(request, listener);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    // Method to update the list of requests and refresh the RecyclerView
    public void setRequests(List<HelpRequest> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged(); // This tells the adapter to refresh its views
    }

    // The ViewHolder class
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        // ASSUMPTION: Your item_request_csr.xml has these TextViews.
        // Adjust the IDs to match your layout file.
        private TextView tvRequestTitle;
        private TextView tvRequestDescription;
        private TextView tvRequestLocation;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            // Replace with the actual IDs from your layout file
            tvRequestTitle = itemView.findViewById(R.id.tvRequestTitle);
            tvRequestDescription = itemView.findViewById(R.id.tvRequestDescription);
            tvRequestLocation = itemView.findViewById(R.id.tvRequestLocation);
        }

        public void bind(final HelpRequest request, final OnItemClickListener listener) {
            // Use getter methods from your HelpRequest class
            tvRequestTitle.setText(request.getTitle()); // Assuming HelpRequest has getTitle()
            tvRequestDescription.setText(request.getDescription()); // Assuming HelpRequest has getDescription()
            tvRequestLocation.setText(request.getLocation()); // Assuming HelpRequest has getLocation()

            // Set the click listener for the entire item view
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(request);
                }
            });
        }
    }
}
