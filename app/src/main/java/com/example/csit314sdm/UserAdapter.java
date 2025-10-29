package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    private List<User> users = new ArrayList<>();
    private final OnItemClickListener listener;

    public UserAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // --- FIX #1: Use your existing layout file ---
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User currentUser = users.get(position);
        holder.bind(currentUser, listener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        // --- FIX #2: Declare TextViews that match your XML ---
        TextView tvUserEmail;
        TextView tvUserRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // --- FIX #3: Find the TextViews using their correct IDs ---
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvUserRole = itemView.findViewById(R.id.tv_user_role);
        }

        public void bind(final User user, final OnItemClickListener listener) {
            // --- FIX #4: Set the text using the correct methods from your User class ---
            tvUserEmail.setText(user.getEmail());
            tvUserRole.setText("Role: " + user.getRole()); // Example: "Role: Admin"

            itemView.setOnClickListener(v -> listener.onItemClick(user));
        }
    }
}
