package com.senin.metin2launcher;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

public class PatcherManager {
    
    private static final String TAG = "PatcherManager";
    private static final String MANIFEST_URL = "https://seninsiten.com/patch/manifest.json";
    private static final String METIN2_FOLDER = Environment.getExternalStorageDirectory() + "/Download/Metin2";
    
    public interface PatcherCallback {
        void onProgress(String dosyaAdi, int yuzde);
        void onComplete();
        void onError(String hata);
    }
    
    public void dosyalariKontrolEtVeGuncelle(Context context, PatcherCallback callback) {
        new Thread(() -> {
            try {
                callback.onProgress("Manifest kontrol ediliyor...", 0);
                
                // 1. Manifest'i indir
                String manifestJson = indirManifest();
                if (manifestJson == null) {
                    callback.onError("Manifest indirilemedi!");
                    return;
                }
                
                // 2. JSON'ı parse et
                JSONArray dosyalar = new JSONArray(manifestJson);
                int toplamDosya = dosyalar.length();
                int tamamlanan = 0;
                
                // 3. Metin2 klasörünü oluştur
                File metin2Klasor = new File(METIN2_FOLDER);
                if (!metin2Klasor.exists()) {
                    metin2Klasor.mkdirs();
                }
                
                // 4. Her dosyayı kontrol et
                for (int i = 0; i < dosyalar.length(); i++) {
                    JSONObject dosya = dosyalar.getJSONObject(i);
                    String name = dosya.getString("name");
                    String url = dosya.getString("url");
                    String beklenenHash = dosya.getString("hash");
                    
                    callback.onProgress(name, (tamamlanan * 100) / toplamDosya);
                    
                    File dosyaYolu = new File(METIN2_FOLDER, name);
                    
                    // Dosya var mı ve hash doğru mu?
                    if (dosyaYolu.exists()) {
                        String mevcutHash = hashHesapla(dosyaYolu);
                        if (mevcutHash.equals(beklenenHash)) {
                            Log.d(TAG, name + " zaten güncel");
                            tamamlanan++;
                            continue;
                        }
                    }
                    
                    // Dosyayı indir
                    callback.onProgress(name + " indiriliyor...", (tamamlanan * 100) / toplamDosya);
                    boolean indirildi = indirDosya(url, dosyaYolu);
                    if (!indirildi) {
                        callback.onError(name + " indirilemedi!");
                        return;
                    }
                    
                    // Hash kontrolü
                    String yeniHash = hashHesapla(dosyaYolu);
                    if (!yeniHash.equals(beklenenHash)) {
                        callback.onError(name + " hash uyuşmuyor!");
                        return;
                    }
                    
                    Log.d(TAG, name + " başarıyla indirildi");
                    tamamlanan++;
                }
                
                callback.onComplete();
                
            } catch (Exception e) {
                Log.e(TAG, "Patcher hatası: " + e.getMessage(), e);
                callback.onError("Patcher hatası: " + e.getMessage());
            }
        }).start();
    }
    
    private String indirManifest() {
        try {
            // TODO: Gerçek manifest indirme kodu
            // Şimdilik örnek JSON döndürüyoruz
            return "[{\"name\":\"metin2client.exe\",\"size\":154200,\"hash\":\"sha256_buraya_gelecek\",\"url\":\"https://seninsiten.com/patch/metin2client.exe\"}]";
        } catch (Exception e) {
            Log.e(TAG, "Manifest indirilemedi: " + e.getMessage());
            return null;
        }
    }
    
    private boolean indirDosya(String url, File hedefDosya) {
        try {
            // TODO: Gerçek dosya indirme kodu (DownloadManager ile)
            // Şimdilik true döndürüyoruz
            Log.d(TAG, "Dosya indiriliyor: " + url + " -> " + hedefDosya.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Dosya indirilemedi: " + e.getMessage());
            return false;
        }
    }
    
    private String hashHesapla(File dosya) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(dosya);
            byte[] byteArray = new byte[1024];
            int bytesCount;
            
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            
            fis.close();
            byte[] bytes = digest.digest();
            
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            
            return "sha256_" + sb.toString();
            
        } catch (NoSuchAlgorithmException | IOException e) {
            Log.e(TAG, "Hash hesaplanamadı: " + e.getMessage());
            return "";
        }
    }
    
    public boolean tumDosyalarHazirMi() {
        File metin2Klasor = new File(METIN2_FOLDER);
        if (!metin2Klasor.exists()) return false;
        
        // TODO: Manifest'e göre gerçek kontrol yapılacak
        File exeDosya = new File(metin2Klasor, "metin2client.exe");
        return exeDosya.exists();
    }
}
