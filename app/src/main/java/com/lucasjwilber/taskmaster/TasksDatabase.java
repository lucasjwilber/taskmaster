package com.lucasjwilber.taskmaster;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {Task.class}, version = 1, exportSchema = false)
public abstract class TasksDatabase extends RoomDatabase {
    public abstract TaskDao userDao();

    //thanks to https://medium.com/@ajaysaini.official/building-database-with-room-persistence-library-ecf7d0b8f3e9
    private static TasksDatabase INSTANCE;
    public static TasksDatabase getTasksDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), TasksDatabase.class, "tasksDb")
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
