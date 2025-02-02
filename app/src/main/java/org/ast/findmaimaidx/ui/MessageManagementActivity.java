package org.ast.findmaimaidx.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.ast.findmaimaidx.R;

public class MessageManagementActivity extends AppCompatActivity {

    private TextView messageTextView;
    private TextView timeTextView;
    private Button deleteAllButton;
    private Button copyButton;
    private Button deleteButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_management);

        messageTextView = findViewById(R.id.messageTextView);
        timeTextView = findViewById(R.id.timeTextView);
        deleteAllButton = findViewById(R.id.deleteAllButton);
        copyButton = findViewById(R.id.copyButton);
        deleteButton = findViewById(R.id.deleteButton);

        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        String time = intent.getStringExtra("time");
        int position = intent.getIntExtra("position", -1);

        messageTextView.setText(message);
        timeTextView.setText(time);

        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("delete_all", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyMessage(message);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("delete_position", position);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void copyMessage(String message) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", message);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Message copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
