package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SimpleRequestAdapter extends RecyclerView.Adapter<SimpleRequestAdapter.ViewHolder> {

    private List<HelpRequest> requests;

    public SimpleRequestAdapter(List<HelpRequest> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_request_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelpRequest request = requests.get(position);
        holder.tvTitle.setText(request.getTitle());
        holder.tvCategory.setText(request.getCategory());
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void setRequests(List<HelpRequest> requests) {
        this.requests = requests;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public TextView tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRequestTitle);
            tvCategory = itemView.findViewById(R.id.tvRequestCategory);
        }
    }
}
