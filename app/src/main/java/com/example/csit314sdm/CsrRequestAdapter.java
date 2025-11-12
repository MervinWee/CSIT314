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

    private List<HelpRequestEntity> requests;
    private OnItemClickListener listener;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(HelpRequestEntity request);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CsrRequestAdapter(List<HelpRequestEntity> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_csr, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        HelpRequestEntity request = requests.get(position);
        holder.bind(request, listener);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }


    public void setRequests(List<HelpRequestEntity> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        private TextView tvRequestTitle;
        private TextView tvRequestDescription;
        private TextView tvRequestLocation;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRequestTitle = itemView.findViewById(R.id.tvRequestTitle);
            tvRequestDescription = itemView.findViewById(R.id.tvRequestDescription);
            tvRequestLocation = itemView.findViewById(R.id.tvRequestLocation);
        }

        public void bind(final HelpRequestEntity request, final OnItemClickListener listener) {
            tvRequestTitle.setText(request.getTitle());
            tvRequestDescription.setText(request.getDescription());
            tvRequestLocation.setText(request.getLocation());


            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(request);
                }
            });
        }
    }
}
