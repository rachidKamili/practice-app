package med.kamili.rachid.practiceapp;

import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import med.kamili.rachid.practiceapp.managers.PermissionManager;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements PermissionManager.IPermissionManager{

    private IntentIntegrator qrScan;
    private PermissionManager permissionManager;
    private RefWatcher refWatcher;

    @BindView(R.id.QRResults)
    TextView tvQRResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Timber.tag("LifeCycles").d("Activity Created");

        permissionManager = new PermissionManager(this);

        //Initialize the Scan Object
        qrScan = new IntentIntegrator(this);

        refWatcher = MyApp.getRefWatcher(this);
    }

    @OnClick({ R.id.hello, R.id.hi })
    public void greetingClicked(Button button) {
        Timber.i("A button with ID %s was clicked to say '%s'.", button.getId(), button.getText());
        Toast.makeText(this, "Check logcat for a greeting!", Toast.LENGTH_SHORT).show();
    }

    @OnClick({ R.id.btnScanQRCode })
    public void scanQRCode(Button button) {
        permissionManager.checkPermission();
    }

    @OnClick({ R.id.tvLeak })
    public void leak(Button button) {
        new Thread () {
            @Override
            public void run() {
                while (true) {
                    SystemClock.sleep(1000);
                }
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.checkResult(requestCode, permissions, grantResults);
    }

    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                tvQRResults.setText("Result Not Found");
            } else {
                tvQRResults.setText("Results: " + result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPermissionResult(boolean isGranted) {
        if (isGranted) {
            //initiating the qr code scan
            qrScan.initiateScan();
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        refWatcher.watch(this);
    }
}
