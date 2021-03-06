package settingsActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.main.R;


/**
 * Okno dialogowe z ustawieniami. Konteneryzuje "fragment ustawień"
 */
public class SettingsActivity extends AppCompatActivity {

    private ApplicationConfig config;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //wyłączenie rotacji ekranu
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Pobiera konfiguracje z MainActivity
        Intent thisIntent = getIntent();
        config = (ApplicationConfig) thisIntent.getSerializableExtra("config");

        //włącza przycisk cofania
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new MySettingsFragment(config))
                .commit();
    }


    @Override
    public void finish() {
        mySetResult();
        super.finish();
    }

    /**
     * Ustawia resultat okna ustwień.
     * Przekazuje referencje do objektu ustawień
     */
    private void mySetResult() {
        int resultCode = 1;
        Intent resultIntent = new Intent();
        resultIntent.putExtra("config", config);
        setResult(resultCode, resultIntent);
    }


    /**
     * Kliknięcie przycisku
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //Klikniecie przycisku powrotu na górze
        if (id==android.R.id.home) {
            mySetResult();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}