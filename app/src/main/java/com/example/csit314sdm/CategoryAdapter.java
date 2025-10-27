package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories = new ArrayList<>();

    // --- ADD THIS LISTENER ---
    private OnEditClickListener listener;

    // --- ADD THIS INTERFACE ---
    public interface OnEditClickListener {
        void onEditClick(Category category);
    }

    // --- ADD THIS CONSTRUCTOR ---
    public CategoryAdapter(OnEditClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category currentCategory = categories.get(position);
        holder.tvCategoryName.setText(currentCategory.getName());
        holder.tvCategoryDescription.setText(currentCategory.getDescription());

        holder.btnEditCategory.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(currentCategory);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategoryName;
        private final TextView tvCategoryDescription;
        // --- ADD THIS BUTTON ---
        private final ImageButton btnEditCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryDescription = itemView.findViewById(R.id.tvCategoryDescription);
            // --- INITIALIZE THE BUTTON ---
            btnEditCategory = itemView.findViewById(R.id.btnEditCategory);
        }
    }
}
