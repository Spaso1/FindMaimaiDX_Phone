package org.ast.findmaimaidx.ui;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import org.ast.findmaimaidx.been.ChartPlay;
import org.ast.findmaimaidx.R;

import java.io.File;
import java.util.List;

public class ChartPlayDetailActivity extends AppCompatActivity {
    private TextView songNameTextView;
    private TextView lengthTextView;
    private TextView difficultyTextView;
    private TextView likesTextView;
    private TextView downloadsTextView;
    private TextView authorTextView;
    private Button downloadButton;

    private ChartPlay chartPlay;
    private DownloadManager downloadManager;
    private long downloadId;
    private File downloadFile;
    private static final int FOLDER_PICK_REQUEST_CODE = 1001; // 可以是任意不冲突的整数值

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_play_detail);

        songNameTextView = findViewById(R.id.songNameTextView);
        lengthTextView = findViewById(R.id.lengthTextView);
        difficultyTextView = findViewById(R.id.difficultyTextView);
        likesTextView = findViewById(R.id.likesTextView);
        downloadsTextView = findViewById(R.id.downloadsTextView);
        authorTextView = findViewById(R.id.authorTextView);
        downloadButton = findViewById(R.id.downloadButton);

        chartPlay = (ChartPlay) getIntent().getSerializableExtra("chartPlay");

        songNameTextView.setText(chartPlay.getSongName());
        lengthTextView.setText(chartPlay.getLength());
        difficultyTextView.setText(chartPlay.getDifficulty());
        likesTextView.setText(String.valueOf(chartPlay.getLikes()));
        downloadsTextView.setText(String.valueOf(chartPlay.getDownloads()));
        authorTextView.setText(chartPlay.getAuthor());

        downloadButton.setOnClickListener(v -> downloadChartPlay());

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void downloadChartPlay() {
        // Create a directory in the app's private directory
        File downloadDir = new File(getExternalFilesDir(null), "downloads");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        downloadFile = new File(downloadDir, chartPlay.getSongName() + ".zip");

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(chartPlay.getChartZipUrl()));
        request.setTitle("Downloading Chart Play");
        request.setDescription("Downloading " + chartPlay.getSongName() + ".zip");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(downloadFile));
        request.setAllowedOverMetered(true); // Allow downloading over metered connections
        request.setAllowedOverRoaming(true); // Allow downloading over roaming

        // Avoid inserting into media provider
        request.setVisibleInDownloadsUi(false);

        downloadId = downloadManager.enqueue(request);
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadId == id) {
                openFileWithChooser();
            }
        }
    };

    private void openFileWithChooser() {
        // 获取下载的具体文件路径，避免重复添加 "files" 目录
        File file = new File(getExternalFilesDir("downloads"), chartPlay.getSongName() + ".zip");
        Uri uri = FileProvider.getUriForFile(this, "org.ast.findmaimaidx.fileprovider", file);

        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "*s/*");
        intent.setPackage("com.Reflektone.AstroDX"); // 指定目标应用的包名
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // 查询目标应用是否存在
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos.isEmpty()) {
            // 如果目标应用不存在，打开文件资源管理器并复制路径到剪切板
            openFileManagerAndCopyPaths(file);
        } else {
            try {
                this.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // 如果目标应用存在但无法处理文件，打开文件资源管理器并复制路径到剪切板
                openFileManagerAndCopyPaths(file);
            }
        }

        // 打印文件路径以确认路径正确性
        Log.d("ChartPlayDetailActivity", "File path: " + file.getAbsolutePath());
    }

    private void openFileManagerAndCopyPaths(File file) {
        // 获取文件所在的目录
        File folder = file.getParentFile();

        // 使用 FileProvider 生成 content:// 类型的 URI
        Uri uri = FileProvider.getUriForFile(this, "org.ast.findmaimaidx.fileprovider", folder);

        // 打开系统文件资源管理器
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "resource/folder");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            String errorMessage = "No file manager found to open the folder: " + folder.getName();
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            Log.e("ChartPlayDetailActivity", errorMessage, e);
        }

        // 复制文件路径和 "/android/data" 到剪切板
        String filePath = file.getAbsolutePath();
        String androidDataPath = "/android/data";

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("File Paths", filePath + "\n" + androidDataPath);
        clipboard.setPrimaryClip(clipData);

        Toast.makeText(this, "Paths copied to clipboard", Toast.LENGTH_SHORT).show();
        Log.d("ChartPlayDetailActivity", "Copied to clipboard: " + filePath + "\n" + androidDataPath);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }
}
