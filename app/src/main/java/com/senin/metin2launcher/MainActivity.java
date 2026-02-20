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

    // Sabit yollar - Winlator'ın anladığı formatta
    private static final String GAME_DIR = "/storage/emulated/0/Metin2";
    private static final String EXE_PATH = "D:\\GecoMT2.exe"; // Winlator içi yol
    private static final String SHORTCUT_FILE = GAME_DIR + "/shortcuts/geco.desktop";
    private static final int STORAGE_PERMISSION_CODE = 101;

    private Button btnStart;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btnStart);
        txtStatus = findViewById(R.id.txtStatus);

        btnStart.setOnClickListener(v -> {
            if (!checkStoragePermission()) {
                requestStoragePermission();
                return;
            }

            if (!isWinlatorInstalled()) {
                txtStatus.setText("Hata: GecoMT2 Engine kurulu değil!");
                Toast.makeText(this, "Önce GecoMT2 Engine kurun!", Toast.LENGTH_LONG).show();
                return;
            }

            // Önce shortcut dosyasını oluştur/güncelle
            txtStatus.setText("Başlatıcı hazırlanıyor...");
            if (createShortcut()) {
                txtStatus.setText("GecoMT2 başlatılıyor...");
                launchGame();
            } else {
                txtStatus.setText("Hata: Başlatıcı oluşturulamadı!");
            }
        });

        // Başlangıç durumunu kontrol et
        checkGameStatus();
    }

    private void checkGameStatus() {
        File exeFile = new File(GAME_DIR + "/GecoMT2.exe");
        if (exeFile.exists()) {
            btnStart.setEnabled(true);
            btnStart.setBackgroundColor(0xFF4CAF50); // Yeşil
            btnStart.setText("GECO MT2 BAŞLAT");
            txtStatus.setText("GecoMT2 hazır!");
        } else {
            btnStart.setEnabled(false);
            btnStart.setBackgroundColor(0xFF757575); // Gri
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
        } else {
            ActivityCompat.requestPermissions(this, 
                new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE, 
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 
                STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi, butonu yeniden kontrol et
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
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    checkGameStatus();
                } else {
                    txtStatus.setText("Depolama izni gerekli!");
                    Toast.makeText(this, "Depolama izni gerekli!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private boolean createShortcut() {
        try {
            File dir = new File(GAME_DIR + "/shortcuts");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(SHORTCUT_FILE);
            
            // Winlator'ın en sevdiği format - crash-proof!
            String content = "[Desktop Entry]\n" +
                             "Name=GecoMT2\n" +
                             "Exec=wine \"" + EXE_PATH + "\"\n" +
                             "Type=Application\n" +
                             "Terminal=false\n" +
                             "Path=D:\\";
                             
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.flush();
            writer.close();
            
            return file.exists();
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void launchGame() {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.winlator", "com.winlator.XServerDisplayActivity");
            intent.putExtra("container_id", 1); // Varsayılan container
            intent.putExtra("shortcut_path", SHORTCUT_FILE);
            
            startActivity(intent);
            
            // Uygulama arka plana geçsin
            moveTaskToBack(true);
            
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
