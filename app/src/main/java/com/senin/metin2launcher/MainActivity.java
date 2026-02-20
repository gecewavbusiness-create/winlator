package com.senin.metin2launcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // GecoMT2 sabitleri - optimize edilmiş
    private static final String GAME_DIR = "/storage/emulated/0/Metin2";
    private static final String EXE_PATH = "D:\\GecoMT2.exe";
    private static final String SHORTCUT_FILE = GAME_DIR + "/shortcuts/geco.desktop";
    private static final int STORAGE_PERMISSION_CODE = 101;

    private Button btnStart;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();
        checkGameStatus();
    }

    private void initializeViews() {
        btnStart = findViewById(R.id.btnStart);
        txtStatus = findViewById(R.id.txtStatus);
    }

    private void setupClickListeners() {
        btnStart.setOnClickListener(v -> handleStartClick());
    }

    private void handleStartClick() {
        if (!checkStoragePermission()) {
            requestStoragePermission();
            return;
        }

        if (!isWinlatorInstalled()) {
            showWinlatorError();
            return;
        }

        launchGecoMT2();
    }

    private void showWinlatorError() {
        txtStatus.setText("Hata: GecoMT2 Engine kurulu değil!");
        Toast.makeText(this, "Önce GecoMT2 Engine kurun!", Toast.LENGTH_LONG).show();
    }

    private void launchGecoMT2() {
        txtStatus.setText("GecoMT2 başlatılıyor...");
        
        if (createShortcut()) {
            startWinlator();
            moveTaskToBack(true); // Arka plana geç
        } else {
            txtStatus.setText("Hata: Başlatıcı oluşturulamadı!");
        }
    }

    private void checkGameStatus() {
        File exeFile = new File(GAME_DIR + "/GecoMT2.exe");
        updateUIBasedOnGameStatus(exeFile.exists());
    }

    private void updateUIBasedOnGameStatus(boolean gameReady) {
        if (gameReady) {
            btnStart.setEnabled(true);
            btnStart.setBackgroundColor(0xFF4CAF50);
            btnStart.setText("GECO MT2 BAŞLAT");
            txtStatus.setText("GecoMT2 hazır!");
        } else {
            btnStart.setEnabled(false);
            btnStart.setBackgroundColor(0xFF757575);
            btnStart.setText("DOSYA EKSİK");
            txtStatus.setText("GecoMT2.exe bulunamadı");
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestManageExternalStoragePermission();
        } else {
            requestLegacyStoragePermissions();
        }
    }

    private void requestManageExternalStoragePermission() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
            startActivityForResult(intent, STORAGE_PERMISSION_CODE);
        } catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, STORAGE_PERMISSION_CODE);
        }
    }

    private void requestLegacyStoragePermissions() {
        ActivityCompat.requestPermissions(this, 
            new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE, 
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 
            STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkGameStatus();
            } else {
                txtStatus.setText("Depolama izni gerekli!");
                Toast.makeText(this, "Depolama izni gerekli!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STORAGE_PERMISSION_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                checkGameStatus();
            } else {
                txtStatus.setText("Depolama izni gerekli!");
                Toast.makeText(this, "Depolama izni gerekli!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean createShortcut() {
        try {
            File shortcutDir = new File(GAME_DIR + "/shortcuts");
            if (!shortcutDir.exists()) shortcutDir.mkdirs();

            File shortcutFile = new File(SHORTCUT_FILE);
            String shortcutContent = createShortcutContent();
            
            FileWriter writer = new FileWriter(shortcutFile);
            writer.write(shortcutContent);
            writer.flush();
            writer.close();
            
            return shortcutFile.exists();
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String createShortcutContent() {
        return "[Desktop Entry]\n" +
               "Name=GecoMT2\n" +
               "Exec=wine \"" + EXE_PATH + "\"\n" +
               "Type=Application\n" +
               "Terminal=false\n" +
               "Path=D:\\";
    }

    private void startWinlator() {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.winlator", "com.winlator.XServerDisplayActivity");
            intent.putExtra("container_id", 1);
            intent.putExtra("shortcut_path", SHORTCUT_FILE);
            startActivity(intent);
        } catch (Exception e) {
            txtStatus.setText("Winlator başlatılamadı: " + e.getMessage());
            Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isWinlatorInstalled() {
        try {
            getPackageManager().getPackageInfo("com.winlator", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
