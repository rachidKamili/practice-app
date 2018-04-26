package med.kamili.rachid.practiceapp;

import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import med.kamili.rachid.practiceapp.managers.PermissionManager;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements PermissionManager.IPermissionManager,GoogleApiClient.OnConnectionFailedListener{

    private IntentIntegrator qrScan;
    private PermissionManager permissionManager;
    private RefWatcher refWatcher;

    @BindView(R.id.QRResults)
    TextView tvQRResults;

    //Android Pay
    private SupportWalletFragment mWalletFragment;
    public static final int MASKED_WALLET_REQUEST_CODE = 888;
    public static final String WALLET_FRAGMENT_ID = "wallet_fragment";
    private MaskedWallet mMaskedWallet;
    private GoogleApiClient mGoogleApiClient;
    public static final int FULL_WALLET_REQUEST_CODE = 889;
    private FullWallet mFullWallet;


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

        // Check if WalletFragment exists
        mWalletFragment = (SupportWalletFragment) getSupportFragmentManager()
                .findFragmentByTag(WALLET_FRAGMENT_ID);

        if (mWalletFragment == null) {
            // Wallet fragment style
            WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                    .setBuyButtonText(WalletFragmentStyle.BuyButtonText.BUY_WITH)
                    .setBuyButtonWidth(WalletFragmentStyle.Dimension.MATCH_PARENT);

            // Wallet fragment options
            WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                    .setFragmentStyle(walletFragmentStyle)
                    .setTheme(WalletConstants.THEME_LIGHT)
                    .setMode(WalletFragmentMode.BUY_BUTTON)
                    .build();

            // Initialize the WalletFragment
            WalletFragmentInitParams.Builder startParamsBuilder =
                    WalletFragmentInitParams.newBuilder()
                            .setMaskedWalletRequest(generateMaskedWalletRequest())
                            .setMaskedWalletRequestCode(MASKED_WALLET_REQUEST_CODE)
                            .setAccountName("Google I/O Codelab");
            mWalletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);
            mWalletFragment.initialize(startParamsBuilder.build());

            // Add the WalletFragment to the UI
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.wallet_button_holder, mWalletFragment, WALLET_FRAGMENT_ID)
                    .commit();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, 0, this)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                        .setTheme(WalletConstants.THEME_LIGHT)
                        .build())
                .build();
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
            switch (requestCode) {
                case MASKED_WALLET_REQUEST_CODE:
                    switch (resultCode) {
                        case RESULT_OK:
                            mMaskedWallet =  data
                                    .getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                            Toast.makeText(this, "Got Masked Wallet", Toast.LENGTH_SHORT).show();
                            break;
                        case RESULT_CANCELED:
                            // The user canceled the operation
                            break;
                        case WalletConstants.RESULT_ERROR:
                            Toast.makeText(this, "An Error Occurred", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case FULL_WALLET_REQUEST_CODE:
                    switch (resultCode) {
                        case RESULT_OK:
                            mFullWallet = data
                                    .getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
                            // Show the credit card number
                            Toast.makeText(this,
                                    "Got Full Wallet, Done!",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case WalletConstants.RESULT_ERROR:
                            Toast.makeText(this, "An Error Occurred", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;

            }

        }
    }

    @Override
    public void onPermissionResult(boolean isGranted) {
        if (isGranted) {
            //initiating the qr code scan
            qrScan.initiateScan();
        }
    }

    private MaskedWalletRequest generateMaskedWalletRequest(){
        // We will implement this in the next step
        // This is just an example publicKey for the purpose of this codelab.
        // To learn how to generate your own visit:
        // https://github.com/android-pay/androidpay-quickstart
        String publicKey = "BO39Rh43UGXMQy5PAWWe7UGWd2a9YRjNLPEEVe+zWIbdIgALcDcnYCuHbmrrzl7h8FZjl6RCzoi5/cDrqXNRVSo=";
        PaymentMethodTokenizationParameters parameters =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(
                                PaymentMethodTokenizationType.NETWORK_TOKEN)
                        .addParameter("publicKey", publicKey)
                        .build();

        MaskedWalletRequest maskedWalletRequest =
                MaskedWalletRequest.newBuilder()
                        .setMerchantName("Google I/O Codelab")
                        .setPhoneNumberRequired(true)
                        .setShippingAddressRequired(true)
                        .setCurrencyCode("USD")
                        .setCart(Cart.newBuilder()
                                .setCurrencyCode("USD")
                                .setTotalPrice("1.00")
                                .addLineItem(LineItem.newBuilder()
                                        .setCurrencyCode("USD")
                                        .setDescription("Google I/O Sticker")
                                        .setQuantity("1")
                                        .setUnitPrice("1.00")
                                        .setTotalPrice("1.00")
                                        .build())
                                .build())
                        .setEstimatedTotalPrice("1.10")
                        .setPaymentMethodTokenizationParameters(parameters)
                        .build();
        return maskedWalletRequest;
    }

    private FullWalletRequest generateFullWalletRequest(String googleTransactionId) {
        FullWalletRequest fullWalletRequest = FullWalletRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setCart(Cart.newBuilder()
                        .setCurrencyCode("USD")
                        .setTotalPrice("1.10")
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode("USD")
                                .setDescription("Google I/O Sticker")
                                .setQuantity("1")
                                .setUnitPrice("1.00")
                                .setTotalPrice("1.00")
                                .build())
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode("USD")
                                .setDescription("Tax")
                                .setRole(LineItem.Role.TAX)
                                .setTotalPrice(".10")
                                .build())
                        .build())
                .build();
        return fullWalletRequest;
    }


    public void requestFullWallet(View view) {
        if (mMaskedWallet == null) {
            Toast.makeText(this, "No masked wallet, can't confirm", Toast.LENGTH_SHORT).show();
            return;
        }
        Wallet.Payments.loadFullWallet(mGoogleApiClient,
                generateFullWalletRequest(mMaskedWallet.getGoogleTransactionId()),
                FULL_WALLET_REQUEST_CODE);

    }

    @Override public void onDestroy() {
        super.onDestroy();
        refWatcher.watch(this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // GoogleApiClient failed to connect, we should log the error and retry
        Timber.tag("MainActivity").d("GoogleApiClient failed to connect, we should log the error and retry");
    }

    public void check(View view) {
        if (mFullWallet!=null){
            System.out.println(123);
        }
    }
}
