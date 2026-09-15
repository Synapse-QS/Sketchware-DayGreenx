# Walkthrough - Fitur Instalasi via Shizuku

Saya telah menambahkan fitur "Install projects via Shizuku" yang berfungsi sama seperti fitur Root, memungkinkan instalasi APK secara otomatis setelah proses *build* selesai.

## Perubahan Utama

### 1. Integrasi Library Shizuku (v12.1.0)
- Menambahkan dependensi `dev.rikka.shizuku:api:12.1.0` dan `dev.rikka.shizuku:provider:12.1.0`.
- Deklarasi **`ShizukuProvider`** di manifest dengan authority `${applicationId}.shizuku`. Ini wajib untuk versi 12.1.0 agar binder bisa diterima.
- Menambahkan blok `<queries>` untuk paket `moe.shizuku.privileged.api`.
- Menambahkan izin `moe.shizuku.manager.permission.API_V23`.

### 2. Pengaturan Baru di ConfigActivity
- Menambahkan kategori **"Shizuku Features"** di bawah kategori Root Features.
- Menambahkan dua opsi baru:
    - **Install projects with Shizuku access**: Mengaktifkan instalasi otomatis via Shizuku. Saat diaktifkan, aplikasi akan mengecek status Shizuku dan meminta izin jika diperlukan.
    - **Launch projects after installing**: Membuka aplikasi secara otomatis setelah berhasil diinstal via Shizuku.

### 3. Logika Instalasi di DesignActivity
- Memperbarui metode `installBuiltApk` untuk mendukung Shizuku.
- Mengimplementasikan `installWithShizuku()` yang menggunakan `pm install` melalui `rikka.shizuku.Shizuku.newProcess`. Metode ini memungkinkan pengiriman data APK secara langsung ke proses instalasi sistem dengan hak akses tinggi yang diberikan oleh Shizuku.

## Cara Penggunaan

1.  Buka **Settings** di dalam aplikasi Sketchware.
2.  Gulir ke bawah hingga menemukan kategori **Shizuku Features**.
3.  Aktifkan sakelar **Install projects with Shizuku access**.
4.  Jika muncul permintaan izin dari Shizuku, klik **Allow**.
5.  Bangun (*build*) proyek Anda seperti biasa. Setelah selesai, aplikasi akan terinstal secara otomatis tanpa perlu menekan tombol instal secara manual.

> [!NOTE]
> Fitur Root dan Shizuku saling meniadakan. Jika Anda mengaktifkan Shizuku, fitur Root akan otomatis dinonaktifkan, dan sebaliknya.

## Perbaikan Masalah "Shizuku Not Running"
- Menambahkan **`ShizukuProvider`** ke dalam `AndroidManifest.xml`. Provider ini sangat penting agar aplikasi dapat mendeteksi layanan Shizuku yang berjalan di sistem.
- Menambahkan izin `moe.shizuku.manager.permission.API_V23` yang diperlukan untuk berinteraksi dengan Shizuku pada beberapa versi Android.

render_diffs(file:///C:/Users/only1/StudioProjects/Sketchware-DayGreen/app/src/main/AndroidManifest.xml)
render_diffs(file:///C:/Users/only1/StudioProjects/Sketchware-DayGreen/app/src/main/java/mod/hilal/saif/activities/tools/ConfigActivity.java)
render_diffs(file:///C:/Users/only1/StudioProjects/Sketchware-DayGreen/app/src/main/java/com/besome/sketch/design/DesignActivity.java)
