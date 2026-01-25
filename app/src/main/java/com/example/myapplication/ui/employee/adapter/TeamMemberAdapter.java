package com.example.myapplication.ui.employee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.models.TeamMember;
import java.util.ArrayList;
import java.util.List;

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.TeamMemberViewHolder> {

    private List<TeamMember> members = new ArrayList<>();

    public void setMembers(List<TeamMember> members) {
        this.members = members != null ? members : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeamMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_member, parent, false);
        return new TeamMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamMemberViewHolder holder, int position) {
        TeamMember member = members.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class TeamMemberViewHolder extends RecyclerView.ViewHolder {
        private final TextView memberName;
        private final TextView memberEmail;
        private final TextView memberRole;

        public TeamMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.memberName);
            memberEmail = itemView.findViewById(R.id.memberEmail);
            memberRole = itemView.findViewById(R.id.memberRole);
        }

        public void bind(TeamMember member) {
            memberName.setText(member.getName());
            memberEmail.setText(member.getEmail());
            memberRole.setText(capitalizeFirst(member.getRole()));
        }

        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty()) return "";
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }
    }
}
