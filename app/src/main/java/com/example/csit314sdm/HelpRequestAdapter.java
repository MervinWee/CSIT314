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

    // FIX: The interface now correctly uses the controller's UpdateCallback
    public interface OnSaveClickListener {
        void onSaveClick(HelpRequest request, boolean isSaved);
    }

    public HelpRequestAdapter(OnItemClickListener listener) {
        this.listener = listener;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
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
        this.context = parent.getContext();
        View view = LayoutInflater.from(this.context).inflate(R.layout.item_help_request, parent, false);
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
        private final TextView tvCategory, tvOrg, tvDate, tvUrgency, tvPinName, tvPinId;
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
            tvPinName = itemView.findViewById(R.id.tvPinName);
            tvPinId = itemView.findViewById(R.id.tvPinId);
        }

        public void bind(final HelpRequest request, final OnItemClickListener listener, final OnSaveClickListener saveClickListener, String currentUserId, Context context) {
            tvCategory.setText(request.getCategory());
            tvOrg.setText(request.getOrganization());

            if (request.getPinName() != null) {
                tvPinName.setText("PIN Name: " + request.getPinName());
                tvPinName.setVisibility(View.VISIBLE);
            } else {
                tvPinName.setVisibility(View.GONE);
            }

            if (request.getPinShortId() != null) {
                tvPinId.setText("PIN ID: " + request.getPinShortId());
                tvPinId.setVisibility(View.VISIBLE);
            } else {
                tvPinId.setVisibility(View.GONE);
            }

            if (request.getCreationTimestamp() != null) {
                long now = System.currentTimeMillis();
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(request.getCreationTimestamp().getTime(), now, DateUtils.DAY_IN_MILLIS);
                tvDate.setText("Posted " + relativeTime);
            }

            if (request.getUrgencyLevel() != null && !request.getUrgencyLevel().isEmpty()) {
                String urgency = request.getUrgencyLevel().trim();
                tvUrgency.setText(urgency);
                tvUrgency.setVisibility(View.VISIBLE);

                if ("High Urgency".equalsIgnoreCase(urgency) || "High".equalsIgnoreCase(urgency)) {
                    tvUrgency.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background_red));
                } else if ("Moderate Urgency".equalsIgnoreCase(urgency) || "Medium".equalsIgnoreCase(urgency)) {
                    tvUrgency.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background_yellow));
                } else if ("Low Urgency".equalsIgnoreCase(urgency) || "Low".equalsIgnoreCase(urgency)) {
                    tvUrgency.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background_green));
                } else {
                    tvUrgency.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background));
                }
            } else {
                tvUrgency.setVisibility(View.GONE);
            }

            final boolean isSaved = request.getSavedByCsrId() != null && request.getSavedByCsrId().contains(currentUserId);

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
