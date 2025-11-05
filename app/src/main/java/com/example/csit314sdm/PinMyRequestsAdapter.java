package com.example.csit314sdm;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PinMyRequestsAdapter extends RecyclerView.Adapter<PinMyRequestsAdapter.ViewHolder> {

    private final List<HelpRequest> requests;
    private final Context context;
    private final OnPinRequestClickListener clickListener;

    public interface OnPinRequestClickListener {
        void onItemClicked(HelpRequest request);
    }

    public PinMyRequestsAdapter(List<HelpRequest> requests, Context context, OnPinRequestClickListener listener) {
        this.requests = requests;
        this.context = context;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pin_my_request_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelpRequest request = requests.get(position);

        holder.title.setText(request.getCategory());
        holder.status.setText(request.getStatus());
        holder.viewCount.setText(String.valueOf(request.getViewCount()));

        // Set the "Date Posted" text
        if (request.getCreationTimestamp() != null) {
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(request.getCreationTimestamp().getTime());
            holder.date.setText("Date Posted: " + relativeTime);
        } else {
            holder.date.setText("Date not available");
        }


        if (request.getUrgencyLevel() != null && !request.getUrgencyLevel().isEmpty()) {
            holder.urgency.setVisibility(View.VISIBLE);
            holder.urgency.setText(request.getUrgencyLevel());


            switch (request.getUrgencyLevel().toLowerCase()) {
                case "low":
                    holder.urgency.setBackground(ContextCompat.getDrawable(context, R.drawable.urgency_low_background)); // Green
                    break;

                case "medium":
                    holder.urgency.setBackground(ContextCompat.getDrawable(context, R.drawable.urgency_medium_background)); // Yellow
                    break;
                case "high":
                    holder.urgency.setBackground(ContextCompat.getDrawable(context, R.drawable.urgency_high_background)); // Red
                    break;
                default:
                    holder.urgency.setVisibility(View.GONE);
                    break;
            }
        } else {

            holder.urgency.setVisibility(View.GONE);
        }

        switch (request.getStatus()) {
            case "Open":
                holder.status.setBackground(ContextCompat.getDrawable(context, R.drawable.status_bubble_open));
                break;
            case "Taken":
                holder.status.setBackground(ContextCompat.getDrawable(context, R.drawable.status_bubble_taken));
                break;
            case "Completed":
                holder.status.setBackground(ContextCompat.getDrawable(context, R.drawable.status_bubble_completed));
                break;
            default:
                holder.status.setBackground(ContextCompat.getDrawable(context, R.drawable.status_bubble_default));
                break;
        }


        holder.itemView.setOnClickListener(v -> clickListener.onItemClicked(request));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView status;
        TextView date;
        TextView urgency;
        TextView viewCount;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tvPinRequestTitle);
            status = view.findViewById(R.id.tvPinRequestStatus);
            date = view.findViewById(R.id.tvPinRequestDate);
            urgency = view.findViewById(R.id.tvPinRequestUrgency);
            viewCount = view.findViewById(R.id.tvPinRequestViewCount);
        }
    }
}
