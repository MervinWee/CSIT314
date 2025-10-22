package com.example.csit314sdm;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HelpRequestAdapter extends RecyclerView.Adapter<HelpRequestAdapter.RequestViewHolder> {

    private List<HelpRequest> requestList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HelpRequest request);
    }

    public HelpRequestAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setRequests(List<HelpRequest> requests) {
        this.requestList = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_help_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        holder.bind(requestList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategory, tvOrg, tvDate, tvUrgency;
        private final Button btnViewDetails;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvRequestCategory);
            tvOrg = itemView.findViewById(R.id.tvRequestOrg);
            tvDate = itemView.findViewById(R.id.tvShortlistedDate);
            tvUrgency = itemView.findViewById(R.id.tvUrgency);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(final HelpRequest request, final OnItemClickListener listener) {
            // This line needs the 'category' field from Firestore.
            tvCategory.setText(request.getCategory());

            // This line needs the 'organization' field from Firestore.
            tvOrg.setText(request.getOrganization());

            // This line needs the 'shortlistedDate' field from Firestore.
            if (request.getShortlistedDate() != null) {
                long now = System.currentTimeMillis();
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(request.getShortlistedDate().getTime(), now, DateUtils.DAY_IN_MILLIS);
                tvDate.setText("Shortlisted " + relativeTime);
            }

            // This block needs the 'urgency' field from Firestore.
            if (request.getUrgency() != null && !request.getUrgency().isEmpty()) {
                tvUrgency.setText(request.getUrgency());
                tvUrgency.setVisibility(View.VISIBLE);
            } else {
                tvUrgency.setVisibility(View.GONE);
            }

            btnViewDetails.setOnClickListener(v -> listener.onItemClick(request));
        }
    }
}
