package com.example.csit314sdm;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HelpRequestAdapter extends RecyclerView.Adapter<HelpRequestAdapter.RequestViewHolder> {

    private List<HelpRequest> requestList = new ArrayList<>();
    private final OnItemClickListener listener;
    private OnSaveClickListener saveClickListener;
    private String currentUserId;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(HelpRequest request);
    }

    public interface OnSaveClickListener {
        void onSaveClick(HelpRequest request, boolean isSaved);
    }

    public HelpRequestAdapter(OnItemClickListener listener, Context context) {
        this.listener = listener;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
        this.context = context;
    }

    public void setOnSaveClickListener(OnSaveClickListener listener) {
        this.saveClickListener = listener;
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
        holder.bind(requestList.get(position), listener, saveClickListener, currentUserId, context);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategory, tvOrg, tvDate, tvUrgency;
        private final Button btnViewDetails;
        private final ImageButton btnSave;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvRequestCategory);
            tvOrg = itemView.findViewById(R.id.tvRequestOrg);
            tvDate = itemView.findViewById(R.id.tvShortlistedDate);
            tvUrgency = itemView.findViewById(R.id.tvUrgency);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnSave = itemView.findViewById(R.id.btnSave);
        }

        public void bind(final HelpRequest request, final OnItemClickListener listener, final OnSaveClickListener saveClickListener, String currentUserId, Context context) {
            tvCategory.setText(request.getCategory());
            tvOrg.setText(request.getOrganization());

            if (request.getShortlistedDate() != null) {
                long now = System.currentTimeMillis();
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(request.getShortlistedDate().getTime(), now, DateUtils.DAY_IN_MILLIS);
                tvDate.setText("Shortlisted " + relativeTime);
            }

            if (request.getUrgency() != null && !request.getUrgency().isEmpty()) {
                String urgency = request.getUrgency().trim();
                tvUrgency.setText(urgency);
                tvUrgency.setVisibility(View.VISIBLE);

                if ("High Urgency".equals(urgency)) {
                    tvUrgency.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background_red));
                } else if ("Moderate Urgency".equals(urgency)) {
                    tvUrgency.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background_yellow));
                } else if ("Low Urgency".equals(urgency)) {
                    tvUrgency.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background_green));
                } else {
                    tvUrgency.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background));
                }
            } else {
                tvUrgency.setVisibility(View.GONE);
            }

            final boolean isSaved = request.getSavedBy() != null && request.getSavedBy().contains(currentUserId);
            btnSave.setImageResource(isSaved ? R.drawable.ic_star_filled : R.drawable.ic_star);

            btnViewDetails.setOnClickListener(v -> listener.onItemClick(request));

            if (saveClickListener != null) {
                btnSave.setOnClickListener(v -> {
                    saveClickListener.onSaveClick(request, isSaved);
                });
            }
        }
    }
}
