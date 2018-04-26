package med.kamili.rachid.practiceapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Timber.tag("LifeCycles").d("Activity Created");

    }

    @OnClick({ R.id.hello, R.id.hi })
    public void greetingClicked(Button button) {
        Timber.i("A button with ID %s was clicked to say '%s'.", button.getId(), button.getText());
        Toast.makeText(this, "Check logcat for a greeting!", Toast.LENGTH_SHORT).show();
    }

}
