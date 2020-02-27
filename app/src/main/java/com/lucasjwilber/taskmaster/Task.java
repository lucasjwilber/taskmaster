package com.lucasjwilber.taskmaster;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String team;
    private String title;
    private String body;
    private String state;
    private String imagePath;

    public Task(String title, String body, String team){
        this.title = title;
        this.body = body;
        this.team = team;
        this.state = "NEW";
    }

    public Task(String title, String body, String team, String imagePath){
        this.title = title;
        this.body = body;
        this.team = team;
        this.state = "NEW";
        this.imagePath = imagePath;
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

    public int getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
