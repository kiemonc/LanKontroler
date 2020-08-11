package scheduleActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.main.UserDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.main.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import lankontroller.LanKontroller;
import lankontroller.Schedule;

public class ScheduleActivity extends AppCompatActivity {


    private ListView listView;
    private LanKontroller lk;
    private List<Schedule> schedulesList;
    private CustomAdapter adapter;
    private ArrayList<ScheduleRow> scheduleRows;

    private TextView connectionLost;

    /**
     * Zmienna do czekania aż nie zosaną pobrane informacje o harmongramach;
     */
    private boolean schedulesValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        //status połączenai
        connectionLost = (TextView) findViewById(R.id.connectionStateInScheduleActivity);


        //wyłączenie rotacji ekranu
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //pobranie harmonogramów i czekanie na rezultat
        /*
        try {
            new GettingSchedulesTask().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         */


        //tolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //włacza przycisk cofania
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        new GettingSchedulesTask(this).execute();






    }


    /**
     * Pobieranie harmonogramów z LK3
     */
    private class GettingSchedulesTask extends AsyncTask {


        private ProgressDialog progressDialog;

        public GettingSchedulesTask(Activity activity) {
            progressDialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            schedulesValid = false;
            progressDialog.setMessage(getString(R.string.downloading));
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                schedulesList = Schedule.getSchedules();
                schedulesValid = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            //tolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            //listView
            listView = (ListView) findViewById(R.id.listView1);


            scheduleRows = new ArrayList<ScheduleRow>();

            if(schedulesList == null) {
                schedulesList = new ArrayList<>();
            }

            makeScheduleRowsList();


            adapter = new CustomAdapter(scheduleRows, getApplicationContext());

            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    ScheduleRow scheduleRow = scheduleRows.get(position);

                    ScheduleDialog scheduleDialog = new ScheduleDialog(ScheduleActivity.this,scheduleRow.getSchedule());
                    scheduleDialog.show();
                }
            });

            //przycisk dodawania harmonogramu
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setImageResource(R.drawable.ic_add);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(Schedule.getNumOfSchedules() < 50) {
                        new ScheduleDialog(ScheduleActivity.this, null).show();
                    } else {
                        UserDialog userDialog = new UserDialog(getString(R.string.information),getString(R.string.max_schedules));
                        userDialog.show(getSupportFragmentManager(),"max schedules");
                    }
                }
            });

            if(!schedulesValid) {
                connectionLost.setVisibility(View.VISIBLE);
            } else {
                connectionLost.setVisibility(View.INVISIBLE);
            }

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

        }
    }


    private class SendingSchedules extends AsyncTask {

        private boolean sended = false;
        private ProgressDialog progressDialog;

        public SendingSchedules(Activity activity) {
            progressDialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(getString(R.string.sending));
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Schedule.send(schedulesList);
                sended = true;
            } catch (IOException e) {

                UserDialog userDialog = new UserDialog(getString(R.string.information),getString(R.string.saving_failed));
                userDialog.show(getSupportFragmentManager(),"saving failed");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if(sended) {
                UserDialog userDialog = new UserDialog(getString(R.string.information), getString(R.string.schedules_saved));
                userDialog.show(getSupportFragmentManager(), "saving succesfull");
            }
        }
    }

    /**
     * Dodawanie przycisków na toolbarze
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schedule_menu,menu);
        return true;
    }

    /**
     * Obsługa przycisku zapisu Wysyła harmonogramy do LK3 oraz przycisku cofania
     * @param item przycisk zapisywania
     * @return nie wiem
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveItem:
                if(schedulesValid)
                new SendingSchedules(this).execute();
                return true;
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Dodaje nowy schedule do listy. Aktulaizuje adapter, czyli dynamiczną listę harmonogramów
     * @param schedule harmonogram do dodania
     */
    public void addSchedule(Schedule schedule) {
        schedulesList.add(0,schedule);
        scheduleRows.add(0,new ScheduleRow(schedule));
        adapter.notifyDataSetChanged();
        Schedule.setNumOfSchedules(Schedule.getNumOfSchedules()+1);
    }


    /**
     * Usuwa harmonogram z listy harmnogramów i aktualizuje adapter
     * @param schedule harmnogram do usunięcia
     */
    public void removeSchedule(Schedule schedule) {
        schedulesList.remove(schedule);
        makeScheduleRowsList();
        adapter.notifyDataSetChanged();
        Schedule.setNumOfSchedules(Schedule.getNumOfSchedules()-1);
    }


    /**
     * Aktualizuje listę harmonogramów, jeśli jakiś harmnogram się zmienił to należy użyć tej metody
     */
    public void updateAdapter() {
        adapter.notifyDataSetChanged();
    }

    /**
     * Aktualizuje liste rzędów na podstawie listy harmonogramów
     */
    private void makeScheduleRowsList() {
        scheduleRows.clear();
        for(Schedule schedule : schedulesList) {
            if(!(schedule.description.equals(LanKontroller.TEMP_SCHEDULE_DESRCIPTION)))
            scheduleRows.add(new ScheduleRow(schedule));
        }
    }
}