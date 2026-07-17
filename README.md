# QLKH - Quản Lý Kho Hàng

## Chức năng

- **Quản lý sản phẩm**: Thêm, sửa, xóa sản phẩm với hình ảnh (tối đa 4 hình)
- **Điều chỉnh tồn kho**: Chỉnh sửa số lượng tồn kho trực tiếp (+/-) với thông báo cảnh báo khi sắp hết hàng
- **Tìm kiếm và lọc**: Tìm kiếm theo tên, mã sản phẩm; lọc theo danh mục, mức tồn kho, giá, số lượng; sắp xếp A-Z, giá tăng/giảm
- **Thông báo thông minh**: Tự động tạo thông báo khi thêm/sửa/xóa sản phẩm, cảnh báo tồn kho thấp
- **Bảng điều khiển**: Tổng quan giá trị kho, số lượng sản phẩm, danh mục, sản phẩm sắp hết hàng
- **Theme sáng/tối**: Hỗ trợ giao diện sáng và tối với màu sắc thương hiệu

## Công nghệ

| Lĩnh vực | Công nghệ |
|---|---|
| UI | Jetpack Compose, Material3 |
| Navigation | Jetpack Navigation Compose |
| Database | Room (KSP) |
| Ảnh | Coil |
| JSON | Moshi (KSP Codegen) |
| Async | Kotlin Coroutines |
| Build | Gradle KTS, AGP |

## Cấu trúc dự án

```
app/src/main/java/com/example/
├── MainActivity.kt                  # Entry point, Navigation, Bottom Nav
├── data/
│   ├── Product.kt                   # Room Entity + TypeConverter
│   ├── ProductDao.kt                # Room DAO
│   ├── ProductRepository.kt         # Repository
│   ├── WarehouseDatabase.kt         # Room Database (v3)
│   └── WarehouseNotification.kt     # Notification Entity
└── ui/
    ├── ProductViewModel.kt          # ViewModel + state management
    ├── screens/
    │   ├── DashboardScreen.kt       # Bảng điều khiển
    │   ├── ItemsListScreen.kt       # Danh sách sản phẩm + lọc
    │   ├── ProductDetailScreen.kt   # Chi tiết sản phẩm
    │   ├── AddEditProductScreen.kt  # Form thêm/sửa sản phẩm
    │   ├── SearchScreen.kt          # Màn hình tìm kiếm
    │   ├── NotificationsScreen.kt   # Hộp thư thông báo
    │   ├── WarehouseUiComponents.kt # Thành phần UI chung
    │   └── GlassShineEffect.kt      # Hiệu ứng gương sáng
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## Cài đặt

- **Android Studio**: Ladybug hoặc mới hơn
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **JDK**: 11

```bash
# Clone repository
git clone <repo-url>

# Mở trong Android Studio và sync Gradle
# Chạy trên emulator hoặc thiết bị thật
```

## Lưu ý

- Dữ liệu mẫu (5 sản phẩm) sẽ tự động được tạo khi chạy lần đầu
- File `.env` được Secrets Gradle Plugin sử dụng (copy từ `.env.example` nếu cần)
- Firebase đã được cấu hình sẵn nhưng không bắt buộc (sẽ hiện cảnh báo nếu không có `google-services.json`)
