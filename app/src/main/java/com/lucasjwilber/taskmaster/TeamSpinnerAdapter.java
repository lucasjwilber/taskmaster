//big thanks to https://stackoverflow.com/questions/1625249/android-how-to-bind-spinner-to-custom-object-list

package com.lucasjwilber.taskmaster;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import java.util.List;

public class TeamSpinnerAdapter extends ArrayAdapter<Team> {

    public Context context;
    public Team[] teams;

    TeamSpinnerAdapter(Context context, int textViewResourceId,
                       Team[] teams) {
        super(context, textViewResourceId);
        this.context = context;
        this.teams = teams;
    }

    @Override
    public int getCount(){
        return teams.length;
    }

    @Override
    public long getItemId(int position){
        return position;
    }


    // And the "magic" goes here
    // This is for the "passive" state of the spinner

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = (TextView) super.getView(position, convertView, parent);
        // Then you can get the current item using the teams array (Teams array) and the current position
        // You can NOW reference each method you has created in your bean object (Team class)
        label.setText(teams[position].getName());

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setText(teams[position].getName());

        return label;
    }
}
