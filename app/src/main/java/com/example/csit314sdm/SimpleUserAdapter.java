// File: app/src/main/java/com/example/csit314sdm/SimpleUserAdapter.java
package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csit314sdm.entity.User;

import java.util.ArrayList;
import java.util.List;

public class SimpleUserAdapter extends RecyclerView.Adapter<SimpleUserAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public SimpleUserAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the new simple layout file
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_simple, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(userList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserEmail;
        private final TextView tvUserRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the views from the new simple layout
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
        }

        public void bind(final User user, final OnItemClickListener listener) {
            tvUserEmail.setText(user.getEmail());
            tvUserRole.setText("Role: " + user.getRole());

            itemView.setOnClickListener(v -> listener.onItemClick(user));
        }
    }
}
