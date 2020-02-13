package com.lucasjwilber.taskmaster;

import androidx.recyclerview.widget.RecyclerView;

//public class Task extends RecyclerView.ViewHolder {
public class Task {
    public String title;
    public String body;
    public String state = "new";

    public Task(String title, String body){
        this.title = title;
        this.body = body;
    }
}
