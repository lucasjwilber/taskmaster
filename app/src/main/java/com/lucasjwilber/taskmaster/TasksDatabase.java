package com.lucasjwilber.taskmaster;

import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {Task.class}, version = 1)
public abstract class TasksDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}
