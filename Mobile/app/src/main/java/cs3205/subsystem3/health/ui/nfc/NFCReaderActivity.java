package cs3205.subsystem3.health.ui.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.logger.Log;
import cs3205.subsystem3.health.logic.nfc.NFCReader;


public class NFCReaderActivity extends AppCompatActivity {

    private TextView mNFCInstruction;

    //private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private String username;
    private NFCReader nfcReader;
    private String[] credentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcreader);

        Intent intentStartNFCReading = getIntent();
        if (intentStartNFCReading.hasExtra(Intent.EXTRA_TEXT)) {
            username = intentStartNFCReading.getStringExtra("username");
        }

        mNFCInstruction = (TextView) findViewById(R.id.nfc_instruction);
        mNFCInstruction.setText("Please scan your NFC tag now.");

        checkNFCStatus();
    }

    private void checkNFCStatus() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter!= null && nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC available!", Toast.LENGTH_LONG).show();
            nfcReader = new NFCReader();
            nfcReader.setAdapter(nfcAdapter);
            mPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        } else {
            Toast.makeText(this, "NFC not available :(", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        mNFCInstruction.setVisibility(TextView.INVISIBLE);
        if (nfcReader.dispatchTagByType(action, intent)) {
            Toast.makeText(this, "onResume() - NDEF_DISCOVERED", Toast.LENGTH_SHORT).show();
            credentials = nfcReader.readCredentials();
            Log.d("username", credentials[0]);
            Log.d("password", credentials[1]);
        } else {
            Toast.makeText(this, "onResume() - NO_TAG_DISCOVERED", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcReader.getAdapter() != null) {
            nfcReader.getAdapter().enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }


}
