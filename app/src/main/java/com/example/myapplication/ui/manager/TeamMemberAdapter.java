package com.example.myapplication.ui.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.models.User;
import java.util.ArrayList;
import java.util.List;

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.MemberViewHolder> {

    private List<User> members = new ArrayList<>();
    private OnMemberActionListener listener;

    public interface OnMemberActionListener {
        void onRemoveMember(User user);
    }

    public TeamMemberAdapter(OnMemberActionListener listener) {
        this.listener = listener;
    }

    public void setMembers(List<User> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User member = members.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private final TextView memberName;
        private final TextView memberEmail;
        private final TextView memberRole;
        private final ImageButton removeMemberButton;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.memberName);
            memberEmail = itemView.findViewById(R.id.memberEmail);
            memberRole = itemView.findViewById(R.id.memberRole);
            removeMemberButton = itemView.findViewById(R.id.removeMemberButton);
        }

        public void bind(User user) {
            memberName.setText(user.getName());
            memberEmail.setText(user.getEmail());

            String roleText = user.getRole() != null ? user.getRole().toString() : "Employee";
            memberRole.setText(roleText);

            removeMemberButton.setVisibility(View.VISIBLE);
            removeMemberButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveMember(user);
                }
            });
        }
    }
}
