package com.lucasjwilber.taskmaster;

public class TaskmasterUser {
    private String id;
    private String username;
    private String team;

    public TaskmasterUser(String id, String username, String team) {
        this.id = id;
        this.username = username;
        this.team = team;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }
}
