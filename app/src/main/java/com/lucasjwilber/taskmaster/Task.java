package com.lucasjwilber.taskmaster;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey
    @NonNull
    private String id;
    private String team;
    private String title;
    private String body;
    private String state;
    private String imagePath; //name of file in s3 bucket's public folder
    private String teamID;

    @Ignore
    public Task(String title, String body, String team, String teamID){
        this.title = title;
        this.body = body;
        this.teamID = teamID;
        this.team = team;
        this.state = "NEW";
    }

    public Task(String title, String body, String team, String teamID, String imagePath){
        this.title = title;
        this.body = body;
        this.teamID = teamID;
        this.team = team;
        this.state = "NEW";
        this.imagePath = imagePath; //name of file in s3 bucket's public folder
    }

    public String getTeamID() {
        return teamID;
    }

    public void setTeamID(String teamID) {
        this.teamID = teamID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
