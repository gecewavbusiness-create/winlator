package com.senin.metin2launcher;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class WinlatorHelper {
    
    public static void oyunuBaslat(Context context, int containerId, String shortcutPath) {
        try {
            // Winlator kurulu mu kontrol et
            Intent winlatorIntent = context.getPackageManager().getLaunchIntentForPackage("com.winlator");
            if (winlatorIntent == null) {
                Toast.makeText(context, "Winlator kurulu değil! Lütfen önce Winlator kurun.", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent();
            // Winlator'ın ana paket adı
            intent.setClassName("com.winlator", "com.winlator.XServerDisplayActivity");
            
            // Winlator'ın beklediği parametreler
            intent.putExtra("container_id", containerId); // Örn: 1
            
            // En sağlam yol: Winlator içinde oluşturulmuş bir kısayolu tetiklemek
            intent.putExtra("shortcut_path", shortcutPath); // Örn: "C:\\Metin2\\metin2client.exe"
            
            // Alternatif yol: Direkt exe yolu (bazı sürümlerde çalışır)
            intent.putExtra("exe_path", "/storage/emulated/0/Download/Metin2/metin2client.exe");

            context.startActivity(intent);
            Toast.makeText(context, "Metin2 başlatılıyor...", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(context, "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    public static boolean winlatorKuruluMu(Context context) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.winlator");
            return intent != null;
        } catch (Exception e) {
            return false;
        }
    }
}
