package scheduleActivity;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dpro.widgets.OnWeekdaysChangeListener;
import com.dpro.widgets.WeekdaysPicker;
import com.example.main.R;
import com.example.main.UserDialog;
import com.ikovac.timepickerwithseconds.MyTimePickerDialog;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import lankontroller.Schedule;

public class ScheduleDialog extends Dialog implements View.OnClickListener {

    private String title;
    private String text;
    private Schedule schedule;
    private ScheduleActivity scheduleActivity;
    private DatePickerDialog datePickerDialog;

    //layout items
    private Button setTimeButton;
    private Button setDateButton;
    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;

    private EditText descriptionText;
    private ToggleButton ifOnButton;
    private ToggleButton ifOnHeatingButton;
    private ToggleButton ifWeekDaysButton;
    private WeekdaysPicker weekdaysPicker;
    private TextView timeText;
    private TextView dateText;
    private TableRow dateGroup;
    private LinearLayout weekdaysGroup;

    //schedule temp parameters
    private String description;
    private boolean ifOn;
    private boolean ifOnHeating;
    private LocalDate scheduleDate;
    private LocalTime scheduleTime;
    private List<Integer> weekdaysList;
    private boolean ifWeekdays;

    private MyDateFormat myDateFormat;



    public ScheduleDialog (Activity activity,Schedule schedule) {
        super(activity);
        this.schedule = schedule;
        myDateFormat = new MyDateFormat(schedule);
        scheduleActivity = (ScheduleActivity) activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.schedule_dialog);

        //Buttons
        saveButton = (Button) findViewById(R.id.saveButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        if(schedule == null) {
            deleteButton.setEnabled(false);
        }

        setTimeButton = (Button) findViewById(R.id.setTimeButton);
        setDateButton = (Button) findViewById(R.id.setDateButton);

        ifOnButton = (ToggleButton) findViewById(R.id.toggleButtonState);
        ifOnHeatingButton = (ToggleButton) findViewById(R.id.toggleButtonEvent);
        ifWeekDaysButton = (ToggleButton) findViewById(R.id.toggleButtonType);

        dateGroup = (TableRow) findViewById(R.id.dateGroup);
        weekdaysGroup = (LinearLayout) findViewById(R.id.weekdaysGroup);


        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        setTimeButton.setOnClickListener(this);
        setDateButton.setOnClickListener(this);
        ifOnButton.setOnClickListener(this);
        ifOnHeatingButton.setOnClickListener(this);
        ifWeekDaysButton.setOnClickListener(this);

        //TextViews
        descriptionText = (EditText) findViewById(R.id.desrciptionEditValue);
        timeText = (TextView) findViewById(R.id.timeValue);
        dateText = (TextView) findViewById(R.id.dateValue);



        //Weekday picker
        weekdaysPicker = (WeekdaysPicker) findViewById(R.id.weekdays);
        //ustawianie, żeby nie był wybrany żaden dzień
        weekdaysPicker.setSelectedDays(Arrays.asList());
        weekdaysPicker.setOnWeekdaysChangeListener(new OnWeekdaysChangeListener() {
            @Override
            public void onChange(View view, int clickedDayOfWeek, List<Integer> selectedDays) {
                weekdaysList = selectedDays;
            }
        });

        setValuesFromSchedule();
        updateLayoutItems();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveButton:
                saveButtonEvent();
                break;
            case R.id.cancelButton:
                dismiss();
                break;
            case R.id.deleteButton:
                scheduleActivity.removeSchedule(schedule);
                dismiss();
                break;
            case R.id.setTimeButton:
                setTime();
                break;
            case R.id.setDateButton:
                setDate();
                break;
            case R.id.toggleButtonState:
                ifOn = ifOnButton.isChecked();
                break;
            case R.id.toggleButtonEvent:
                ifOnHeating = ifOnHeatingButton.isChecked();
                break;
            case R.id.toggleButtonType:
                setType();

        }
    }


    private void setValuesFromSchedule() {
        MyDateFormat myDateFormat = new MyDateFormat(schedule);
        if(schedule != null) {
            //przypisanie wartości z schedule
            description = schedule.description;
            ifOn = schedule.ifOn;
            ifOnHeating = schedule.ifOnHeating;
            scheduleTime = myDateFormat.getTime();
            ifWeekdays = schedule.ifWeekdays;
            if(ifWeekdays) {
                weekdaysList = myDateFormat.getWeekdays();
            } else {
                scheduleDate = myDateFormat.getDate();
            }


        } else {
            //przypisanie wartości z schedule
            description = scheduleActivity.getResources().getString(R.string.descriptionValue);
            ifOn = true;
            ifOnHeating = true;
            scheduleTime = LocalTime.now();
            scheduleDate = LocalDate.now();
            ifWeekdays = false;
            weekdaysList = new ArrayList<>();

        }
    }


    /**
     * Aktualizuje stan wyświetlanych rzeczy w oknie dialogowym
     */
    private void updateLayoutItems() {
        descriptionText.setText(description);
        ifOnButton.setChecked(ifOn);
        ifOnHeatingButton.setChecked(ifOnHeating);
        timeText.setText(scheduleTime.toString());
        ifWeekDaysButton.setChecked(ifWeekdays);


        if(ifWeekdays) {
            dateGroup.setVisibility(View.INVISIBLE);

            weekdaysPicker.setSelectedDays(weekdaysList);

        } else {
            weekdaysGroup.setVisibility(View.INVISIBLE);

            //data
            dateText.setText(scheduleDate.toString());
            timeText.setText(scheduleTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString());
        }
    }

    /**
     * Okno ustawiania czasu
     */
    private void setTime() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        int second = mcurrentTime.get(Calendar.SECOND);

        //ładny picker obrotowy
                /*
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        timeText.setText( selectedHour + ":" + String.format("%02d",selectedMinute));
                        scheduleTime = LocalTime.of(selectedHour,selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();


                 */
        MyTimePickerDialog mTimePicker = new MyTimePickerDialog(getContext(), new MyTimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(com.ikovac.timepickerwithseconds.TimePicker view, int hourOfDay, int minute, int seconds) {
                timeText.setText(  String.format("%02d",hourOfDay) + ":" + String.format("%02d",minute) + ":" + String.format("%02d",second));
                scheduleTime = LocalTime.of(hourOfDay,minute,second);
            }
        }, scheduleTime.getHour(), scheduleTime.getMinute(), scheduleTime.getSecond(), true);
        mTimePicker.show();
    }

    /**
     * Okno ustawiania daty
     */
    private void setDate() {
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        // date picker dialog
        datePickerDialog = new DatePickerDialog(scheduleActivity,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dateText.setText(year + "." + String.format("%02d",monthOfYear + 1) + "." + String.format("%02d",dayOfMonth));
                        scheduleDate = LocalDate.of(year,monthOfYear+1,dayOfMonth);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Rekacja na przełączenie przycisku typu zdarzenia, czy harmongrama ma być w jakieś dni tygodnia czy w konkretnym dniu
     */
    private void setType() {
        ifWeekdays = ifWeekDaysButton.isChecked();
        if (ifWeekDaysButton.isChecked()) {
            weekdaysGroup.setVisibility(View.VISIBLE);
            dateGroup.setVisibility(View.INVISIBLE);
        } else {
            weekdaysGroup.setVisibility(View.INVISIBLE);
            dateGroup.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Akcje wykonywane po kliknięciu przycisku save
     */
    private void saveButtonEvent() {
        description = descriptionText.getText().toString();
        if(!description.equals("")  && scheduleTime != null  && ((ifWeekdays && (weekdaysList != null && weekdaysList.size() != 0)) || (!ifWeekdays && scheduleDate != null))) {
            if(schedule != null) {
                schedule.description = description;
                schedule.ifOn = ifOn;
                schedule.ifOnHeating = ifOnHeating;
                schedule.time = scheduleTime.toSecondOfDay();
                schedule.ifWeekdays = ifWeekdays;
                if (ifWeekdays) {
                    schedule.weekdays = MyDateFormat.weekdaysToInt(weekdaysList);
                    schedule.date = 0;
                } else {
                    schedule.weekdays = 0;
                    schedule.date = (int) scheduleDate.toEpochDay();
                }

                scheduleActivity.updateAdapter();
            } else {
                int date = 0;
                int weekdaysInt = 0;
                if(ifWeekdays) {
                    weekdaysInt = MyDateFormat.weekdaysToInt(weekdaysList);
                } else {
                    date = (int) scheduleDate.toEpochDay();
                }
                Schedule newSchedule = new Schedule(Schedule.getNumOfSchedules(),description,ifOn,ifOnHeating,scheduleTime.toSecondOfDay(),date,ifWeekdays,weekdaysInt);
                scheduleActivity.addSchedule(newSchedule);
            }
            dismiss();
        } else {
            UserDialog userDialog = new UserDialog(scheduleActivity.getResources().getString(R.string.information),scheduleActivity.getResources().getString(R.string.pick_all_data));
            userDialog.show(scheduleActivity.getSupportFragmentManager(),"invalid data");
        }


    }

}
