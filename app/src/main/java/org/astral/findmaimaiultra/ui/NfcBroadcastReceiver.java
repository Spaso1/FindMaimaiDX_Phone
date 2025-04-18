package org.astral.findmaimaiultra.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class NfcBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                // Process the messages
                processNdefMessages(context, msgs);
            }
        }
    }

    private void processNdefMessages(Context context, NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        for (NdefMessage msg : msgs) {
            NdefRecord[] records = msg.getRecords();
            for (NdefRecord record : records) {
                if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_URI)) {
                    String uri = parseUri(record);
                    Log.d("NfcBroadcastReceiver", "URI: " + uri);
                    // 你可以在这里启动MainActivity或者执行其他操作
                    Intent mainIntent = new Intent(context, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(mainIntent);
                } else if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                    String text = parseText(record);
                    Log.d("NfcBroadcastReceiver", "Text: " + text);
                    // 你可以在这里启动MainActivity或者执行其他操作
                    Intent mainIntent = new Intent(context, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mainIntent.putExtra("nfc_text", text);
                    context.startActivity(mainIntent);
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

    private String parseText(NdefRecord record) {
        byte[] payload = record.getPayload();
        if (payload.length < 2) {
            Log.w("NfcBroadcastReceiver", "Invalid payload length for text record");
            return null;
        }

        // 第一个字节的高4位表示字符编码（0表示UTF-8，1表示UTF-16）
        String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0077;

        if (languageCodeLength > payload.length - 1) {
            Log.w("NfcBroadcastReceiver", "Invalid language code length");
            return null;
        }

        // 解析语言代码（通常不需要，除非你有特殊需求）
        String languageCode = new String(payload, 1, languageCodeLength, StandardCharsets.US_ASCII);

        // 解析文本数据
        int textStartIndex = 1 + languageCodeLength;
        int textLength = payload.length - textStartIndex;

        if (textLength <= 0) {
            Log.w("NfcBroadcastReceiver", "No text data found");
            return null;
        }

        try {
            return new String(payload, textStartIndex, textLength, Charset.forName(textEncoding));
        } catch (Exception e) {
            Log.w("NfcBroadcastReceiver", "Unsupported charset: " + textEncoding, e);
            return null;
        }
    }
}
