package cs3205.subsystem3.health.logic.nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;

import java.io.UnsupportedEncodingException;

/**
 * Created by danwen on 4/10/17.
 */

public class NFCReader {
    private NfcAdapter nfcAdapter;
    private String[] credentials;

    public void setAdapter(NfcAdapter adpter) {
        this.nfcAdapter = adpter;
    }

    public NfcAdapter getAdapter() {
        return this.nfcAdapter;
    }

    public String[] readCredentials() {
        return this.credentials;
    }


    public boolean dispatchTagByType(String action, Intent intent) {
        if (nfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
//            Toast.makeText(this,
//                    "onResume() - NDEF_DISCOVERED",
//                    Toast.LENGTH_SHORT).show();

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (tag == null) {

            } else {
                this.credentials = new String[2];
                readTagCredentials(rawMsgs);
            }
        } else if (nfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {//only for compatibility
        } else {
            return false;
        }

        return true;
    }

    private void readTagCredentials(Parcelable[] rawMsgs) {
        NdefMessage ndefMessage = (NdefMessage) rawMsgs[0];
        NdefRecord ndefRecord1 = ndefMessage.getRecords()[0];
        NdefRecord ndefRecord2 = ndefMessage.getRecords()[1];
        String username = parseRecord(ndefRecord1);
        String password = parseRecord(ndefRecord2);
        this.credentials[0] = username;
        this.credentials[1] = password;
    }


    private String parseRecord(NdefRecord record) {
        byte[] payload = record.getPayload();
        String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0077;
        String text = "";
        try {
            text = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }

}
