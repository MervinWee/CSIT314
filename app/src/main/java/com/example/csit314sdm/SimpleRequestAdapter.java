// SimpleRequestAdapter.java
package com.example.csit314sdm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class SimpleRequestAdapter extends RecyclerView.Adapter<SimpleRequestAdapter.SimpleViewHolder> {

    private final List<HelpRequest> requestList;
    private final Context context;
    private final OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(HelpRequest request);
    }

    public SimpleRequestAdapter(List<HelpRequest> requestList, Context context, OnItemClickListener listener) {
        this.requestList = requestList;
        this.context = context;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the simple card layout we will create next.
        View view = LayoutInflater.from(context).inflate(R.layout.item_request_card_simple, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleViewHolder holder, int position) {
        HelpRequest request = requestList.get(position);
        holder.bind(request, clickListener);


        if (request.getStatus() != null) {
            holder.status.setText(request.getStatus());

            // Change the color of the bubble based on the status text.
            switch (request.getStatus()) {
                case "Open":
                    holder.status.setBackground(ContextCompat.getDrawable(context, R.drawable.status_bubble_open)); // Green
                    break;
                case "Taken":
                case "Shortlisted":
                    holder.status.setBackground(ContextCompat.getDrawable(context, R.drawable.status_bubble_taken)); // Blue
                    break;
                case "Completed":
                    holder.status.setBackground(ContextCompat.getDrawable(context, R.drawable.status_bubble_completed)); // Grey
                    break;
                default:
                    holder.status.setBackground(ContextCompat.getDrawable(context, R.drawable.status_bubble_default)); // Default color
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }


    static class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView status;

        public SimpleViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.item_request_title);
            status = itemView.findViewById(R.id.item_request_status);
        }


        public void bind(final HelpRequest request, final OnItemClickListener listener) {
            title.setText(request.getCategory());
            itemView.setOnClickListener(v -> listener.onItemClick(request));
        }
    }
}
