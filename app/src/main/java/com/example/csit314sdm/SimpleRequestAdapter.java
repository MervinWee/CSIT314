// SimpleRequestAdapter.java
package com.example.csit314sdm;

import android.view.LayoutInflater;    import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// --- FIX: Create the SimpleRequestAdapter class ---
public class SimpleRequestAdapter extends RecyclerView.Adapter<SimpleRequestAdapter.RequestViewHolder> {

    private List<HelpRequest> requestList;

    // Constructor to initialize the list of requests
    public SimpleRequestAdapter(List<HelpRequest> requestList) {
        this.requestList = requestList;
    }

    // This method is called when the RecyclerView needs a new ViewHolder.
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate a simple layout for each item.
        // NOTE: You must create a layout file named 'simple_request_item.xml'
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new RequestViewHolder(view);
    }

    // This method binds the data from your HelpRequest object to the views in the ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        HelpRequest request = requestList.get(position);
        // Assuming HelpRequest has a 'getTitle()' method or similar.
        // Change 'request.getTitle()' to whatever method returns the string you want to display.
        holder.titleTextView.setText(request.getTitle());
    }

    // Returns the total number of items in the list.
    @Override
    public int getItemCount() {
        return requestList.size();
    }

    // Method to update the list of requests and refresh the RecyclerView
    public void setRequests(List<HelpRequest> newRequestList) {
        this.requestList = newRequestList;
        notifyDataSetChanged(); // Notifies the adapter that the data has changed
    }

    // The ViewHolder class holds the views for a single item in the list.
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the TextView from the layout.
            // Using the default ID from 'android.R.layout.simple_list_item_1'.
            titleTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}
    