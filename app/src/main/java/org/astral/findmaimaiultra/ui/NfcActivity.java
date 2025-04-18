package org.astral.findmaimaiultra.ui;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;
import org.astral.findmaimaiultra.R;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class NfcActivity extends Activity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        textView = findViewById(R.id.textView);

        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                // Process the messages
                processNdefMessages(msgs);
            }
        }
    }

    private void processNdefMessages(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        for (NdefMessage msg : msgs) {
            NdefRecord[] records = msg.getRecords();
            for (NdefRecord record : records) {
                if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_URI)) {
                    String uri = parseUri(record);
                    textView.setText(uri);
                }
            }
        }
    }

    private String parseUri(NdefRecord record) {
        byte[] uriField = record.getPayload();
        String prefix = ((char) (uriField[0] & 0x0F)) + "";
        byte[] fullUri = new byte[uriField.length - 1];
        System.arraycopy(uriField, 1, fullUri, 0, uriField.length - 1);
        return prefix + new String(fullUri, StandardCharsets.UTF_8);
    }
}
   