package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

// ADAPTER: A simplified adapter that ONLY shows User Role, never the full name.
public class UserRoleAdapter extends RecyclerView.Adapter<UserRoleAdapter.UserRoleViewHolder> {

    private List<User> userList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public UserRoleAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserRoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the same item layout as before
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_card, parent, false);
        return new UserRoleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserRoleViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    static class UserRoleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserEmail;
        private final TextView tvUserRole;

        public UserRoleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
        }

        // *** THIS IS THE IMPORTANT PART ***
        // This bind method is simplified and will NEVER show the user's full name.
        public void bind(final User user, final OnItemClickListener listener) {
            tvUserEmail.setText(user.getEmail());
            // Always display the userType, regardless of whether a profile exists.
            tvUserRole.setText("Role: " + user.getUserType());

            // The item is still clickable to view details
            itemView.setOnClickListener(v -> listener.onItemClick(user));
        }
    }
}
