package cs3205.subsystem3.health.ui.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import cs3205.subsystem3.health.R;


public class NFCReader extends AppCompatActivity {

    private TextView mNFCInstruction;

    private NfcAdapter nfcAdapter;
    private TextView mTagInfo;
    PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcreader);

        Intent intentStartNFCReading = getIntent();
        if (intentStartNFCReading.hasExtra(Intent.EXTRA_TEXT)) {
        }

        mNFCInstruction = (TextView) findViewById(R.id.nfc_instruction);
        mNFCInstruction.setText("Please scan your NFC tag to complete login.");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter!= null && nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC available!", Toast.LENGTH_LONG).show();
            mTagInfo = (TextView) findViewById(R.id.info);
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
        System.out.println(action);

        if (nfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Toast.makeText(this,
                    "onResume() - NDEF_DISCOVERED",
                    Toast.LENGTH_SHORT).show();

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (tag == null) {
                mTagInfo.setText("tag == null");
            } else {
                String tagInfo = tag.toString() + "\n";
                tagInfo = readTagInfo(tag, tagInfo);
                tagInfo = readTagCredentials(rawMsgs, tagInfo);
                mTagInfo.setText(tagInfo);
            }
        } else if (nfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Toast.makeText(this,
                    "onResume() - TAG_DISCOVERED",
                    Toast.LENGTH_SHORT).show();

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if (tag == null) {
                mTagInfo.setText("tag == null");
            } else {

                String tagInfo = tag.toString() + "\n";
                tagInfo = readTagInfo(tag, tagInfo);
                mTagInfo.setText(tagInfo);
            }
        }   else {
            Toast.makeText(this,
                    "onResume() : " + action,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String readTagInfo(Tag tag, String tagInfo) {
        tagInfo += "\nTag Id: \n";
        byte[] tagId = tag.getId();
        tagInfo += "length = " + tagId.length +"\n";
        for(int i=0; i<tagId.length; i++){
            tagInfo += Integer.toHexString(tagId[i] & 0xFF) + " ";
        }
        tagInfo += "\n";

        String[] techList = tag.getTechList();
        tagInfo += "\nTech List\n";
        tagInfo += "length = " + techList.length +"\n";
        for(int i=0; i<techList.length; i++) {
            tagInfo += techList[i] + "\n ";
        }
        return tagInfo;
    }

    private String readTagCredentials(Parcelable[] rawMsgs, String tagInfo) {
        NdefMessage ndefMessage = (NdefMessage) rawMsgs[0];
        NdefRecord ndefRecord = ndefMessage.getRecords()[0];
        String msg = parseRecord(ndefRecord);
        tagInfo += "\nTag Username\n";
        tagInfo += "length = " + msg.length() +"\n";
        tagInfo += msg;
        return tagInfo;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
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
