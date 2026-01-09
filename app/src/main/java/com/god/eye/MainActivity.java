package com.god.eye;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity
 * - 选择 APK
 * - 执行真实分析
 * - 跳转结果页
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQ_PICK_APK = 1001;

    private TextView tvApkPath;
    private Button btnPickApk;
    private Button btnStart;

    private Uri selectedApkUri;
    private File cachedApkFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvApkPath = findViewById(R.id.tvApkPath);
        btnPickApk = findViewById(R.id.btnPickApk);
        btnStart = findViewById(R.id.btnStartAnalyze);

        btnPickApk.setOnClickListener(v -> pickApk());
        btnStart.setOnClickListener(v -> startAnalyze());
    }

    /* ================= 选择 APK ================= */

    private void pickApk() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.android.package-archive");
        startActivityForResult(intent, REQ_PICK_APK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_APK && resultCode == Activity.RESULT_OK && data != null) {
            selectedApkUri = data.getData();
            if (selectedApkUri != null) {

                // ✅ 取文件名用于显示
                String apkName = getFileName(selectedApkUri);
                tvApkPath.setText("已选择 APK：\n" + apkName);

                // ✅ 拷贝到 cache 目录供分析
                cachedApkFile = copyToCache(selectedApkUri);
            }
        }
    }

    /**
     * 从 content:// URI 中获取文件名
     */
    private String getFileName(Uri uri) {
        String name = "unknown.apk";
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    name = cursor.getString(idx);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return name;
    }

    /**
     * 拷贝 APK 到 cache 目录
     */
    private File copyToCache(Uri uri) {
        try {
            File out = new File(getCacheDir(), "target.apk");
            InputStream is = getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(out);

            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }

            is.close();
            fos.close();
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* ================= 核心分析 ================= */

    private void startAnalyze() {
        if (cachedApkFile == null || !cachedApkFile.exists()) {
            Toast.makeText(this, "请先选择 APK", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "正在分析，请稍候…", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                DexAnalyzer dexAnalyzer = new DexAnalyzer(cachedApkFile);
                List<DexAnalyzer.DexItem> dexList = dexAnalyzer.loadAllDex();

                String appPkg = getPackageNameFromApk(cachedApkFile);
                if (appPkg == null) appPkg = "";

                List<InjectResult> allResults = new ArrayList<>();

                for (DexAnalyzer.DexItem dex : dexList) {
                    allResults.addAll(
                            SmaliScanner.scanDex(dex.data, appPkg)
                    );
                }

                ArrayList<InjectResult> finalResults = new ArrayList<>(allResults);

                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                    ResultHolder.results = finalResults;
                    startActivity(intent);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "分析失败", Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    /**
     * 解析 APK 包名
     */
    private String getPackageNameFromApk(File apk) {
        try {
            android.content.pm.PackageManager pm = getPackageManager();
            android.content.pm.PackageInfo info =
                    pm.getPackageArchiveInfo(apk.getAbsolutePath(), 0);
            if (info != null) {
                return info.packageName;
            }
        } catch (Exception ignored) {}
        return null;
    }
}
