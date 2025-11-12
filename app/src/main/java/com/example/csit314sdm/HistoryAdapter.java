package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HelpRequestEntity> requests;
    private OnItemClickListener listener;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(HelpRequestEntity request);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public HistoryAdapter(List<HelpRequestEntity> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HelpRequestEntity currentRequest = requests.get(position);
        holder.bind(currentRequest, listener);
    }

    @Override
    public int getItemCount() {
        return requests != null ? requests.size() : 0;
    }

    public void setRequests(List<HelpRequestEntity> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }


    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategory;
        private TextView tvDate;
        private TextView tvStatus;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views from the 'history_item.xml' layout
            tvCategory = itemView.findViewById(R.id.tvHistoryItemCategory);
            tvDate = itemView.findViewById(R.id.tvHistoryItemDate);
            tvStatus = itemView.findViewById(R.id.tvHistoryItemStatus);
        }

        public void bind(final HelpRequestEntity request, final OnItemClickListener listener) {
            tvCategory.setText(request.getCategory());
            tvStatus.setText(request.getStatus());


            if (request.getCreationTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                tvDate.setText(sdf.format(request.getCreationTimestamp()));
            } else {
                tvDate.setText("No date available");
            }


            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(request);
                }
            });
        }
    }
}
