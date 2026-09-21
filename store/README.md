# Play Store listing — Photo Recovery

Bộ nội dung sẵn sàng dán vào Google Play Console.

## Upload nhanh

1. Mở [Play Console](https://play.google.com/console) → app `com.mobile.photo.recovery.io` → **Grow** → **Store presence** → **Main store listing**.
2. Ngôn ngữ mặc định: **English (United States)**. Dán `store/listing/en-US.txt`.
3. Thêm bản dịch **Vietnamese** và các locale còn lại trong `store/listing/`.
4. Đồ họa:
   - High-res icon: `store/graphics/icon-512.png` (512×512)
   - Feature graphic: `store/graphics/feature-graphic-1024x500.png` (1024×500, bắt buộc)
   - Phone screenshots (EN): `store/graphics/phone/01` → `06` theo thứ tự
   - Phone screenshots (VI): `store/graphics/phone-vi/` khi chỉnh listing tiếng Việt
5. Form còn lại (ads, Data safety, IARC, category): xem `store/PLAY_CONSOLE.txt`.
6. Privacy policy URL (bắt buộc trước khi publish):
   `https://htmlpreview.github.io/?https://raw.githubusercontent.com/Thuquanguyen/photo_recovery_android/main/policy/privacy_policy.html`

## Copy đã viết sẵn

| Locale | File | Title | Short |
| --- | --- | --- | --- |
| en-US (default) | `listing/en-US.txt` | Photo Recovery | Restore copies, clean duplicates… |
| vi-VN | `listing/vi-VN.txt` | Photo Recovery | Lưu ảnh vào album Restored… |
| es-419 | `listing/es-419.txt` | Photo Recovery | Guarda copias… |
| fr-FR | `listing/fr-FR.txt` | Photo Recovery | Enregistrez copies… |
| de-DE | `listing/de-DE.txt` | Photo Recovery | Kopien speichern… |
| ja-JP | `listing/ja-JP.txt` | Photo Recovery | コピーを保存し… |
| ko-KR | `listing/ko-KR.txt` | Photo Recovery | 복사본 저장… |
| zh-CN | `listing/zh-CN.txt` | Photo Recovery | 保存副本… |
| pt-BR | `listing/pt-BR.txt` | Photo Recovery | Salve cópias… |

Trong mỗi file, chỉ copy **dòng nội dung** (không copy nhãn `TITLE` / `SHORT DESCRIPTION` / `FULL DESCRIPTION`).

## Vì sao mô tả không hứa “khôi phục ảnh đã xóa vĩnh viễn”

App **không** undelete file đã bị xóa khỏi bộ nhớ. Photo Recovery duyệt MediaStore, xuất bản sao vào album `Restored`, dọn trùng / screenshot, và giữ file trong Vault trên máy.

Google Play thường từ chối app recovery nếu listing / screenshot hứa khôi phục file đã format, factory reset, hoặc sector-level undelete. Copy hiện tại mô tả đúng việc app làm, vẫn giữ từ khóa restore / clean / vault.

## Tạo lại ảnh

```
python store/scripts/generate_store_graphics.py
```

Cần Pillow. Icon nguồn: asset `play-icon-512.png` trong project Cursor.
