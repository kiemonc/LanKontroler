package scheduleActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.main.R;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<ScheduleRow> implements View.OnClickListener{

    private ArrayList<ScheduleRow> scheduleSet;
    Context mContext;
    ArrayList<Integer> checkedRows;

    // View lookup cache
    private static class ViewHolder {
        TextView description;
        Switch switchButton;
        TextView date;
        TextView time;
        TextView heatingState;
    }

    public CustomAdapter(ArrayList<ScheduleRow> data, Context context) {
        super(context, R.layout.listview_row, data);
        this.scheduleSet = data;
        this.mContext=context;
        checkedRows = new ArrayList();



    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        ScheduleRow scheduleRow=(ScheduleRow)object;

        switch (v.getId())
        {
        }
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ScheduleRow scheduleRow = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview_row, parent, false);
            viewHolder.description = (TextView) convertView.findViewById(R.id.desrciption);
            viewHolder.date = (TextView) convertView.findViewById(R.id.dateValue);
            viewHolder.time = (TextView) convertView.findViewById(R.id.hourValue);
            viewHolder.heatingState = (TextView) convertView.findViewById(R.id.heatingState);
            viewHolder.switchButton = (Switch) convertView.findViewById(R.id.switchButton);



            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }


        //opis
        viewHolder.description.setText(scheduleRow.getDescription());

        //status grzanie, czyli zadanie harmonogramu
        if(scheduleRow.getHeatingState()) {
            viewHolder.heatingState.setText(mContext.getString(R.string.on));
            viewHolder.heatingState.setBackgroundColor(mContext.getColor(R.color.on));
        } else {
            viewHolder.heatingState.setText(mContext.getString(R.string.off));
            viewHolder.heatingState.setBackgroundColor(mContext.getColor(R.color.off));
        }

        //czas i data
        viewHolder.date.setText(scheduleRow.getDate());
        viewHolder.time.setText(scheduleRow.getTime());

        //animacja
        /*
        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;
        //viewHolder.switchButton.setTag(position);
         */

        //Switch button
        //dziwny zabieg, żeby switch buttony nie zmianialy stanu podczas scrollowania
        viewHolder.switchButton.setTag(position);
        viewHolder.switchButton.setOnCheckedChangeListener(null);

        if(scheduleRow.getState()) {
            if(!checkedRows.contains(position)) {
                checkedRows.add(position);
            }
        } else {
            if(checkedRows.contains(position)) {
                try {
                    checkedRows.remove(position);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }

        if (checkedRows.contains(position) ) {
            viewHolder.switchButton.setChecked(true);
        }
        else {
            viewHolder.switchButton.setChecked(false);
        }

        viewHolder.switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                scheduleRow.getSchedule().ifOn = isChecked;
                if (isChecked )
                {
                    checkedRows.add((Integer)buttonView.getTag() );
                }
                else
                {
                    checkedRows.remove((Integer)buttonView.getTag() );
                }
            }
        });


        // Return the completed view to render on screen
        return convertView;
    }

}