package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csit314sdm.entity.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category currentCategory = categories.get(position);
        holder.bind(currentCategory, listener, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = new ArrayList<>(newCategories);
        selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        TextView tvCategoryDescription;

        CategoryViewHolder(View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(android.R.id.text1);
            tvCategoryDescription = itemView.findViewById(android.R.id.text2);
        }

        public void bind(final Category category, final OnCategoryClickListener listener, boolean isSelected) {
            tvCategoryName.setText(category.getName());
            tvCategoryDescription.setText(category.getDescription());
            itemView.setActivated(isSelected);

            itemView.setOnClickListener(v -> {
                if (getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;

                notifyItemChanged(selectedPosition);
                selectedPosition = getBindingAdapterPosition();
                notifyItemChanged(selectedPosition);

                listener.onCategoryClick(category);
            });
        }
    }
}
