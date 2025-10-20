package com.example.csit314sdm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

// ADAPTER: Binds User data to the views in item_user_card.xml
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged(); // Tell the RecyclerView to refresh
    }

    // Represents a single list item view
    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserEmail;
        private final TextView tvUserRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);

            // TODO: Set an OnClickListener on itemView to handle clicks on a user
        }

        public void bind(User user) {
            tvUserEmail.setText(user.getEmail());
            tvUserRole.setText(user.getUserType());
        }
    }
}
