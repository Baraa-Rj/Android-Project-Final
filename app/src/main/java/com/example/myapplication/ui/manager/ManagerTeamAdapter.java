package com.example.myapplication.ui.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.models.Team;
import java.util.ArrayList;
import java.util.List;

public class ManagerTeamAdapter extends RecyclerView.Adapter<ManagerTeamAdapter.TeamViewHolder> {

    private List<Team> teams = new ArrayList<>();
    private OnTeamClickListener listener;

    public interface OnTeamClickListener {
        void onTeamClick(Team team);
    }

    public ManagerTeamAdapter(OnTeamClickListener listener) {
        this.listener = listener;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teams.get(position);
        holder.bind(team);
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    class TeamViewHolder extends RecyclerView.ViewHolder {
        private final TextView teamName;
        private final TextView teamDescription;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            teamName = itemView.findViewById(R.id.teamName);
            teamDescription = itemView.findViewById(R.id.teamDescription);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTeamClick(teams.get(position));
                }
            });
        }

        public void bind(Team team) {
            teamName.setText(team.getName());
            String description = team.getDescription() != null && !team.getDescription().isEmpty()
                    ? team.getDescription()
                    : "No description";
            teamDescription.setText(description);
        }
    }
}
