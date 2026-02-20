# Winlator Ayarları (GecoMT2 için)

## 🎯 Gerekli Ayarlar

GecoMT2 Launcher'ın çalışması için Winlator'da şu ayarı yapmalısın:

### 1. Sürücü (Drive) Ayarı
1. Winlator'ı aç
2. Container Settings (Konteyner Ayarları) gir
3. **Drives (Sürücüler)** sekmesine git
4. Yeni sürücü ekle veya mevcut düzenle:
   - **Drive Letter:** `D:`
   - **Path:** `/storage/emulated/0/Metin2`
5. Kaydet ve çık

### 2. Container Oluşturma
- Yeni container oluştur veya mevcut kullan
- Container adı önemli değil (varsayılan 1)

## 🚀 Çalışma Akışı

1. **Launcher açılır** → GecoMT2.exe kontrol eder
2. **Dosya yoksa** → İndirir
3. **.desktop oluştur** → Winlator anlar
4. **Winlator başlat** → Direkt GecoMT2 Patcher açılır
5. **Patcher çalışır** → Pack dosyalarını indirir
6. **Oyun başlar** → Metin2 dünyası! 🎮

## 📁 Dosya Yapısı

```
/storage/emulated/0/Metin2/
├── GecoMT2.exe          (Ana patcher)
├── shortcuts/
│   └── geco.desktop     (Winlator kısayolu)
├── pack/               (Patcher indirir)
│   ├── root.epk
│   └── root.eix
└── ...                 (Diğer oyun dosyaları)
```

## ⚡ Hızlı Test

1. Winlator'ı kur
2. Yukarıdaki D: sürücü ayarını yap
3. GecoMT2 Launcher'ı kur ve test et

**Bu kadar!** 🎉
