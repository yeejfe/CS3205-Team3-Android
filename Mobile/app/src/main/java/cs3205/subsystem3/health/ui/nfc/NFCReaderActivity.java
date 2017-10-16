package cs3205.subsystem3.health.ui.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.miscellaneous.AppMessage;
import cs3205.subsystem3.health.common.miscellaneous.Value;
import cs3205.subsystem3.health.common.utilities.SessionManager;
import cs3205.subsystem3.health.logic.nfc.NFCReader;

public class NFCReaderActivity extends AppCompatActivity {

    private TextView mNFCInstruction;
    private PendingIntent mPendingIntent;
    private NFCReader nfcReader;
    private String[] credentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcreader);

        mNFCInstruction = (TextView) findViewById(R.id.nfc_instruction);
        mNFCInstruction.setText(AppMessage.MESSAGE_SCAN_NFC_TAG);

        checkNFCStatus();
    }

    private void checkNFCStatus() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter!= null && nfcAdapter.isEnabled()) {
            Toast.makeText(this, AppMessage.TOAST_MESSAGE_NFC_AVAILABLE, Toast.LENGTH_LONG).show();
            nfcReader = new NFCReader();
            nfcReader.setAdapter(nfcAdapter);
            mPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        } else {
            Toast.makeText(this, AppMessage.TOAST_MESSAGE_NFC_UNAVAILABLE, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        mNFCInstruction.setVisibility(TextView.INVISIBLE);
        switch (nfcReader.dispatchTagByType(action, intent)) {
            case TAG_TYPE_VALID:
                credentials = nfcReader.readCredentials();
                Intent returnToLoginIntent = new Intent();
                returnToLoginIntent.putExtra(Value.KEY_VALUE_LOGIN_INTENT_USERNAME, credentials[0]);
                returnToLoginIntent.putExtra(Value.KEY_VALUE_LOGIN_INTENT_PASSWORD, credentials[1]);
                setResult(RESULT_OK, returnToLoginIntent);
                finish();
                break;
            case TAG_ABSENT:
                Toast.makeText(this, AppMessage.TOAST_MESSAGE_NO_NFC_TAG_DISCOVERED, Toast.LENGTH_SHORT).show();
                break;
            case TAG_INCOMPATIBLE_TYPE:
                Toast.makeText(this, AppMessage.TOAST_MESSAGE_NFC_TAG_INCOMPATIBLE, Toast.LENGTH_SHORT).show();
                break;
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
        if (SessionManager.isTimerSet()) {
            SessionManager.cancelTimer();
        } else {
            SessionManager.setTimer(this);
        }
        if (nfcReader.getAdapter() != null) {
            nfcReader.getAdapter().enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SessionManager.isTimerSet()) {
            SessionManager.resetTimer(this);
        } else {
            SessionManager.setTimer(this);
        }
    }
}
