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

import java.util.ArrayList;
import java.util.List;

public class HelpRequestAdapter extends RecyclerView.Adapter<HelpRequestAdapter.RequestViewHolder> {

    private List<HelpRequestEntity> requestList = new ArrayList<>();
    private final OnItemClickListener listener;
    private OnSaveClickListener saveClickListener;
    private String currentUserId;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(HelpRequestEntity request);
    }

    public interface OnSaveClickListener {
        void onSaveClick(HelpRequestEntity request, boolean isSaved);
    }

    public HelpRequestAdapter(OnItemClickListener listener, String currentUserId) {
        this.listener = listener;
        this.currentUserId = currentUserId;
    }

    public void setOnSaveClickListener(OnSaveClickListener listener) {
        this.saveClickListener = listener;
    }

    public void setRequests(List<HelpRequestEntity> requests) {
        this.requestList = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_help_request, parent, false);
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

        public void bind(final HelpRequestEntity request, final OnItemClickListener listener,
                         final OnSaveClickListener saveClickListener, String currentUserId, Context context) {

            tvCategory.setText(request.getCategory());
            tvOrg.setText(request.getOrganization());

            tvPinName.setVisibility(request.getPinName() != null ? View.VISIBLE : View.GONE);
            if(request.getPinName() != null) tvPinName.setText("PIN Name: " + request.getPinName());

            tvPinId.setVisibility(request.getPinShortId() != null ? View.VISIBLE : View.GONE);
            if(request.getPinShortId() != null) tvPinId.setText("PIN ID: " + request.getPinShortId());

            if(request.getCreationTimestamp() != null){
                long now = System.currentTimeMillis();
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(request.getCreationTimestamp().getTime(), now, DateUtils.DAY_IN_MILLIS);
                tvDate.setText("Posted " + relativeTime);
            }

            tvUrgency.setVisibility(request.getUrgencyLevel() != null ? View.VISIBLE : View.GONE);
            if(request.getUrgencyLevel() != null){
                tvUrgency.setText(request.getUrgencyLevel());
                switch(request.getUrgencyLevel().toLowerCase()){
                    case "high": tvUrgency.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background_red)); break;
                    case "medium": tvUrgency.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background_yellow)); break;
                    case "low": tvUrgency.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background_green)); break;
                    default: tvUrgency.setBackground(ContextCompat.getDrawable(context, R.drawable.chip_background));
                }
            }

            final boolean isSaved = request.getSavedByCsrId() != null && request.getSavedByCsrId().contains(currentUserId);
            btnSave.setImageResource(isSaved ? R.drawable.ic_star_filled : R.drawable.ic_star);

            btnViewDetails.setOnClickListener(v -> listener.onItemClick(request));

            if(saveClickListener != null){
                btnSave.setOnClickListener(v -> saveClickListener.onSaveClick(request, isSaved));
            }
        }
    }
}
