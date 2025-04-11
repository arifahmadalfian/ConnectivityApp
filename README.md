# 🌐 Koneksi Status Monitor

Aplikasi Android menggunakan **Jetpack Compose** untuk memantau status dan kualitas koneksi internet secara real-time.  
Terinspirasi dari indikator jaringan pada aplikasi **m-Banking BCA**, aplikasi ini menampilkan status koneksi dengan warna berbeda berdasarkan kecepatan respon (latency) ke server.

![status-demo](https://user-images.githubusercontent.com/your-username/demo-status.gif) <!-- (Opsional: ganti dengan screenshot/rekaman aplikasi) -->

---

## ✨ Fitur Utama

- ✅ Deteksi status koneksi internet (`Connected` / `Disconnected`)
- ⏱️ Perhitungan latency ke server (`google.com`) setiap 5 detik
- 🔴🟡🟢 Indikator warna sesuai kualitas koneksi:
  - 🟢 Hijau: Koneksi Bagus (< 100ms)
  - 🟡 Kuning: Koneksi Lemah (100–500ms)
  - 🔴 Merah: Koneksi Buruk atau Tidak Ada Internet
- ⚙️ Arsitektur modular menggunakan `interface` dan `stateIn` dari Kotlin Flow
- 💡 Responsive UI dengan **Jetpack Compose**

---

## 📱 Teknologi yang Digunakan

- Kotlin
- Jetpack Compose
- ViewModel + StateFlow
- Kotlin Coroutines
- ConnectivityManager (Android NetworkCallback)
- Clean Architecture

---

## 📦 Instalasi & Build

1. Clone repository ini:
   ```bash
   git clone https://github.com/username/koneksi-status-monitor.git
   cd koneksi-status-monitor
