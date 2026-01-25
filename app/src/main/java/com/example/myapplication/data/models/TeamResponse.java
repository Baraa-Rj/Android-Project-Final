package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TeamResponse {
    @SerializedName("team")
    private Team team;

    @SerializedName("members")
    private List<TeamMember> members;

    // Getters
    public Team getTeam() { return team; }
    public List<TeamMember> getMembers() { return members; }

    // Setters
    public void setTeam(Team team) { this.team = team; }
    public void setMembers(List<TeamMember> members) { this.members = members; }
}
