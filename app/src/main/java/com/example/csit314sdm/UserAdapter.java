// File: app/src/main/java/com/example/csit314sdm/UserAdapter.java
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


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public UserAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }


    public void setUsers(List<User> users) {
        this.userList = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_card, parent, false);
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
        private final TextView tvUserName, tvUserEmail;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
        }

        public void bind(final User user, final OnItemClickListener listener) {
            tvUserEmail.setText(user.getEmail());
            String fullName = user.getFullName();
            if (fullName == null || fullName.trim().isEmpty()) {
                tvUserName.setText("Name: Not available");
            } else {
                tvUserName.setText("Name: " + fullName);
            }
            itemView.setOnClickListener(v -> listener.onItemClick(user));
        }
    }
}
